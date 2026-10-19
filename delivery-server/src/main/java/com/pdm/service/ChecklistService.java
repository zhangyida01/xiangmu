package com.pdm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pdm.common.BizException;
import com.pdm.entity.ChecklistItem;
import com.pdm.entity.ChecklistTemplate;
import com.pdm.entity.Project;
import com.pdm.mapper.ChecklistItemMapper;
import com.pdm.mapper.ChecklistTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChecklistService {

    private final ChecklistTemplateMapper templateMapper;
    private final ChecklistItemMapper itemMapper;
    private final ProjectService projectService;
    private final AuditService auditService;

    public List<ChecklistTemplate> templates(String projectType) {
        LambdaQueryWrapper<ChecklistTemplate> w = new LambdaQueryWrapper<>();
        w.eq(ChecklistTemplate::getEnabled, true);
        if (projectType != null && !projectType.isBlank()) {
            w.in(ChecklistTemplate::getProjectType, projectType, "*");
        }
        w.orderByAsc(ChecklistTemplate::getProjectType).orderByAsc(ChecklistTemplate::getSortNo);
        return templateMapper.selectList(w);
    }

    public List<ChecklistTemplate> allTemplates() {
        return templateMapper.selectList(new LambdaQueryWrapper<ChecklistTemplate>()
                .orderByAsc(ChecklistTemplate::getProjectType).orderByAsc(ChecklistTemplate::getSortNo));
    }

    public Long createTemplate(ChecklistTemplate t) {
        t.setId(null);
        if (t.getEnabled() == null) {
            t.setEnabled(true);
        }
        if (t.getRequired() == null) {
            t.setRequired(true);
        }
        if (t.getSortNo() == null) {
            t.setSortNo(0);
        }
        templateMapper.insert(t);
        return t.getId();
    }

    public void updateTemplate(Long id, ChecklistTemplate t) {
        requireTemplate(id);
        t.setId(id);
        templateMapper.updateById(t);
    }

    public void deleteTemplate(Long id) {
        requireTemplate(id);
        templateMapper.deleteById(id);
    }

    public List<ChecklistItem> items(Long projectId) {
        return itemMapper.selectList(new LambdaQueryWrapper<ChecklistItem>()
                .eq(ChecklistItem::getProjectId, projectId)
                .orderByAsc(ChecklistItem::getSortNo).orderByAsc(ChecklistItem::getId));
    }

    public Map<String, Object> progress(Long projectId) {
        List<ChecklistItem> items = items(projectId);
        long done = items.stream().filter(i -> !"PENDING".equals(i.getStatus())).count();
        long missingRequired = items.stream()
                .filter(i -> Boolean.TRUE.equals(i.getRequired()) && "PENDING".equals(i.getStatus())).count();
        Map<String, Object> m = new HashMap<>();
        m.put("total", items.size());
        m.put("done", done);
        m.put("missingRequired", missingRequired);
        m.put("rate", items.isEmpty() ? 0 : Math.round(done * 100.0 / items.size()));
        m.put("items", items);
        return m;
    }

    /** 按项目类型从模板实例化清单，已存在同名项则跳过 */
    @Transactional(rollbackFor = Exception.class)
    public int generate(Long projectId, String projectType) {
        Project p = projectService.get(projectId);
        String type = projectType == null || projectType.isBlank() ? p.getProjectType() : projectType;
        List<ChecklistTemplate> templates = templates(type);
        Set<String> existing = new HashSet<>();
        for (ChecklistItem i : items(projectId)) {
            existing.add(i.getItemName());
        }
        int added = 0;
        int sort = existing.size();
        for (ChecklistTemplate t : templates) {
            if (existing.contains(t.getItemName())) {
                continue;
            }
            ChecklistItem item = new ChecklistItem();
            item.setProjectId(projectId);
            item.setTemplateId(t.getId());
            item.setItemName(t.getItemName());
            item.setRequired(t.getRequired());
            item.setStatus("PENDING");
            item.setSortNo(t.getSortNo() == null ? ++sort : t.getSortNo());
            itemMapper.insert(item);
            added++;
        }
        projectService.refreshDocRate(projectId);
        auditService.record("PROJECT", "GENERATE_CHECKLIST", "project", projectId, p.getName(), projectId,
                "生成 " + added + " 项交付清单", true, null);
        return added;
    }

    public Long addItem(Long projectId, ChecklistItem item) {
        projectService.get(projectId);
        item.setId(null);
        item.setProjectId(projectId);
        if (item.getStatus() == null) {
            item.setStatus("PENDING");
        }
        if (item.getRequired() == null) {
            item.setRequired(false);
        }
        itemMapper.insert(item);
        projectService.refreshDocRate(projectId);
        return item.getId();
    }

    public void updateItem(Long id, ChecklistItem form) {
        ChecklistItem item = requireItem(id);
        form.setId(id);
        form.setProjectId(item.getProjectId());
        itemMapper.updateById(form);
        projectService.refreshDocRate(item.getProjectId());
    }

    public void deleteItem(Long id) {
        ChecklistItem item = requireItem(id);
        itemMapper.deleteById(id);
        projectService.refreshDocRate(item.getProjectId());
    }

    public void bindDocument(Long itemId, Long documentId) {
        ChecklistItem item = requireItem(itemId);
        item.setDocumentId(documentId);
        item.setStatus(documentId == null ? "PENDING" : "SUBMITTED");
        itemMapper.updateById(item);
        projectService.refreshDocRate(item.getProjectId());
    }

    private ChecklistTemplate requireTemplate(Long id) {
        ChecklistTemplate t = templateMapper.selectById(id);
        if (t == null) {
            throw BizException.notFound("清单模板项");
        }
        return t;
    }

    private ChecklistItem requireItem(Long id) {
        ChecklistItem item = itemMapper.selectById(id);
        if (item == null) {
            throw BizException.notFound("交付清单项");
        }
        return item;
    }
}
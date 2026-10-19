package com.pdm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pdm.common.BizException;
import com.pdm.common.PageResult;
import com.pdm.entity.ChecklistItem;
import com.pdm.entity.Credential;
import com.pdm.entity.Customer;
import com.pdm.entity.Document;
import com.pdm.entity.Project;
import com.pdm.entity.ProjectEnv;
import com.pdm.entity.ProjectMember;
import com.pdm.entity.ProjectProduct;
import com.pdm.entity.SysAuditLog;
import com.pdm.mapper.ChecklistItemMapper;
import com.pdm.mapper.CredentialMapper;
import com.pdm.mapper.CustomerMapper;
import com.pdm.mapper.DocumentMapper;
import com.pdm.mapper.ProjectEnvMapper;
import com.pdm.mapper.ProjectMapper;
import com.pdm.mapper.ProjectMemberMapper;
import com.pdm.mapper.ProjectProductMapper;
import com.pdm.mapper.SysAuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectMapper projectMapper;
    private final CustomerMapper customerMapper;
    private final ProjectMemberMapper memberMapper;
    private final ProjectEnvMapper envMapper;
    private final ProjectProductMapper productMapper;
    private final DocumentMapper documentMapper;
    private final CredentialMapper credentialMapper;
    private final ChecklistItemMapper checklistItemMapper;
    private final SysAuditLogMapper auditLogMapper;
    private final AuditService auditService;
    private final ConfigService configService;

    public PageResult<Map<String, Object>> page(long page, long size, String keyword, String status,
                                                String projectType, Long customerId, String manager,
                                                Boolean archived, String orderBy, boolean asc) {
        LambdaQueryWrapper<Project> w = new LambdaQueryWrapper<>();
        w.eq(status != null && !status.isBlank(), Project::getStatus, status)
                .eq(projectType != null && !projectType.isBlank(), Project::getProjectType, projectType)
                .eq(customerId != null, Project::getCustomerId, customerId)
                .eq(manager != null && !manager.isBlank(), Project::getManager, manager)
                .eq(archived != null, Project::getArchived, archived)
                .and(keyword != null && !keyword.isBlank(), q -> q
                        .like(Project::getName, keyword).or()
                        .like(Project::getProjectNo, keyword).or()
                        .like(Project::getContractNo, keyword).or()
                        .like(Project::getTags, keyword))
                .orderBy(true, asc, sortColumn(orderBy));
        Page<Project> p = projectMapper.selectPage(new Page<>(page, size), w);

        Map<Long, String> customerNames = customerNames(p.getRecords());
        return PageResult.of(p, pr -> {
            Map<String, Object> m = toMap(pr);
            m.put("customerName", customerNames.get(pr.getCustomerId()));
            return m;
        });
    }

    public List<Map<String, Object>> all() {
        return projectMapper.selectList(new LambdaQueryWrapper<Project>()
                        .orderByDesc(Project::getId).last("LIMIT 1000"))
                .stream().map(this::toMap).toList();
    }

    public Project get(Long id) {
        Project p = projectMapper.selectById(id);
        if (p == null) {
            throw BizException.notFound("项目");
        }
        return p;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(Project p) {
        p.setId(null);
        ensureNoUnique(p.getProjectNo(), null);
        applyDefaults(p);
        projectMapper.insert(p);
        auditService.record("PROJECT", "CREATE", "project", p.getId(), p.getName(), p.getId(), "新建项目", true, null);
        return p.getId();
    }

    public void update(Long id, Project form) {
        Project p = get(id);
        if (Boolean.TRUE.equals(p.getLocked())) {
            throw BizException.conflict("项目已锁定，请先解锁");
        }
        if (form.getProjectNo() != null && !form.getProjectNo().equals(p.getProjectNo())) {
            ensureNoUnique(form.getProjectNo(), id);
        }
        form.setId(id);
        form.setCreateTime(null);
        form.setUpdateTime(null);
        projectMapper.updateById(form);
        auditService.record("PROJECT", "UPDATE", "project", id, form.getName(), id, "修改项目", true, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Project p = get(id);
        projectMapper.deleteById(id);
        memberMapper.delete(new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getProjectId, id));
        envMapper.delete(new LambdaQueryWrapper<ProjectEnv>().eq(ProjectEnv::getProjectId, id));
        productMapper.delete(new LambdaQueryWrapper<ProjectProduct>().eq(ProjectProduct::getProjectId, id));
        checklistItemMapper.delete(new LambdaQueryWrapper<ChecklistItem>().eq(ChecklistItem::getProjectId, id));
        auditService.record("PROJECT", "DELETE", "project", id, p.getName(), id, "删除项目（文档与凭据保留）", true, null);
    }

    public void changeStatus(Long id, String status, String reason) {
        Project p = get(id);
        requireEditable(p);
        String old = p.getStatus();
        p.setStatus(status);
        if ("ACCEPTED".equals(status)) {
            p.setActualAcceptDate(LocalDate.now());
        }
        if ("ARCHIVED".equals(status)) {
            p.setArchived(true);
            p.setArchiveDate(LocalDate.now());
        }
        projectMapper.updateById(p);
        auditService.record("PROJECT", "CHANGE_STATUS", "project", id, p.getName(), id,
                old + " -> " + status + (reason == null ? "" : "，原因：" + reason), true, null);
    }

    public void archive(Long id, boolean archived) {
        Project p = get(id);
        if (archived) {
            ensureArchivable(id);
        }
        // 置空字段必须用 UpdateWrapper，updateById 默认忽略 null
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Project> uw =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Project>()
                        .eq(Project::getId, id)
                        .set(Project::getArchived, archived)
                        .set(Project::getArchiveDate, archived ? LocalDate.now() : null);
        if (archived) {
            uw.set(Project::getStatus, "ARCHIVED");
        }
        projectMapper.update(null, uw);
        p.setArchived(archived);
        auditService.record("PROJECT", archived ? "ARCHIVE" : "UNARCHIVE", "project", id, p.getName(), id, null, true, null);
    }

    public void lock(Long id, boolean locked) {
        Project p = get(id);
        p.setLocked(locked);
        projectMapper.updateById(p);
        auditService.record("PROJECT", locked ? "LOCK" : "UNLOCK", "project", id, p.getName(), id, null, true, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long copy(Long id, String newNo, String newName, boolean copyEnv) {
        Project src = get(id);
        Project p = new Project();
        org.springframework.beans.BeanUtils.copyProperties(src, p, "id", "createTime", "updateTime", "deleted",
                "docRate", "archived", "locked", "archiveDate");
        p.setProjectNo(newNo);
        p.setName(newName);
        p.setStatus("PRESALE");
        p.setProgress(0);
        p.setDocRate(0);
        p.setArchived(false);
        p.setLocked(false);
        ensureNoUnique(newNo, null);
        projectMapper.insert(p);
        if (copyEnv) {
            for (ProjectEnv e : envMapper.selectList(new LambdaQueryWrapper<ProjectEnv>().eq(ProjectEnv::getProjectId, id))) {
                e.setId(null);
                e.setProjectId(p.getId());
                envMapper.insert(e);
            }
        }
        auditService.record("PROJECT", "COPY", "project", p.getId(), p.getName(), p.getId(), "复制自项目 " + src.getProjectNo(), true, null);
        return p.getId();
    }

    public Map<String, Object> overview(Long id) {
        Project p = get(id);
        Customer c = p.getCustomerId() == null ? null : customerMapper.selectById(p.getCustomerId());
        long docCount = documentMapper.selectCount(new LambdaQueryWrapper<Document>().eq(Document::getProjectId, id));
        long credCount = credentialMapper.selectCount(new LambdaQueryWrapper<Credential>().eq(Credential::getProjectId, id));
        long total = checklistItemMapper.selectCount(new LambdaQueryWrapper<ChecklistItem>().eq(ChecklistItem::getProjectId, id));
        long done = checklistItemMapper.selectCount(new LambdaQueryWrapper<ChecklistItem>()
                .eq(ChecklistItem::getProjectId, id).in(ChecklistItem::getStatus, "SUBMITTED", "CONFIRMED", "NA"));
        long missingRequired = checklistItemMapper.selectCount(new LambdaQueryWrapper<ChecklistItem>()
                .eq(ChecklistItem::getProjectId, id).eq(ChecklistItem::getRequired, true).eq(ChecklistItem::getStatus, "PENDING"));

        Map<String, Object> m = new HashMap<>();
        m.put("project", p);
        m.put("customer", c);
        m.put("members", memberMapper.selectList(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, id).orderByDesc(ProjectMember::getIsMain)));
        m.put("envs", envMapper.selectList(new LambdaQueryWrapper<ProjectEnv>().eq(ProjectEnv::getProjectId, id)));
        m.put("products", productMapper.selectList(new LambdaQueryWrapper<ProjectProduct>().eq(ProjectProduct::getProjectId, id)));
        m.put("docCount", docCount);
        m.put("credentialCount", credCount);
        m.put("checklistTotal", total);
        m.put("checklistDone", done);
        m.put("missingRequired", missingRequired);
        m.put("docRate", total == 0 ? 0 : Math.round(done * 100.0 / total));
        m.put("recentLogs", auditLogMapper.selectList(new LambdaQueryWrapper<SysAuditLog>()
                .eq(SysAuditLog::getProjectId, id).orderByDesc(SysAuditLog::getId).last("LIMIT 20")));
        return m;
    }

    public void requireEditable(Project p) {
        if (Boolean.TRUE.equals(p.getLocked())) {
            throw BizException.conflict("项目已锁定，不能修改");
        }
    }

    /** 归档前校验：doc.checklist.blockArchive=true 时，必填清单项未完成则拒绝归档 */
    private void ensureArchivable(Long projectId) {
        if (!configService.getBool("doc.checklist.blockArchive", false)) {
            return;
        }
        Long missing = checklistItemMapper.selectCount(new LambdaQueryWrapper<ChecklistItem>()
                .eq(ChecklistItem::getProjectId, projectId)
                .eq(ChecklistItem::getRequired, true)
                .notIn(ChecklistItem::getStatus, "SUBMITTED", "CONFIRMED", "NA"));
        if (missing != null && missing > 0) {
            throw BizException.conflict("还有 " + missing + " 项必填交付清单未完成，不能归档"
                    + "（如需强制归档，可在「字典与参数」中把 doc.checklist.blockArchive 设为 false）");
        }
    }

    public void refreshDocRate(Long projectId) {
        long total = checklistItemMapper.selectCount(new LambdaQueryWrapper<ChecklistItem>().eq(ChecklistItem::getProjectId, projectId));
        long done = checklistItemMapper.selectCount(new LambdaQueryWrapper<ChecklistItem>()
                .eq(ChecklistItem::getProjectId, projectId).in(ChecklistItem::getStatus, "SUBMITTED", "CONFIRMED", "NA"));
        Project p = projectMapper.selectById(projectId);
        if (p != null) {
            p.setDocRate(total == 0 ? 0 : (int) Math.round(done * 100.0 / total));
            projectMapper.updateById(p);
        }
    }

    private void applyDefaults(Project p) {
        if (p.getStatus() == null) {
            p.setStatus("PRESALE");
        }
        if (p.getProjectType() == null) {
            p.setProjectType("STANDARD");
        }
        if (p.getHealth() == null) {
            p.setHealth("GREEN");
        }
        if (p.getProgress() == null) {
            p.setProgress(0);
        }
        p.setDocRate(0);
        p.setArchived(false);
        p.setLocked(false);
    }

    private void ensureNoUnique(String projectNo, Long excludeId) {
        Long cnt = projectMapper.selectCount(new LambdaQueryWrapper<Project>()
                .eq(Project::getProjectNo, projectNo)
                .ne(excludeId != null, Project::getId, excludeId));
        if (cnt != null && cnt > 0) {
            throw BizException.conflict("项目编号已存在：" + projectNo);
        }
    }

    private com.baomidou.mybatisplus.core.toolkit.support.SFunction<Project, ?> sortColumn(String orderBy) {
        if (orderBy == null) {
            return Project::getId;
        }
        return switch (orderBy) {
            case "planAcceptDate" -> Project::getPlanAcceptDate;
            case "planOnlineDate" -> Project::getPlanOnlineDate;
            case "status" -> Project::getStatus;
            case "progress" -> Project::getProgress;
            case "docRate" -> Project::getDocRate;
            case "projectNo" -> Project::getProjectNo;
            case "name" -> Project::getName;
            case "createTime" -> Project::getCreateTime;
            default -> Project::getId;
        };
    }

    private Map<Long, String> customerNames(List<Project> projects) {
        Map<Long, String> map = new HashMap<>();
        List<Long> ids = projects.stream().map(Project::getCustomerId).filter(java.util.Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return map;
        }
        for (Customer c : customerMapper.selectList(new LambdaQueryWrapper<Customer>().in(Customer::getId, ids))) {
            map.put(c.getId(), c.getName());
        }
        return map;
    }

    private Map<String, Object> toMap(Project p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("projectNo", p.getProjectNo());
        m.put("name", p.getName());
        m.put("customerId", p.getCustomerId());
        m.put("projectType", p.getProjectType());
        m.put("status", p.getStatus());
        m.put("health", p.getHealth());
        m.put("manager", p.getManager());
        m.put("deliveryOwner", p.getDeliveryOwner());
        m.put("contractNo", p.getContractNo());
        m.put("amount", p.getAmount());
        m.put("tags", p.getTags());
        m.put("progress", p.getProgress());
        m.put("docRate", p.getDocRate());
        m.put("archived", p.getArchived());
        m.put("locked", p.getLocked());
        m.put("planAcceptDate", p.getPlanAcceptDate());
        m.put("actualAcceptDate", p.getActualAcceptDate());
        m.put("warrantyEndDate", p.getWarrantyEndDate());
        m.put("createTime", p.getCreateTime());
        m.put("updateTime", p.getUpdateTime());
        return m;
    }

}
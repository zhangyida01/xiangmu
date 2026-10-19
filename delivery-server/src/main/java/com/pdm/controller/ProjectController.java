package com.pdm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pdm.common.ApiResult;
import com.pdm.common.Audit;
import com.pdm.common.BizException;
import com.pdm.common.PageResult;
import com.pdm.common.Perm;
import com.pdm.common.RequiresPerm;
import com.pdm.common.Role;
import com.pdm.entity.ChecklistItem;
import com.pdm.entity.ChecklistTemplate;
import com.pdm.entity.Project;
import com.pdm.entity.ProjectEnv;
import com.pdm.entity.ProjectMember;
import com.pdm.entity.ProjectProduct;
import com.pdm.mapper.ProjectEnvMapper;
import com.pdm.mapper.ProjectMemberMapper;
import com.pdm.mapper.ProjectProductMapper;
import com.pdm.service.ChecklistService;
import com.pdm.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final ChecklistService checklistService;
    private final ProjectMemberMapper memberMapper;
    private final ProjectEnvMapper envMapper;
    private final ProjectProductMapper productMapper;

    public record StatusBody(String status, String reason) {
    }

    public record CopyBody(String projectNo, String name, Boolean copyEnv) {
    }

    @GetMapping("/projects")
    public ApiResult<PageResult<Map<String, Object>>> page(@RequestParam(defaultValue = "1") long page,
                                                          @RequestParam(defaultValue = "20") long size,
                                                          @RequestParam(required = false) String keyword,
                                                          @RequestParam(required = false) String status,
                                                          @RequestParam(required = false) String projectType,
                                                          @RequestParam(required = false) Long customerId,
                                                          @RequestParam(required = false) String manager,
                                                          @RequestParam(required = false) Boolean archived,
                                                          @RequestParam(required = false) String orderBy,
                                                          @RequestParam(defaultValue = "false") boolean asc) {
        return ApiResult.ok(projectService.page(page, size, keyword, status, projectType, customerId,
                manager, archived, orderBy, asc));
    }

    @GetMapping("/projects/all")
    public ApiResult<List<Map<String, Object>>> all() {
        return ApiResult.ok(projectService.all());
    }

    @GetMapping("/projects/{id}")
    public ApiResult<Project> get(@PathVariable Long id) {
        return ApiResult.ok(projectService.get(id));
    }

    @GetMapping("/projects/{id}/overview")
    public ApiResult<Map<String, Object>> overview(@PathVariable Long id) {
        return ApiResult.ok(projectService.overview(id));
    }

    @PostMapping("/projects")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Long> create(@Valid @RequestBody Project p,
                                  @RequestParam(defaultValue = "true") boolean withChecklist) {
        Long id = projectService.create(p);
        if (withChecklist) {
            checklistService.generate(id, p.getProjectType());
        }
        return ApiResult.ok(id);
    }

    @PutMapping("/projects/{id}")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody Project p) {
        projectService.update(id, p);
        return ApiResult.ok();
    }

    @DeleteMapping("/projects/{id}")
    @RequiresPerm(perms = {Perm.PROJECT_DELETE})
    public ApiResult<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ApiResult.ok();
    }

    @PutMapping("/projects/{id}/status")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Void> changeStatus(@PathVariable Long id, @RequestBody StatusBody body) {
        if (body.status() == null || body.status().isBlank()) {
            throw BizException.badParam("状态不能为空");
        }
        projectService.changeStatus(id, body.status(), body.reason());
        return ApiResult.ok();
    }

    @PutMapping("/projects/{id}/archive")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Void> archive(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean archived) {
        projectService.archive(id, archived);
        return ApiResult.ok();
    }

    @PutMapping("/projects/{id}/lock")
    @RequiresPerm(roles = {Role.ADMIN})
    public ApiResult<Void> lock(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean locked) {
        projectService.lock(id, locked);
        return ApiResult.ok();
    }

    @PostMapping("/projects/{id}/copy")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Long> copy(@PathVariable Long id, @RequestBody CopyBody body) {
        if (body.projectNo() == null || body.projectNo().isBlank()) {
            throw BizException.badParam("新项目编号不能为空");
        }
        return ApiResult.ok(projectService.copy(id, body.projectNo(),
                body.name() == null ? body.projectNo() : body.name(),
                body.copyEnv() == null || body.copyEnv()));
    }

    @GetMapping("/projects/{id}/members")
    public ApiResult<List<ProjectMember>> members(@PathVariable Long id) {
        return ApiResult.ok(memberMapper.selectList(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, id)
                .orderByDesc(ProjectMember::getIsMain).orderByAsc(ProjectMember::getId)));
    }

    @PostMapping("/projects/{id}/members")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    @Audit(module = "PROJECT", action = "ADD_MEMBER", targetType = "project_member")
    public ApiResult<Long> addMember(@PathVariable Long id, @Valid @RequestBody ProjectMember m) {
        editable(id);
        m.setId(null);
        m.setProjectId(id);
        memberMapper.insert(m);
        return ApiResult.ok(m.getId());
    }

    @PutMapping("/projects/{id}/members/{mid}")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Void> updateMember(@PathVariable Long id, @PathVariable Long mid, @RequestBody ProjectMember m) {
        editable(id);
        m.setId(mid);
        m.setProjectId(id);
        memberMapper.updateById(m);
        return ApiResult.ok();
    }

    @DeleteMapping("/projects/{id}/members/{mid}")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Void> deleteMember(@PathVariable Long id, @PathVariable Long mid) {
        editable(id);
        memberMapper.deleteById(mid);
        return ApiResult.ok();
    }

    @GetMapping("/projects/{id}/envs")
    public ApiResult<List<ProjectEnv>> envs(@PathVariable Long id) {
        return ApiResult.ok(envMapper.selectList(new LambdaQueryWrapper<ProjectEnv>()
                .eq(ProjectEnv::getProjectId, id).orderByAsc(ProjectEnv::getId)));
    }

    @PostMapping("/projects/{id}/envs")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    @Audit(module = "PROJECT", action = "ADD_ENV", targetType = "project_env")
    public ApiResult<Long> addEnv(@PathVariable Long id, @Valid @RequestBody ProjectEnv e) {
        editable(id);
        e.setId(null);
        e.setProjectId(id);
        envMapper.insert(e);
        return ApiResult.ok(e.getId());
    }

    @PutMapping("/projects/{id}/envs/{eid}")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Void> updateEnv(@PathVariable Long id, @PathVariable Long eid, @RequestBody ProjectEnv e) {
        editable(id);
        e.setId(eid);
        e.setProjectId(id);
        envMapper.updateById(e);
        return ApiResult.ok();
    }

    @DeleteMapping("/projects/{id}/envs/{eid}")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Void> deleteEnv(@PathVariable Long id, @PathVariable Long eid) {
        editable(id);
        envMapper.deleteById(eid);
        return ApiResult.ok();
    }

    @GetMapping("/projects/{id}/products")
    public ApiResult<List<ProjectProduct>> products(@PathVariable Long id) {
        return ApiResult.ok(productMapper.selectList(new LambdaQueryWrapper<ProjectProduct>()
                .eq(ProjectProduct::getProjectId, id).orderByAsc(ProjectProduct::getId)));
    }

    @PostMapping("/projects/{id}/products")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Long> addProduct(@PathVariable Long id, @Valid @RequestBody ProjectProduct pr) {
        editable(id);
        pr.setId(null);
        pr.setProjectId(id);
        productMapper.insert(pr);
        return ApiResult.ok(pr.getId());
    }

    @PutMapping("/projects/{id}/products/{pid}")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Void> updateProduct(@PathVariable Long id, @PathVariable Long pid, @RequestBody ProjectProduct pr) {
        editable(id);
        pr.setId(pid);
        pr.setProjectId(id);
        productMapper.updateById(pr);
        return ApiResult.ok();
    }

    @DeleteMapping("/projects/{id}/products/{pid}")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Void> deleteProduct(@PathVariable Long id, @PathVariable Long pid) {
        editable(id);
        productMapper.deleteById(pid);
        return ApiResult.ok();
    }

    @GetMapping("/projects/{id}/checklist")
    public ApiResult<Map<String, Object>> checklist(@PathVariable Long id) {
        return ApiResult.ok(checklistService.progress(id));
    }

    @PostMapping("/projects/{id}/checklist/generate")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Integer> generateChecklist(@PathVariable Long id,
                                                @RequestParam(required = false) String projectType) {
        return ApiResult.ok("已生成", checklistService.generate(id, projectType));
    }

    @PostMapping("/projects/{id}/checklist")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Long> addChecklistItem(@PathVariable Long id, @RequestBody ChecklistItem item) {
        editable(id);
        return ApiResult.ok(checklistService.addItem(id, item));
    }

    @PutMapping("/checklist/{itemId}")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Void> updateChecklistItem(@PathVariable Long itemId, @RequestBody ChecklistItem item) {
        checklistService.updateItem(itemId, item);
        return ApiResult.ok();
    }

    @DeleteMapping("/checklist/{itemId}")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Void> deleteChecklistItem(@PathVariable Long itemId) {
        checklistService.deleteItem(itemId);
        return ApiResult.ok();
    }

    @GetMapping("/checklist-templates")
    public ApiResult<List<ChecklistTemplate>> templates(@RequestParam(required = false) String projectType) {
        return ApiResult.ok(projectType == null ? checklistService.allTemplates() : checklistService.templates(projectType));
    }

    @PostMapping("/checklist-templates")
    @RequiresPerm(roles = {Role.ADMIN})
    public ApiResult<Long> createTemplate(@RequestBody ChecklistTemplate t) {
        return ApiResult.ok(checklistService.createTemplate(t));
    }

    @PutMapping("/checklist-templates/{id}")
    @RequiresPerm(roles = {Role.ADMIN})
    public ApiResult<Void> updateTemplate(@PathVariable Long id, @RequestBody ChecklistTemplate t) {
        checklistService.updateTemplate(id, t);
        return ApiResult.ok();
    }

    @DeleteMapping("/checklist-templates/{id}")
    @RequiresPerm(roles = {Role.ADMIN})
    public ApiResult<Void> deleteTemplate(@PathVariable Long id) {
        checklistService.deleteTemplate(id);
        return ApiResult.ok();
    }

    private void editable(Long projectId) {
        projectService.requireEditable(projectService.get(projectId));
    }
}
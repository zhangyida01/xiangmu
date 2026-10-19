package com.pdm.controller;

import com.pdm.common.ApiResult;
import com.pdm.common.FileResponses;
import com.pdm.common.PageResult;
import com.pdm.common.Perm;
import com.pdm.common.RequiresPerm;
import com.pdm.common.Role;
import com.pdm.entity.DocCategory;
import com.pdm.entity.Document;
import com.pdm.entity.DocumentVersion;
import com.pdm.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping("/doc-categories/tree")
    public ApiResult<List<Map<String, Object>>> categoryTree() {
        return ApiResult.ok(documentService.categoryTree());
    }

    @PostMapping("/doc-categories")
    @RequiresPerm(roles = {Role.ADMIN})
    public ApiResult<Long> createCategory(@Valid @RequestBody DocCategory c) {
        return ApiResult.ok(documentService.createCategory(c));
    }

    @PutMapping("/doc-categories/{id}")
    @RequiresPerm(roles = {Role.ADMIN})
    public ApiResult<Void> updateCategory(@PathVariable Long id, @RequestBody DocCategory c) {
        documentService.updateCategory(id, c);
        return ApiResult.ok();
    }

    @DeleteMapping("/doc-categories/{id}")
    @RequiresPerm(roles = {Role.ADMIN})
    public ApiResult<Void> deleteCategory(@PathVariable Long id) {
        documentService.deleteCategory(id);
        return ApiResult.ok();
    }

    @GetMapping("/documents")
    public ApiResult<PageResult<Document>> page(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "20") long size,
                                                @RequestParam(required = false) Long projectId,
                                                @RequestParam(required = false) Long categoryId,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String secretLevel) {
        return ApiResult.ok(documentService.page(page, size, projectId, categoryId, keyword, secretLevel));
    }

    @GetMapping("/documents/{id}")
    public ApiResult<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResult.ok(documentService.detail(id));
    }

    @PostMapping("/documents")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Long> create(@Valid @RequestBody Document d) {
        return ApiResult.ok(documentService.create(d));
    }

    @PutMapping("/documents/{id}")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody Document d) {
        documentService.update(id, d);
        return ApiResult.ok();
    }

    @DeleteMapping("/documents/{id}")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return ApiResult.ok();
    }

    @PostMapping("/documents/{id}/versions")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<DocumentVersion> upload(@PathVariable Long id,
                                             @RequestParam("file") MultipartFile file,
                                             @RequestParam(required = false) String description,
                                             @RequestParam(required = false) String versionNo) {
        return ApiResult.ok("上传成功", documentService.uploadVersion(id, file, description, versionNo));
    }

    @GetMapping("/documents/{id}/versions")
    public ApiResult<List<DocumentVersion>> versions(@PathVariable Long id) {
        return ApiResult.ok(documentService.versions(id));
    }

    @PutMapping("/documents/{id}/rollback/{versionId}")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<DocumentVersion> rollback(@PathVariable Long id, @PathVariable Long versionId) {
        return ApiResult.ok("已回滚", documentService.rollback(id, versionId));
    }

    @DeleteMapping("/documents/versions/{versionId}")
    @RequiresPerm(roles = {Role.ADMIN})
    public ApiResult<Void> deleteVersion(@PathVariable Long versionId) {
        documentService.deleteVersion(versionId);
        return ApiResult.ok();
    }

    @GetMapping("/files/download/{versionId}")
    @RequiresPerm(perms = {Perm.FILE_DOWNLOAD})
    public ResponseEntity<byte[]> download(@PathVariable Long versionId) {
        DocumentService.DownloadFile f = documentService.load(versionId, true);
        return FileResponses.of(f, true);
    }

    @GetMapping("/files/preview/{versionId}")
    public ResponseEntity<byte[]> preview(@PathVariable Long versionId) {
        DocumentService.DownloadFile f = documentService.load(versionId, false);
        return FileResponses.of(f, false);
    }
}
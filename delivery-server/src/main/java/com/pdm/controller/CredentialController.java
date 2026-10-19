package com.pdm.controller;

import com.pdm.common.ApiResult;
import com.pdm.common.Audit;
import com.pdm.common.FileResponses;
import com.pdm.common.PageResult;
import com.pdm.common.Perm;
import com.pdm.common.RequiresPerm;
import com.pdm.common.Role;
import com.pdm.dto.CredentialForm;
import com.pdm.dto.CredentialVO;
import com.pdm.dto.RevealResponse;
import com.pdm.entity.CredentialAccess;
import com.pdm.entity.CredentialHistory;
import com.pdm.service.CredentialService;
import com.pdm.service.DocumentService;
import jakarta.servlet.http.HttpServletRequest;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/credentials")
@RequiredArgsConstructor
public class CredentialController {

    private final CredentialService credentialService;

    public record RevealBody(String recheckToken) {
    }

    public record BatchOwnerBody(List<Long> ids, String owner, Boolean needChange) {
    }

    @GetMapping
    public ApiResult<PageResult<CredentialVO>> page(@RequestParam(defaultValue = "1") long page,
                                                    @RequestParam(defaultValue = "20") long size,
                                                    @RequestParam(required = false) Long projectId,
                                                    @RequestParam(required = false) String credType,
                                                    @RequestParam(required = false) String envType,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) Boolean favorite) {
        return ApiResult.ok(credentialService.page(page, size, projectId, credType, envType, keyword, favorite));
    }

    @GetMapping("/expiring")
    public ApiResult<List<CredentialVO>> expiring(@RequestParam(required = false) Integer days) {
        return ApiResult.ok(credentialService.expiring(days == null ? 30 : days));
    }

    @GetMapping("/generate-password")
    public ApiResult<Map<String, String>> generatePassword(@RequestParam(defaultValue = "16") int length) {
        return ApiResult.ok(Map.of("password", credentialService.generatePassword(length)));
    }

    @GetMapping("/{id}")
    public ApiResult<CredentialVO> get(@PathVariable Long id) {
        return ApiResult.ok(credentialService.detail(id));
    }

    @PostMapping
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Long> create(@Valid @RequestBody CredentialForm form) {
        return ApiResult.ok(credentialService.create(form));
    }

    @PutMapping("/{id}")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    @Audit(module = "CREDENTIAL", action = "UPDATE_FORM", targetType = "credential", recordArgs = false)
    public ApiResult<Void> update(@PathVariable Long id, @Valid @RequestBody CredentialForm form) {
        credentialService.update(id, form);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Void> delete(@PathVariable Long id) {
        credentialService.delete(id);
        return ApiResult.ok();
    }

    /** 查看明文：需要 secret:view 能力，可能要求二次密码校验，全程审计 */
    @PostMapping("/{id}/reveal")
    @RequiresPerm(perms = {Perm.SECRET_VIEW})
    public ApiResult<RevealResponse> reveal(@PathVariable Long id,
                                            @RequestBody(required = false) RevealBody body,
                                            HttpServletRequest request) {
        return ApiResult.ok(credentialService.reveal(id, body == null ? null : body.recheckToken(), request));
    }

    @PostMapping("/{id}/copy-log")
    public ApiResult<Void> copyLog(@PathVariable Long id, HttpServletRequest request) {
        credentialService.logCopy(id, request);
        return ApiResult.ok();
    }

    @PostMapping("/{id}/rotate")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Void> rotate(@PathVariable Long id, @RequestParam(required = false) String date) {
        credentialService.markRotated(id, date == null || date.isBlank() ? null : LocalDate.parse(date));
        return ApiResult.ok();
    }

    @PutMapping("/{id}/favorite")
    public ApiResult<Void> favorite(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean favorite) {
        credentialService.toggleFavorite(id, favorite);
        return ApiResult.ok();
    }

    @PutMapping("/batch-owner")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Integer> batchOwner(@RequestBody BatchOwnerBody body) {
        if (body.ids() == null || body.ids().isEmpty()) {
            return ApiResult.ok(0);
        }
        return ApiResult.ok("已处理", credentialService.batchOwner(body.ids(), body.owner(),
                body.needChange() != null && body.needChange()));
    }

    @GetMapping("/{id}/history")
    public ApiResult<List<CredentialHistory>> history(@PathVariable Long id) {
        return ApiResult.ok(credentialService.history(id));
    }

    @GetMapping("/{id}/access-logs")
    @RequiresPerm(perms = {Perm.AUDIT_VIEW})
    public ApiResult<List<CredentialAccess>> accessLogs(@PathVariable Long id) {
        return ApiResult.ok(credentialService.accessLogs(id));
    }

    @PostMapping("/{id}/attachment")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<String> uploadAttachment(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return ApiResult.ok("上传成功", credentialService.uploadAttachment(id, file));
    }

    @GetMapping("/{id}/attachment")
    @RequiresPerm(perms = {Perm.SECRET_VIEW})
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long id, HttpServletRequest request) {
        DocumentService.DownloadFile f = credentialService.downloadAttachment(id, request);
        return FileResponses.of(f, true);
    }

    @DeleteMapping("/{id}/attachment")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    public ApiResult<Void> deleteAttachment(@PathVariable Long id) {
        credentialService.deleteAttachment(id);
        return ApiResult.ok();
    }
}
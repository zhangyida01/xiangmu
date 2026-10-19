package com.pdm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pdm.common.BizException;
import com.pdm.common.ErrorCode;
import com.pdm.common.LoginUser;
import com.pdm.common.PageResult;
import com.pdm.common.Perm;
import com.pdm.common.UserContext;
import com.pdm.common.WebUtils;
import com.pdm.crypto.CryptoService;
import com.pdm.crypto.MaskUtil;
import com.pdm.dto.CredentialForm;
import com.pdm.dto.CredentialVO;
import com.pdm.dto.RevealResponse;
import com.pdm.entity.Credential;
import com.pdm.entity.CredentialAccess;
import com.pdm.entity.CredentialHistory;
import com.pdm.entity.Project;
import com.pdm.mapper.CredentialAccessMapper;
import com.pdm.mapper.CredentialHistoryMapper;
import com.pdm.mapper.CredentialMapper;
import com.pdm.mapper.ProjectMapper;
import com.pdm.storage.FileStorage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CredentialService {

    private static final String PWD_POOL = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%^&*";
    private static final SecureRandom RND = new SecureRandom();

    private final CredentialMapper credentialMapper;
    private final CredentialHistoryMapper historyMapper;
    private final CredentialAccessMapper accessMapper;
    private final ProjectMapper projectMapper;
    private final CryptoService cryptoService;
    private final FileStorage fileStorage;
    private final ConfigService configService;
    private final AuditService auditService;
    private final AuthService authService;

    public PageResult<CredentialVO> page(long page, long size, Long projectId, String credType, String envType,
                                         String keyword, Boolean favorite) {
        LambdaQueryWrapper<Credential> w = new LambdaQueryWrapper<>();
        w.eq(projectId != null, Credential::getProjectId, projectId)
                .eq(credType != null && !credType.isBlank(), Credential::getCredType, credType)
                .eq(envType != null && !envType.isBlank(), Credential::getEnvType, envType)
                .eq(favorite != null && favorite, Credential::getFavorite, true)
                .and(keyword != null && !keyword.isBlank(), q -> q
                        .like(Credential::getPlatformName, keyword).or()
                        .like(Credential::getUsername, keyword).or()
                        .like(Credential::getHost, keyword).or()
                        .like(Credential::getUrl, keyword).or()
                        .like(Credential::getOwner, keyword))
                .orderByDesc(Credential::getFavorite).orderByDesc(Credential::getId);
        Page<Credential> p = credentialMapper.selectPage(new Page<>(page, size), w);
        Map<Long, String> names = projectNames(p.getRecords());
        LocalDate today = LocalDate.now();
        return PageResult.of(p, c -> toVO(c, names.get(c.getProjectId()), today));
    }

    public List<CredentialVO> list(Long projectId) {
        List<Credential> list = credentialMapper.selectList(new LambdaQueryWrapper<Credential>()
                .eq(Credential::getProjectId, projectId)
                .orderByDesc(Credential::getFavorite).orderByAsc(Credential::getPlatformName));
        Map<Long, String> names = projectNames(list);
        LocalDate today = LocalDate.now();
        return list.stream().map(c -> toVO(c, names.get(c.getProjectId()), today)).toList();
    }

    public CredentialVO detail(Long id) {
        Credential c = require(id);
        Project p = c.getProjectId() == null ? null : projectMapper.selectById(c.getProjectId());
        return toVO(c, p == null ? null : p.getName(), LocalDate.now());
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(CredentialForm form) {
        LoginUser u = UserContext.require();
        requireEditableProject(form.getProjectId());
        Credential c = new Credential();
        copyForm(form, c);
        if (form.getSecret() != null && !form.getSecret().isEmpty()) {
            c.setSecretCipher(cryptoService.encrypt(form.getSecret()));
            c.setStrength(cryptoService.strengthLevel(form.getSecret()));
            c.setSecretHint("长度 " + form.getSecret().length());
        }
        if (c.getRotateDays() == null) {
            c.setRotateDays(configService.defaultRotateDays());
        }
        if (c.getLastRotateDate() == null && c.getSecretCipher() != null) {
            c.setLastRotateDate(LocalDate.now());
        }
        recomputeNextRotate(c);
        c.setFavorite(form.getFavorite() != null && form.getFavorite());
        c.setNeedChange(form.getNeedChange() != null && form.getNeedChange());
        credentialMapper.insert(c);
        writeHistory(c.getId(), "CREATE", "secret", null, c.getSecretCipher(), u, "新建凭据");
        auditService.record("CREDENTIAL", "CREATE", "credential", c.getId(), c.getPlatformName(), c.getProjectId(),
                "type=" + c.getCredType() + ", username=" + MaskUtil.maskAccount(c.getUsername()), true, null);
        return c.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, CredentialForm form) {
        LoginUser u = UserContext.require();
        Credential c = require(id);
        requireEditableProject(c.getProjectId());
        String oldCipher = c.getSecretCipher();
        copyForm(form, c);
        boolean rotated = false;
        if (form.getSecret() != null && !form.getSecret().isEmpty()) {
            c.setSecretCipher(cryptoService.encrypt(form.getSecret()));
            c.setStrength(cryptoService.strengthLevel(form.getSecret()));
            c.setSecretHint("长度 " + form.getSecret().length());
            if (form.getLastRotateDate() == null) {
                c.setLastRotateDate(LocalDate.now());
            }
            rotated = true;
        }
        recomputeNextRotate(c);
        credentialMapper.updateById(c);
        if (rotated) {
            writeHistory(id, "ROTATE", "secret", oldCipher, c.getSecretCipher(), u, "更新密文");
        } else {
            writeHistory(id, "UPDATE", null, null, null, u, "修改凭据信息");
        }
        auditService.record("CREDENTIAL", rotated ? "ROTATE" : "UPDATE", "credential", id, c.getPlatformName(),
                c.getProjectId(), "username=" + MaskUtil.maskAccount(c.getUsername()), true, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        LoginUser u = UserContext.require();
        Credential c = require(id);
        credentialMapper.deleteById(id);
        writeHistory(id, "DELETE", null, null, c.getSecretCipher(), u, "删除凭据");
        auditService.record("CREDENTIAL", "DELETE", "credential", id, c.getPlatformName(), c.getProjectId(), null, true, null);
    }

    /** 查看明文：校验能力与二次确认，写访问记录与审计 */
    public RevealResponse reveal(Long id, String recheckToken, HttpServletRequest http) {
        LoginUser u = UserContext.require();
        Credential c = require(id);
        if (!u.hasPerm(Perm.SECRET_VIEW)) {
            recordAccess(c, u, "VIEW", http, false, false);
            auditService.record("CREDENTIAL", "VIEW_SECRET", "credential", id, c.getPlatformName(), c.getProjectId(),
                    "无权限尝试查看明文", false, "forbidden");
            throw BizException.forbidden("无查看凭据明文的权限");
        }
        boolean recheckUsed = false;
        if (configService.getBool("cred.recheck.enabled", false) || u.isNeedRecheck()) {
            if (!authService.verifyRecheckToken(recheckToken)) {
                recordAccess(c, u, "VIEW", http, false, true);
                auditService.record("CREDENTIAL", "VIEW_SECRET", "credential", id, c.getPlatformName(), c.getProjectId(),
                        "二次密码校验未通过", false, "recheck failed");
                throw new BizException(ErrorCode.RECHECK_FAILED, "需要二次密码校验");
            }
            recheckUsed = true;
        }
        String plain = c.getSecretCipher() == null ? null : cryptoService.decrypt(c.getSecretCipher());
        Long accessId = recordAccess(c, u, "VIEW", http, true, recheckUsed);
        auditService.record("CREDENTIAL", "VIEW_SECRET", "credential", id, c.getPlatformName(), c.getProjectId(),
                "username=" + MaskUtil.maskAccount(c.getUsername()), true, null);

        RevealResponse r = new RevealResponse();
        r.setId(id);
        r.setUsername(c.getUsername());
        r.setSecret(plain);
        r.setSecretType(c.getSecretType());
        r.setAutoHideSec(configService.plainHideSec());
        r.setClipboardClearSec(configService.clipboardClearSec());
        r.setAccessId(accessId);
        return r;
    }

    public void logCopy(Long id, HttpServletRequest http) {
        LoginUser u = UserContext.require();
        Credential c = require(id);
        recordAccess(c, u, "COPY", http, true, false);
        auditService.record("CREDENTIAL", "COPY", "credential", id, c.getPlatformName(), c.getProjectId(), null, true, null);
    }

    public void markRotated(Long id, LocalDate date) {
        Credential c = require(id);
        c.setLastRotateDate(date == null ? LocalDate.now() : date);
        recomputeNextRotate(c);
        c.setNeedChange(false);
        credentialMapper.updateById(c);
        writeHistory(id, "ROTATE", "lastRotateDate", null, null, UserContext.require(), "标记已改密");
        auditService.record("CREDENTIAL", "ROTATE", "credential", id, c.getPlatformName(), c.getProjectId(),
                "lastRotateDate=" + c.getLastRotateDate(), true, null);
    }

    public void toggleFavorite(Long id, boolean favorite) {
        Credential c = require(id);
        c.setFavorite(favorite);
        credentialMapper.updateById(c);
    }

    public int batchOwner(List<Long> ids, String owner, boolean needChange) {
        int n = 0;
        for (Long id : ids) {
            Credential c = credentialMapper.selectById(id);
            if (c == null) {
                continue;
            }
            c.setOwner(owner);
            c.setNeedChange(needChange);
            credentialMapper.updateById(c);
            n++;
        }
        auditService.record("CREDENTIAL", "BATCH_OWNER", "credential", null, owner, null,
                "转移 " + n + " 条凭据，needChange=" + needChange, true, null);
        return n;
    }

    public List<CredentialHistory> history(Long id) {
        require(id);
        return historyMapper.selectList(new LambdaQueryWrapper<CredentialHistory>()
                .eq(CredentialHistory::getCredentialId, id)
                .orderByDesc(CredentialHistory::getId));
    }

    public List<CredentialAccess> accessLogs(Long id) {
        return accessMapper.selectList(new LambdaQueryWrapper<CredentialAccess>()
                .eq(CredentialAccess::getCredentialId, id)
                .orderByDesc(CredentialAccess::getId).last("LIMIT 200"));
    }

    @Transactional(rollbackFor = Exception.class)
    public String uploadAttachment(Long id, MultipartFile file) {
        Credential c = require(id);
        requireEditableProject(c.getProjectId());
        if (file == null || file.isEmpty()) {
            throw BizException.badParam("上传文件不能为空");
        }
        long maxBytes = configService.credAttachmentMaxBytes();
        if (maxBytes > 0 && file.getSize() > maxBytes) {
            throw BizException.badParam("凭据附件超过上限 " + (maxBytes / 1024 / 1024)
                    + "MB（附件需整体加密后落盘，更大的文件请走「交付文档」上传）");
        }
        String name = file.getOriginalFilename() == null ? "attachment" : file.getOriginalFilename();
        String relPath = c.getProjectId() + "/secret/" + UUID.randomUUID().toString().replace("-", "") + ".enc";
        try {
            byte[] enc = cryptoService.encryptBytes(file.getBytes());
            fileStorage.write(relPath, enc);
        } catch (IOException e) {
            throw new BizException("读取上传文件失败：" + e.getMessage());
        }
        if (c.getAttachmentPath() != null) {
            fileStorage.delete(c.getAttachmentPath());
        }
        c.setAttachmentPath(relPath);
        c.setAttachmentName(name);
        credentialMapper.updateById(c);
        auditService.record("CREDENTIAL", "UPLOAD_ATTACHMENT", "credential", id, c.getPlatformName(),
                c.getProjectId(), "file=" + name, true, null);
        return name;
    }

    public DocumentService.DownloadFile downloadAttachment(Long id, HttpServletRequest http) {
        LoginUser u = UserContext.require();
        Credential c = require(id);
        if (!u.hasPerm(Perm.SECRET_VIEW)) {
            recordAccess(c, u, "ATTACHMENT", http, false, false);
            throw BizException.forbidden("无查看凭据附件的权限");
        }
        if (c.getAttachmentPath() == null) {
            throw BizException.notFound("附件");
        }
        byte[] plain = cryptoService.decryptBytes(fileStorage.read(c.getAttachmentPath()));
        recordAccess(c, u, "ATTACHMENT", http, true, false);
        auditService.record("CREDENTIAL", "DOWNLOAD_ATTACHMENT", "credential", id, c.getPlatformName(),
                c.getProjectId(), "file=" + c.getAttachmentName(), true, null);
        return new DocumentService.DownloadFile(plain, c.getAttachmentName(), plain.length, "application/octet-stream");
    }

    public void deleteAttachment(Long id) {
        Credential c = require(id);
        if (c.getAttachmentPath() != null) {
            fileStorage.delete(c.getAttachmentPath());
        }
        credentialMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Credential>()
                .eq(Credential::getId, id)
                .set(Credential::getAttachmentPath, null)
                .set(Credential::getAttachmentName, null));
        auditService.record("CREDENTIAL", "DELETE_ATTACHMENT", "credential", id, c.getPlatformName(),
                c.getProjectId(), null, true, null);
    }

    public List<CredentialVO> expiring(int days) {
        LocalDate limit = LocalDate.now().plusDays(days);
        List<Credential> list = credentialMapper.selectList(new LambdaQueryWrapper<Credential>()
                .and(q -> q.isNotNull(Credential::getExpireDate).le(Credential::getExpireDate, limit)
                        .or(o -> o.isNotNull(Credential::getNextRotateDate).le(Credential::getNextRotateDate, limit))
                        .or(o -> o.eq(Credential::getNeedChange, true)))
                .orderByAsc(Credential::getExpireDate));
        Map<Long, String> names = projectNames(list);
        LocalDate today = LocalDate.now();
        return list.stream().map(c -> toVO(c, names.get(c.getProjectId()), today)).toList();
    }

    public String generatePassword(int length) {
        int len = Math.max(8, Math.min(length, 64));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(PWD_POOL.charAt(RND.nextInt(PWD_POOL.length())));
        }
        return sb.toString();
    }

    public Credential require(Long id) {
        Credential c = credentialMapper.selectById(id);
        if (c == null) {
            throw BizException.notFound("凭据");
        }
        return c;
    }

    private void requireEditableProject(Long projectId) {
        Project p = projectMapper.selectById(projectId);
        if (p == null) {
            throw BizException.notFound("项目");
        }
        if (Boolean.TRUE.equals(p.getLocked())) {
            throw BizException.conflict("项目已锁定，不能修改凭据");
        }
    }

    private void copyForm(CredentialForm f, Credential c) {
        c.setProjectId(f.getProjectId());
        c.setPlatformName(f.getPlatformName());
        c.setCredType(f.getCredType() == null ? "OTHER" : f.getCredType());
        c.setEnvType(f.getEnvType() == null ? "PROD" : f.getEnvType());
        c.setUrl(f.getUrl());
        c.setHost(f.getHost());
        c.setPort(f.getPort());
        c.setUsername(f.getUsername());
        c.setSecretType(f.getSecretType() == null ? "PASSWORD" : f.getSecretType());
        c.setOwner(f.getOwner());
        c.setExpireDate(f.getExpireDate());
        c.setRotateDays(f.getRotateDays());
        c.setLastRotateDate(f.getLastRotateDate());
        c.setRemark(f.getRemark());
        if (f.getFavorite() != null) {
            c.setFavorite(f.getFavorite());
        }
        if (f.getNeedChange() != null) {
            c.setNeedChange(f.getNeedChange());
        }
    }

    private void recomputeNextRotate(Credential c) {
        if (c.getLastRotateDate() != null && c.getRotateDays() != null && c.getRotateDays() > 0) {
            c.setNextRotateDate(c.getLastRotateDate().plusDays(c.getRotateDays()));
        } else {
            c.setNextRotateDate(null);
        }
    }

    private void writeHistory(Long credentialId, String changeType, String field, String oldCipher,
                              String newCipher, LoginUser u, String remark) {
        CredentialHistory h = new CredentialHistory();
        h.setCredentialId(credentialId);
        h.setChangeType(changeType);
        h.setFieldName(field);
        h.setOldCipher(oldCipher);
        h.setNewCipher(newCipher);
        h.setOperatorId(u.getId());
        h.setOperatorName(u.getRealName() == null ? u.getUsername() : u.getRealName());
        h.setRemark(remark);
        historyMapper.insert(h);
    }

    private Long recordAccess(Credential c, LoginUser u, String action, HttpServletRequest http,
                              boolean success, boolean recheckUsed) {
        try {
            CredentialAccess a = new CredentialAccess();
            a.setCredentialId(c.getId());
            a.setProjectId(c.getProjectId());
            a.setUserId(u.getId());
            a.setUsername(u.getUsername());
            a.setAction(action);
            a.setIp(WebUtils.clientIp(http));
            a.setUserAgent(WebUtils.userAgent(http));
            a.setRecheckUsed(recheckUsed);
            a.setSuccess(success);
            accessMapper.insert(a);
            return a.getId();
        } catch (Exception e) {
            return null;
        }
    }

    private Map<Long, String> projectNames(List<Credential> list) {
        Map<Long, String> map = new HashMap<>();
        List<Long> ids = list.stream().map(Credential::getProjectId).filter(java.util.Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return map;
        }
        for (Project p : projectMapper.selectList(new LambdaQueryWrapper<Project>().in(Project::getId, ids))) {
            map.put(p.getId(), p.getProjectNo() + " " + p.getName());
        }
        return map;
    }

    private CredentialVO toVO(Credential c, String projectName, LocalDate today) {
        CredentialVO v = new CredentialVO();
        v.setId(c.getId());
        v.setProjectId(c.getProjectId());
        v.setProjectName(projectName);
        v.setPlatformName(c.getPlatformName());
        v.setCredType(c.getCredType());
        v.setEnvType(c.getEnvType());
        v.setUrl(c.getUrl());
        v.setHost(c.getHost());
        v.setPort(c.getPort());
        v.setUsername(c.getUsername());
        v.setUsernameMasked(MaskUtil.maskAccount(c.getUsername()));
        v.setSecretType(c.getSecretType());
        v.setHasSecret(c.getSecretCipher() != null && !c.getSecretCipher().isBlank());
        v.setSecretMasked(v.isHasSecret() ? MaskUtil.SECRET_MASK : null);
        v.setSecretHint(c.getSecretHint());
        v.setStrength(c.getStrength());
        v.setHasAttachment(c.getAttachmentPath() != null);
        v.setAttachmentName(c.getAttachmentName());
        v.setOwner(c.getOwner());
        v.setExpireDate(c.getExpireDate());
        v.setRotateDays(c.getRotateDays());
        v.setLastRotateDate(c.getLastRotateDate());
        v.setNextRotateDate(c.getNextRotateDate());
        v.setFavorite(c.getFavorite());
        v.setNeedChange(c.getNeedChange());
        v.setRemark(c.getRemark());
        v.setCreateTime(c.getCreateTime());
        v.setUpdateTime(c.getUpdateTime());
        LocalDate nearest = null;
        if (c.getExpireDate() != null) {
            nearest = c.getExpireDate();
        }
        if (c.getNextRotateDate() != null && (nearest == null || c.getNextRotateDate().isBefore(nearest))) {
            nearest = c.getNextRotateDate();
        }
        if (nearest != null) {
            v.setDaysLeft((int) ChronoUnit.DAYS.between(today, nearest));
        }
        return v;
    }
}
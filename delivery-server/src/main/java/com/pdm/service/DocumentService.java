package com.pdm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pdm.common.BizException;
import com.pdm.common.LoginUser;
import com.pdm.common.PageResult;
import com.pdm.common.UserContext;
import com.pdm.entity.ChecklistItem;
import com.pdm.entity.DocCategory;
import com.pdm.entity.Document;
import com.pdm.entity.DocumentVersion;
import com.pdm.mapper.ChecklistItemMapper;
import com.pdm.mapper.DocCategoryMapper;
import com.pdm.mapper.DocumentMapper;
import com.pdm.mapper.DocumentVersionMapper;
import com.pdm.storage.FileStorage;
import com.pdm.storage.LocalFileStorage;
import com.pdm.storage.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentService {

    public record DownloadFile(byte[] data, String fileName, long size, String contentType) {
    }

    private final DocumentMapper documentMapper;
    private final DocumentVersionMapper versionMapper;
    private final DocCategoryMapper categoryMapper;
    private final ChecklistItemMapper checklistItemMapper;
    private final FileStorage fileStorage;
    private final ConfigService configService;
    private final AuditService auditService;

    public List<Map<String, Object>> categoryTree() {
        List<DocCategory> all = categoryMapper.selectList(new LambdaQueryWrapper<DocCategory>()
                .orderByAsc(DocCategory::getSortNo).orderByAsc(DocCategory::getId));
        List<Map<String, Object>> roots = new ArrayList<>();
        Map<Long, Map<String, Object>> index = new HashMap<>();
        for (DocCategory c : all) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", c.getId());
            node.put("name", c.getName());
            node.put("code", c.getCode());
            node.put("parentId", c.getParentId());
            node.put("children", new ArrayList<Map<String, Object>>());
            index.put(c.getId(), node);
        }
        for (DocCategory c : all) {
            Map<String, Object> node = index.get(c.getId());
            Long pid = c.getParentId();
            if (pid == null || pid == 0 || !index.containsKey(pid)) {
                roots.add(node);
            } else {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> children = (List<Map<String, Object>>) index.get(pid).get("children");
                children.add(node);
            }
        }
        return roots;
    }

    public Long createCategory(DocCategory c) {
        c.setId(null);
        if (c.getParentId() == null) {
            c.setParentId(0L);
        }
        if (c.getEnabled() == null) {
            c.setEnabled(true);
        }
        if (c.getSortNo() == null) {
            c.setSortNo(0);
        }
        categoryMapper.insert(c);
        return c.getId();
    }

    public void updateCategory(Long id, DocCategory c) {
        requireCategory(id);
        c.setId(id);
        categoryMapper.updateById(c);
    }

    public void deleteCategory(Long id) {
        requireCategory(id);
        Long used = documentMapper.selectCount(new LambdaQueryWrapper<Document>().eq(Document::getCategoryId, id));
        if (used != null && used > 0) {
            throw BizException.conflict("该分类下还有 " + used + " 个文档，不能删除");
        }
        Long children = categoryMapper.selectCount(new LambdaQueryWrapper<DocCategory>().eq(DocCategory::getParentId, id));
        if (children != null && children > 0) {
            throw BizException.conflict("请先删除子分类");
        }
        categoryMapper.deleteById(id);
    }

    public PageResult<Document> page(long page, long size, Long projectId, Long categoryId,
                                     String keyword, String secretLevel) {
        LambdaQueryWrapper<Document> w = new LambdaQueryWrapper<>();
        w.eq(projectId != null, Document::getProjectId, projectId)
                .eq(categoryId != null, Document::getCategoryId, categoryId)
                .eq(secretLevel != null && !secretLevel.isBlank(), Document::getSecretLevel, secretLevel)
                .and(keyword != null && !keyword.isBlank(), q -> q
                        .like(Document::getName, keyword).or()
                        .like(Document::getTags, keyword).or()
                        .like(Document::getRemark, keyword))
                .orderByDesc(Document::getId);
        return PageResult.of(documentMapper.selectPage(new Page<>(page, size), w));
    }

    public Map<String, Object> detail(Long id) {
        Document d = requireDoc(id);
        Map<String, Object> m = new HashMap<>();
        m.put("document", d);
        m.put("versions", versions(id));
        return m;
    }

    public List<DocumentVersion> versions(Long documentId) {
        return versionMapper.selectList(new LambdaQueryWrapper<DocumentVersion>()
                .eq(DocumentVersion::getDocumentId, documentId)
                .orderByDesc(DocumentVersion::getId));
    }

    public Long create(Document d) {
        LoginUser u = UserContext.require();
        d.setId(null);
        d.setUploaderId(u.getId());
        d.setUploaderName(u.getRealName() == null ? u.getUsername() : u.getRealName());
        d.setDownloadCount(0);
        d.setViewCount(0);
        if (d.getSecretLevel() == null) {
            d.setSecretLevel("INTERNAL");
        }
        documentMapper.insert(d);
        auditService.record("DOC", "CREATE", "document", d.getId(), d.getName(), d.getProjectId(), "新建文档", true, null);
        return d.getId();
    }

    public void update(Long id, Document form) {
        Document d = requireDoc(id);
        form.setId(id);
        form.setProjectId(d.getProjectId());
        form.setUploaderId(d.getUploaderId());
        form.setUploaderName(d.getUploaderName());
        documentMapper.updateById(form);
        auditService.record("DOC", "UPDATE", "document", id, form.getName(), d.getProjectId(), "修改文档信息", true, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Document d = requireDoc(id);
        documentMapper.deleteById(id);
        versionMapper.delete(new LambdaQueryWrapper<DocumentVersion>().eq(DocumentVersion::getDocumentId, id));
        auditService.record("DOC", "DELETE", "document", id, d.getName(), d.getProjectId(),
                "删除文档（物理文件保留，等待清理任务）", true, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public DocumentVersion uploadVersion(Long documentId, MultipartFile file, String description, String versionNo) {
        Document d = requireDoc(documentId);
        if (file == null || file.isEmpty()) {
            throw BizException.badParam("上传文件不能为空");
        }
        String original = file.getOriginalFilename() == null ? "unnamed" : file.getOriginalFilename();
        String ext = LocalFileStorage.extOf(original);
        List<String> allow = configService.allowedExtensions();
        if (!allow.isEmpty() && !ext.isEmpty() && !allow.contains(ext.toLowerCase(Locale.ROOT))) {
            throw BizException.badParam("不允许上传 ." + ext + " 类型文件");
        }
        long maxBytes = configService.uploadMaxBytes();
        if (maxBytes > 0 && file.getSize() > maxBytes) {
            throw BizException.badParam("文件超过上限 " + (maxBytes / 1024 / 1024) + "MB");
        }
        String no = versionNo == null || versionNo.isBlank() ? nextVersionNo(documentId) : versionNo.trim();
        Long dup = versionMapper.selectCount(new LambdaQueryWrapper<DocumentVersion>()
                .eq(DocumentVersion::getDocumentId, documentId)
                .eq(DocumentVersion::getVersionNo, no));
        if (dup != null && dup > 0) {
            throw BizException.conflict("版本号已存在：" + no);
        }

        StoredFile stored;
        try {
            stored = fileStorage.store(String.valueOf(d.getProjectId()), original, file.getInputStream());
        } catch (IOException e) {
            throw new BizException("读取上传文件失败：" + e.getMessage());
        }

        LoginUser u = UserContext.require();
        DocumentVersion v = new DocumentVersion();
        v.setDocumentId(documentId);
        v.setVersionNo(no);
        v.setFileName(original);
        v.setFilePath(stored.path());
        v.setFileExt(stored.ext());
        v.setFileSize(stored.size());
        v.setMd5(stored.md5());
        v.setDescription(description);
        v.setUploaderId(u.getId());
        v.setUploaderName(u.getRealName() == null ? u.getUsername() : u.getRealName());
        versionMapper.insert(v);

        d.setLatestVersionId(v.getId());
        d.setLatestVersionNo(no);
        if (d.getName() == null || d.getName().isBlank()) {
            d.setName(stripExt(original));
        }
        documentMapper.updateById(d);

        if (d.getChecklistItemId() != null) {
            ChecklistItem item = checklistItemMapper.selectById(d.getChecklistItemId());
            if (item != null && "PENDING".equals(item.getStatus())) {
                item.setStatus("SUBMITTED");
                item.setDocumentId(documentId);
                checklistItemMapper.updateById(item);
            }
        }
        auditService.record("DOC", "UPLOAD", "document_version", v.getId(), d.getName() + " " + no,
                d.getProjectId(), "size=" + stored.size(), true, null);
        return v;
    }

    @Transactional(rollbackFor = Exception.class)
    public DocumentVersion rollback(Long documentId, Long versionId) {
        Document d = requireDoc(documentId);
        DocumentVersion old = versionMapper.selectById(versionId);
        if (old == null || !old.getDocumentId().equals(documentId)) {
            throw BizException.notFound("版本");
        }
        LoginUser u = UserContext.require();
        DocumentVersion v = new DocumentVersion();
        v.setDocumentId(documentId);
        v.setVersionNo(nextVersionNo(documentId));
        v.setFileName(old.getFileName());
        v.setFilePath(old.getFilePath());
        v.setFileExt(old.getFileExt());
        v.setFileSize(old.getFileSize());
        v.setMd5(old.getMd5());
        v.setDescription("回滚自 " + old.getVersionNo());
        v.setUploaderId(u.getId());
        v.setUploaderName(u.getRealName() == null ? u.getUsername() : u.getRealName());
        versionMapper.insert(v);
        d.setLatestVersionId(v.getId());
        d.setLatestVersionNo(v.getVersionNo());
        documentMapper.updateById(d);
        auditService.record("DOC", "ROLLBACK", "document", documentId, d.getName(), d.getProjectId(),
                "回滚到 " + old.getVersionNo(), true, null);
        return v;
    }

    public void deleteVersion(Long versionId) {
        DocumentVersion v = versionMapper.selectById(versionId);
        if (v == null) {
            throw BizException.notFound("版本");
        }
        Document d = requireDoc(v.getDocumentId());
        if (versionId.equals(d.getLatestVersionId())) {
            throw BizException.conflict("当前版本不能删除，请先回滚到其他版本");
        }
        versionMapper.deleteById(versionId);
        auditService.record("DOC", "DELETE_VERSION", "document_version", versionId,
                d.getName() + " " + v.getVersionNo(), d.getProjectId(), null, true, null);
    }

    public DownloadFile load(Long versionId, boolean download) {
        DocumentVersion v = versionMapper.selectById(versionId);
        if (v == null) {
            throw BizException.notFound("文件版本");
        }
        Document d = requireDoc(v.getDocumentId());
        byte[] data = fileStorage.read(v.getFilePath());
        if (download) {
            d.setDownloadCount((d.getDownloadCount() == null ? 0 : d.getDownloadCount()) + 1);
        } else {
            d.setViewCount((d.getViewCount() == null ? 0 : d.getViewCount()) + 1);
        }
        documentMapper.updateById(d);
        auditService.record("DOC", download ? "DOWNLOAD" : "PREVIEW", "document_version", versionId,
                d.getName() + " " + v.getVersionNo(), d.getProjectId(), v.getFileName(), true, null);
        return new DownloadFile(data, v.getFileName(), v.getFileSize() == null ? data.length : v.getFileSize(),
                contentTypeOf(v.getFileExt()));
    }

    public Document requireDoc(Long id) {
        Document d = documentMapper.selectById(id);
        if (d == null) {
            throw BizException.notFound("文档");
        }
        return d;
    }

    private DocCategory requireCategory(Long id) {
        DocCategory c = categoryMapper.selectById(id);
        if (c == null) {
            throw BizException.notFound("文档分类");
        }
        return c;
    }

    public String nextVersionNo(Long documentId) {
        DocumentVersion last = versionMapper.selectOne(new LambdaQueryWrapper<DocumentVersion>()
                .eq(DocumentVersion::getDocumentId, documentId)
                .orderByDesc(DocumentVersion::getId).last("LIMIT 1"));
        if (last == null) {
            return "V1.0";
        }
        String no = last.getVersionNo() == null ? "V1.0" : last.getVersionNo();
        String upper = no.toUpperCase(Locale.ROOT).replaceFirst("^V", "");
        String[] parts = upper.split("\\.");
        try {
            int major = Integer.parseInt(parts[0]);
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            return "V" + major + "." + (minor + 1);
        } catch (NumberFormatException e) {
            return no + "-1";
        }
    }

    public static String stripExt(String name) {
        int i = name.lastIndexOf('.');
        return i > 0 ? name.substring(0, i) : name;
    }

    public static String contentTypeOf(String ext) {
        if (ext == null) {
            return "application/octet-stream";
        }
        return switch (ext.toLowerCase(Locale.ROOT)) {
            case "pdf" -> "application/pdf";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "svg" -> "image/svg+xml";
            case "txt", "log", "csv", "md" -> "text/plain; charset=utf-8";
            case "json" -> "application/json";
            case "html", "htm" -> "text/html; charset=utf-8";
            default -> "application/octet-stream";
        };
    }

}
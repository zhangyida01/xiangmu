package com.pdm.storage;

import com.pdm.common.BizException;
import com.pdm.common.PdmProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Component
public class LocalFileStorage implements FileStorage {

    private static final int MAX_EXT_LEN = 10;

    private final Path root;

    public LocalFileStorage(PdmProperties props) {
        this.root = Path.of(props.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("附件根目录创建失败：" + root, e);
        }
        log.info("附件存储根目录：{}", root);
    }

    @Override
    public StoredFile store(String relativeDir, String originalName, InputStream in) {
        String ext = extOf(originalName);
        LocalDate now = LocalDate.now();
        String fileName = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);
        String relPath = join(relativeDir, now.getYear() + "/" + String.format("%02d", now.getMonthValue()) + "/" + fileName);
        Path target = resolveSafe(relPath);
        try {
            Files.createDirectories(target.getParent());
            Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
            long size = Files.size(tmp);
            String md5 = md5(tmp);
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            return new StoredFile(relPath, originalName, ext, size, md5);
        } catch (IOException e) {
            throw new BizException("文件保存失败：" + e.getMessage());
        }
    }

    @Override
    public byte[] read(String relativePath) {
        Path p = resolveSafe(relativePath);
        if (!Files.exists(p)) {
            throw BizException.notFound("文件");
        }
        try {
            return Files.readAllBytes(p);
        } catch (IOException e) {
            throw new BizException("文件读取失败：" + e.getMessage());
        }
    }

    @Override
    public void write(String relativePath, byte[] data) {
        Path p = resolveSafe(relativePath);
        try {
            Files.createDirectories(p.getParent());
            Files.write(p, data);
        } catch (IOException e) {
            throw new BizException("文件写入失败：" + e.getMessage());
        }
    }

    @Override
    public void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolveSafe(relativePath));
        } catch (IOException e) {
            log.warn("删除文件失败 {} : {}", relativePath, e.getMessage());
        }
    }

    @Override
    public boolean exists(String relativePath) {
        try {
            return Files.exists(resolveSafe(relativePath));
        } catch (BizException e) {
            return false;
        }
    }

    @Override
    public long totalSize() {
        try (Stream<Path> s = Files.walk(root)) {
            return s.filter(Files::isRegularFile).mapToLong(p -> {
                try {
                    return Files.size(p);
                } catch (IOException e) {
                    return 0L;
                }
            }).sum();
        } catch (IOException e) {
            return 0L;
        }
    }

    public Path resolveSafe(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            throw BizException.badParam("文件路径不能为空");
        }
        Path p = root.resolve(relativePath).normalize();
        if (!p.startsWith(root)) {
            throw BizException.badParam("非法的文件路径");
        }
        return p;
    }

    public Path root() {
        return root;
    }

    public static String join(String dir, String name) {
        if (dir == null || dir.isBlank()) {
            return name;
        }
        String d = dir.replaceAll("^/+|/+$", "");
        return d + "/" + name;
    }

    public static String extOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int idx = fileName.lastIndexOf('.');
        if (idx < 0 || idx == fileName.length() - 1) {
            return "";
        }
        String ext = fileName.substring(idx + 1).replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        return ext.length() > MAX_EXT_LEN ? ext.substring(0, MAX_EXT_LEN) : ext;
    }

    private String md5(Path p) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (InputStream in = Files.newInputStream(p)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) {
                    md.update(buf, 0, n);
                }
            }
            return HexFormat.of().formatHex(md.digest());
        } catch (Exception e) {
            throw new IOException("计算MD5失败", e);
        }
    }
}
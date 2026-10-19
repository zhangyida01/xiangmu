package com.pdm.crypto;

import com.pdm.common.PdmProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 凭据字段级加密：AES-256-GCM，存储格式 Base64(IV || ciphertext || tag)。
 * 主密钥来自宿主机文件（容器内只读挂载），不入库、不进镜像。
 * 主密钥丢失将导致历史密文永久无法解密，务必备份。
 */
@Slf4j
@Service
public class CryptoService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;
    private static final SecureRandom RND = new SecureRandom();

    private final PdmProperties props;
    private SecretKeySpec key;

    public CryptoService(PdmProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void init() {
        Path path = Path.of(props.getMasterKeyFile()).toAbsolutePath().normalize();
        try {
            if (!Files.exists(path)) {
                throw new IllegalStateException("主密钥文件不存在：" + path
                        + "，请执行 openssl rand -base64 32 > " + path + " 并 chmod 600");
            }
            String content = Files.readString(path, StandardCharsets.UTF_8).trim();
            byte[] raw = Base64.getDecoder().decode(content);
            if (raw.length != 32) {
                throw new IllegalStateException("主密钥必须是 32 字节（Base64 编码后 44 字符），当前 " + raw.length + " 字节");
            }
            this.key = new SecretKeySpec(raw, "AES");
            log.info("凭据加密主密钥已加载：{}", path);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("主密钥加载失败：" + path + " -> " + e.getMessage(), e);
        }
    }

    public String encrypt(String plain) {
        if (plain == null || plain.isEmpty()) {
            return null;
        }
        return Base64.getEncoder().encodeToString(encryptBytes(plain.getBytes(StandardCharsets.UTF_8)));
    }

    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isBlank()) {
            return null;
        }
        try {
            return new String(decryptBytes(Base64.getDecoder().decode(cipherText)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("密文解密失败，请确认 master.key 是否为加密时使用的密钥", e);
        }
    }

    public byte[] encryptBytes(byte[] data) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            RND.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] enc = cipher.doFinal(data);
            byte[] out = new byte[iv.length + enc.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(enc, 0, out, iv.length, enc.length);
            return out;
        } catch (Exception e) {
            throw new IllegalStateException("加密失败：" + e.getMessage(), e);
        }
    }

    public byte[] decryptBytes(byte[] blob) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, blob, 0, IV_LENGTH));
            return cipher.doFinal(blob, IV_LENGTH, blob.length - IV_LENGTH);
        } catch (Exception e) {
            throw new IllegalStateException("解密失败，请确认 master.key 是否为加密时使用的密钥", e);
        }
    }

    public int strengthScore(String plain) {
        if (plain == null || plain.isEmpty()) {
            return 0;
        }
        int score = 0;
        if (plain.length() >= 8) {
            score++;
        }
        if (plain.length() >= 12) {
            score++;
        }
        if (plain.chars().anyMatch(Character::isLowerCase) && plain.chars().anyMatch(Character::isUpperCase)) {
            score++;
        }
        if (plain.chars().anyMatch(Character::isDigit)) {
            score++;
        }
        if (plain.chars().anyMatch(c -> !Character.isLetterOrDigit(c))) {
            score++;
        }
        return score;
    }

    public String strengthLevel(String plain) {
        int s = strengthScore(plain);
        if (s <= 2) {
            return "WEAK";
        }
        return s <= 4 ? "MEDIUM" : "STRONG";
    }
}
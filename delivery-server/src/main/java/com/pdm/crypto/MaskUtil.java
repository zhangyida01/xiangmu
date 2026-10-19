package com.pdm.crypto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MaskUtil {

    public static final String SECRET_MASK = "••••••••";

    private static final Pattern SENSITIVE_JSON = Pattern.compile(
            "(\"[^\"]*(?:password|passwd|secret|cipher|token|privateKey)[^\"]*\"\\s*:\\s*)\"[^\"]*\"",
            Pattern.CASE_INSENSITIVE);

    private MaskUtil() {
    }

    /** 账号掩码：保留前 2 后 3，如 admin@corp.com -> ad***m.com */
    public static String maskAccount(String account) {
        if (account == null || account.isEmpty()) {
            return account;
        }
        int len = account.length();
        if (len <= 3) {
            return account.charAt(0) + "***";
        }
        if (len <= 6) {
            return account.substring(0, 2) + "***";
        }
        return account.substring(0, 2) + "***" + account.substring(len - 3);
    }

    /** 日志脱敏：把 JSON 里的敏感字段值替换掉 */
    public static String maskJson(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        Matcher m = SENSITIVE_JSON.matcher(json);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(m.group(1) + "\"***\""));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
package com.pdm.security;

import com.pdm.common.BizException;
import com.pdm.common.PdmProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CaptchaService {

    private static final char[] POOL = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private static final String PREFIX = "captcha:";
    private static final SecureRandom RND = new SecureRandom();

    private final TokenStore tokenStore;
    private final PdmProperties props;

    public record Captcha(String captchaId, String svg) {
    }

    public Captcha generate() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            code.append(POOL[RND.nextInt(POOL.length)]);
        }
        String id = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(PREFIX + id, code.toString(), 300);
        return new Captcha(id, render(code.toString()));
    }

    public void verify(String captchaId, String input) {
        if (!props.isCaptchaEnabled()) {
            return;
        }
        if (captchaId == null || captchaId.isBlank()) {
            throw BizException.badParam("请输入验证码");
        }
        String real = tokenStore.take(PREFIX + captchaId);
        if (real == null) {
            throw BizException.badParam("验证码已过期，请刷新");
        }
        if (input == null || !real.equalsIgnoreCase(input.trim())) {
            throw BizException.badParam("验证码错误");
        }
    }

    private String render(String code) {
        StringBuilder sb = new StringBuilder();
        sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"120\" height=\"40\" viewBox=\"0 0 120 40\">");
        sb.append("<rect width=\"120\" height=\"40\" fill=\"#f4f6f9\"/>");
        for (int i = 0; i < 4; i++) {
            sb.append(String.format(
                    "<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"#%06x\" stroke-width=\"1\"/>",
                    RND.nextInt(120), RND.nextInt(40), RND.nextInt(120), RND.nextInt(40), 0x999999 + RND.nextInt(0x444444)));
        }
        for (int i = 0; i < code.length(); i++) {
            int x = 14 + i * 26 + RND.nextInt(5);
            int y = 27 + RND.nextInt(6);
            int rotate = RND.nextInt(40) - 20;
            sb.append(String.format(
                    "<text x=\"%d\" y=\"%d\" font-size=\"24\" font-family=\"Verdana,sans-serif\" font-weight=\"bold\" fill=\"#%06x\" transform=\"rotate(%d %d %d)\">%c</text>",
                    x, y, 0x2233aa + RND.nextInt(0x553322), rotate, x, y, code.charAt(i)));
        }
        for (int i = 0; i < 20; i++) {
            sb.append(String.format("<circle cx=\"%d\" cy=\"%d\" r=\"1\" fill=\"#%06x\"/>",
                    RND.nextInt(120), RND.nextInt(40), 0x888888 + RND.nextInt(0x555555)));
        }
        sb.append("</svg>");
        return sb.toString();
    }
}
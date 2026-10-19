package com.pdm.common;

import com.pdm.service.DocumentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class FileResponses {

    private FileResponses() {
    }

    public static ResponseEntity<byte[]> of(DocumentService.DownloadFile file, boolean attachment) {
        String name = file.fileName() == null ? "file" : file.fileName();
        String encoded = URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                file.contentType() == null ? "application/octet-stream" : file.contentType()));
        headers.setContentLength(file.data().length);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                (attachment ? "attachment" : "inline") + "; filename=\"" + encoded + "\"; filename*=UTF-8''" + encoded);
        headers.setCacheControl("no-store");
        return new ResponseEntity<>(file.data(), headers, HttpStatus.OK);
    }
}
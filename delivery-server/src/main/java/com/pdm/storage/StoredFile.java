package com.pdm.storage;

public record StoredFile(String path, String originalName, String ext, long size, String md5) {
}
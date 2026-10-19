package com.pdm.storage;

import java.io.InputStream;

public interface FileStorage {

    StoredFile store(String relativeDir, String originalName, InputStream in);

    byte[] read(String relativePath);

    void write(String relativePath, byte[] data);

    void delete(String relativePath);

    boolean exists(String relativePath);

    long totalSize();
}
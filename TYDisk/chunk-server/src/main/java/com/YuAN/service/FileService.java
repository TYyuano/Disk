package com.YuAN.service;

import com.YuAN.entity.FileChunkDto;

public interface FileService {
    long write(FileChunkDto fileChunkDto);

    byte[] read(String filename, String extension, Integer chunkNo, String bucketName);
}

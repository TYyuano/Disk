package com.YuAN.service;

import com.YuAN.entity.FileChunkDto;

public interface FileService {
    String write(FileChunkDto fileChunkDto);

    byte[] read(String filename, String extension, Integer chunkNo, String bucketName);
}

package com.YuAN.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String upload(String bucketName, MultipartFile file);
}

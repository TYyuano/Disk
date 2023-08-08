package com.YuAN.service;

import com.YuAN.BO.FileChunkMeta;
import com.YuAN.BO.MetaFile;
import com.YuAN.Dto.FileMeta;
import com.YuAN.Vo.BucketVO;
import com.YuAN.Vo.MetaFileVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    String upload(String bucketName, MultipartFile file);

    MetaFile getMeta(String bucketName, String filename);

    byte[] downloadChunk(FileChunkMeta chunk);

    MetaFileVo meta(FileMeta fileMeta);

    String uploadChunk(String bucketName, String filename, String md5, Integer chunkNo, MultipartFile file);

    List<BucketVO> files();

    void delete(String bucketName, String fileName);
}

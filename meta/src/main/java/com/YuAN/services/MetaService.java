package com.YuAN.services;

import com.YuAN.Do.FileChunkMeta;
import com.YuAN.Do.MetaFile;
import com.YuAN.Vo.BucketVO;
import com.YuAN.dto.CompleteChunkDto;
import com.YuAN.dto.FileMeta;

import java.util.List;

public interface MetaService {
    MetaFile generate(FileMeta fileMeta);

    void completeChunk(CompleteChunkDto completeChunk);

    MetaFile meta(String bucketName, String filename);

    List<BucketVO> files();

    void delete(String bucketName, String fileName);

    List<FileChunkMeta> chunkInfo(String bucketName, String fileName, Integer chunkNo);
}

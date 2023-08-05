package com.YuAN.services;

import com.YuAN.Do.MetaFile;
import com.YuAN.dto.CompleteChunkDto;
import com.YuAN.dto.FileMeta;

public interface MetaService {
    MetaFile generate(FileMeta fileMeta);

    void completeChunk(CompleteChunkDto completeChunk);
}

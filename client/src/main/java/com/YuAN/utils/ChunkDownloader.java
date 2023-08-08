package com.YuAN.utils;

import com.YuAN.BO.FileChunkMeta;

public interface ChunkDownloader {
    byte[] download(FileChunkMeta chunkMeta);
    String schema();
}

package com.YuAN.utils;

import com.YuAN.BO.FileChunkMeta;

public interface ClientUtilChunkAddrBuilder {
    String build(FileChunkMeta chunkMeta);
    String schema();
}

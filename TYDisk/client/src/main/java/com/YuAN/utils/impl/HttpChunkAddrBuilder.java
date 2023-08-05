package com.YuAN.utils.impl;

import com.YuAN.BO.FileChunkMeta;
import com.YuAN.utils.ClientUtilChunkAddrBuilder;
import org.springframework.stereotype.Component;

@Component
public class HttpChunkAddrBuilder implements ClientUtilChunkAddrBuilder {
    @Override
    public String build(FileChunkMeta chunkMeta) {
        return "%s://%s".formatted(chunkMeta.getSchema(),chunkMeta.getAddress());
    }

    @Override
    public String schema() {
        return "http";
    }
}

package com.YuAN.utils;

import com.YuAN.BO.FileChunkMeta;
import com.YuAN.errors.BusinessException;
import com.YuAN.errors.EnumClientException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class ChunkAddrStrategy {
    private final Map<String,ClientUtilChunkAddrBuilder> builderMap;

    public ChunkAddrStrategy(List<ClientUtilChunkAddrBuilder> builderList) {
        this.builderMap = new HashMap<>();
        builderList.forEach(builder -> builderMap.put(builder.schema(),builder));
    }

    public String get(FileChunkMeta fileChunkMeta){
        String schema = fileChunkMeta.getSchema();
        ClientUtilChunkAddrBuilder clientUtilChunkAddrBuilder = builderMap.get(schema);
        if (Objects.isNull(clientUtilChunkAddrBuilder)){
            throw new BusinessException(EnumClientException.SCHEMA_DOES_NOT_SUPPORT);
        }
        return clientUtilChunkAddrBuilder.build(fileChunkMeta);
    }
}

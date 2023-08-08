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
public class ChunkDownloaderStrategy {
    private final Map<String,ChunkDownloader> downloaderMap;


    public ChunkDownloaderStrategy(List<ChunkDownloader> downloaderList) {
        this.downloaderMap = new HashMap<>();
        downloaderList.forEach(downloader -> downloaderMap.put(downloader.schema(),downloader));

    }

    public byte[] download(FileChunkMeta fileChunkMeta){
        String schema = fileChunkMeta.getSchema();
        ChunkDownloader chunkDownloader = downloaderMap.get(schema);
        if (Objects.isNull(chunkDownloader)){
            throw new BusinessException(EnumClientException.SCHEMA_DOES_NOT_SUPPORT);
        }
        return chunkDownloader.download(fileChunkMeta);
    }
}

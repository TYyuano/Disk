package com.YuAN.utils.impl;

import com.YuAN.BO.FileChunkMeta;
import com.YuAN.BO.MetaFile;
import com.YuAN.response.CommonResponse;
import com.YuAN.utils.ChunkAddrStrategy;
import com.YuAN.utils.ChunkDownloader;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class HttpChunkDownloader implements ChunkDownloader {
    private final ChunkAddrStrategy chunkAddrStrategy;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    public HttpChunkDownloader(ChunkAddrStrategy chunkAddrStrategy, RestTemplate restTemplate, ObjectMapper mapper) {
        this.chunkAddrStrategy = chunkAddrStrategy;
        this.restTemplate = restTemplate;
        this.mapper = mapper;
    }

    @Override
    public byte[] download(FileChunkMeta chunkMeta) {
        String address = chunkAddrStrategy.get(chunkMeta);
        String url = address+"/file/read?filename={filename}&extension={extension}&chunkNo={chunkNo}&bucketName={bucketName}";
        Map<String,Object> param = new HashMap<>();
        param.put("filename",chunkMeta.getFilename());
        param.put("extension",chunkMeta.getExtension());
        param.put("chunkNo",chunkMeta.getChunkNo());
        param.put("bucketName",chunkMeta.getBucketName());
        Object response = restTemplate.getForObject(url, Object.class, param);
        CommonResponse<byte[]> commonResponse = mapper.convertValue(response, new TypeReference<CommonResponse<byte[]>>() {
        });
        return commonResponse.getData();
    }

    @Override
    public String schema() {
        return "http";
    }
}

package com.YuAN.service.impl;

import com.YuAN.BO.CompleteChunkBo;
import com.YuAN.BO.FileChunkBO;
import com.YuAN.BO.FileChunkMeta;
import com.YuAN.BO.MetaFile;
import com.YuAN.Dto.FileMeta;
import com.YuAN.config.ClientConfig;
import com.YuAN.errors.BusinessException;
import com.YuAN.errors.EnumClientException;
import com.YuAN.response.CommonResponse;
import com.YuAN.service.FileService;
import com.YuAN.utils.ChunkAddrStrategy;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileServiceImpl implements FileService {
    private final RestTemplate restTemplate;
    private final ClientConfig config;
    private final ObjectMapper mapper;
    private final ChunkAddrStrategy chunkAddrStrategy;

    public FileServiceImpl(RestTemplate restTemplate, ClientConfig config, ObjectMapper mapper, ChunkAddrStrategy chunkAddrStrategy) {
        this.restTemplate = restTemplate;
        this.config = config;
        this.mapper = mapper;
        this.chunkAddrStrategy = chunkAddrStrategy;
    }

    @Override
    public String upload(String bucketName, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (Objects.nonNull(originalFilename)){
            int doIndex = originalFilename.lastIndexOf(".");
            if (doIndex != -1){
                extension = originalFilename.substring(doIndex);
            }
        }
        FileMeta fileMeta = new FileMeta()
                .setBucketName(bucketName)
                .setExtension(extension)
                .setFilesize(file.getSize());
        Object response = restTemplate.postForObject(config.getMetaServerAddr() + "/meta/generate", fileMeta, Object.class);
        CommonResponse<MetaFile> commonResponse = mapper.
                convertValue(response, new TypeReference<CommonResponse<MetaFile>>() {
        });
        if (Objects.isNull(commonResponse)){
            throw new BusinessException(EnumClientException.FAILED_TO_GET_METAFILE);
        }
        MetaFile metaFile = commonResponse.getData();
        if (Objects.isNull(metaFile)){
            throw new BusinessException("metafile为空",
                    EnumClientException.FAILED_TO_GET_METAFILE);
        }

        try {
            uploadChunks(file, metaFile);
        } catch(Exception e){
//            e.printStackTrace();
            throw new BusinessException(EnumClientException.FAILED_TO_UPLOAD_CHUNK_FILE);
        }
        return "%s/%s.%s".formatted(bucketName,metaFile.getFilename(),metaFile.getExtension());
    }
    private void uploadChunks(MultipartFile file,MetaFile metaFile) throws IOException {
        List<FileChunkMeta> chunks = metaFile.getChunks();

        chunks = chunks.stream()
                        .filter(e -> !e.getCompleted()).collect(Collectors.toList());
        if (chunks.isEmpty()){
            return;
        }
        InputStream inputStream = file.getInputStream();
        for (FileChunkMeta chunk : chunks) {
            FileChunkBO fileChunkBO = new FileChunkBO();
            Integer chunkSize = chunk.getChunkSize();
            byte[] buffer = new byte[chunkSize];
            inputStream.read(buffer);

            fileChunkBO.setFilename(chunk.getFilename())
                    .setExtension(chunk.getExtension())
                    .setChunkNo(chunk.getChunkNo())
                    .setChunkSize(chunkSize)
                    .setBucketName(chunk.getBucketName())
                    .setBytes(buffer);
//            String schema = chunk.getSchema();
//            String address = schema + "://" + chunk.getAddress();
            String addr = chunkAddrStrategy.get(chunk);
            Objects response = restTemplate
                    .postForObject(addr + "/file/write", fileChunkBO, Objects.class);
            if (Objects.isNull(response)){
                throw new BusinessException("第"+chunk.getChunkNo()+"分片上传失败",EnumClientException.FAILED_TO_UPLOAD_CHUNK_FILE);
            }

            CompleteChunkBo completeChunkBo = new CompleteChunkBo();
            completeChunkBo.setFilename(chunk.getFilename())
                    .setChunkNo(chunk.getChunkNo())
                    .setAddress(chunk.getAddress())
                    .setSchema(chunk.getSchema());

            Objects resp = restTemplate.postForObject(
                    config.getMetaServerAddr() + "/meta/chunk/complete",
                    completeChunkBo,
                    Objects.class
            );
            if (Objects.isNull(resp)){
                throw new BusinessException(EnumClientException.FAILED_TO_UPDATE_CHUNK_FILE_COMPLETE_STATUS);
            }
            log.info("更新文件分片状态:{}",resp);
        }
    }
}

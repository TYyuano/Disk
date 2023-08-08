package com.YuAN.service.impl;

import com.YuAN.BO.CompleteChunkBo;
import com.YuAN.BO.FileChunkBO;
import com.YuAN.BO.FileChunkMeta;
import com.YuAN.BO.MetaFile;
import com.YuAN.Dto.FileMeta;
import com.YuAN.Vo.BucketVO;
import com.YuAN.Vo.FileChunkMetaVo;
import com.YuAN.Vo.MetaFileVo;
import com.YuAN.config.ClientConfig;
import com.YuAN.errors.BusinessException;
import com.YuAN.errors.EnumClientException;
import com.YuAN.response.CommonResponse;
import com.YuAN.service.FileService;
import com.YuAN.utils.ChunkAddrStrategy;
import com.YuAN.utils.ChunkDownloaderStrategy;
import com.YuAN.utils.Md5Util;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileServiceImpl implements FileService {
    private final RestTemplate restTemplate;
    private final ClientConfig config;
    private final ObjectMapper mapper;
    private final ChunkAddrStrategy chunkAddrStrategy;
    private final ChunkDownloaderStrategy chunkDownloaderStrategy;

    public FileServiceImpl(RestTemplate restTemplate, ClientConfig config, ObjectMapper mapper, ChunkAddrStrategy chunkAddrStrategy, ChunkDownloaderStrategy chunkDownloaderStrategy) {
        this.restTemplate = restTemplate;
        this.config = config;
        this.mapper = mapper;
        this.chunkAddrStrategy = chunkAddrStrategy;
        this.chunkDownloaderStrategy = chunkDownloaderStrategy;
    }

    @Override
    public String upload(String bucketName, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if(Objects.nonNull(originalFilename)){
            int dotIndex = originalFilename.lastIndexOf(".");
            if(dotIndex != -1){
                extension = originalFilename.substring(dotIndex);
            }
        }
        FileMeta fileMeta = new FileMeta();
        fileMeta.setBucketName(bucketName)
                .setExtension(extension)
                .setFilesize(file.getSize());
        String metaServerAddress = config.getMetaServerAddr();
        //生成meta（元数据+分片）
        Object response = restTemplate.postForObject( metaServerAddress+ "/meta/generate",
                fileMeta, Object.class);
        CommonResponse<MetaFile> commonResponse = mapper
                .convertValue(response, new TypeReference<CommonResponse<MetaFile>>() {
                });
        if(Objects.isNull(commonResponse)){
            throw new BusinessException(EnumClientException.FAILED_TO_GET_METAFILE);
        }

        MetaFile metaFile = commonResponse.getData();
        if(Objects.isNull(metaFile)){
            throw new BusinessException("meta file 为空",EnumClientException.FAILED_TO_GET_METAFILE);
        }

        try {
            //上传分片
            uploadChunks(file,metaFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //返回文件路径
        return "%s/%s%s".formatted(bucketName,metaFile.getFilename(),metaFile.getExtension());
    }

    @Override
    public MetaFile getMeta(String bucketName, String fileName) {
        if(fileName.contains(".")){
            fileName = fileName.split("\\.")[0];
        }
        String url = config.getMetaServerAddr()+"/meta/info?bucketName={bucketName}&filename={filename}";
        Map<String,Object> params = new HashMap<>();
        params.put("bucketName",bucketName);
        params.put("filename",fileName);
        Object response = restTemplate.getForObject(url, Object.class, params);
        CommonResponse<MetaFile> commonResponse = mapper
                .convertValue(response, new TypeReference<CommonResponse<MetaFile>>() {
                });
        return commonResponse.getData();
    }
    @Override
    public byte[] downloadChunk(FileChunkMeta chunk) {
        return chunkDownloaderStrategy.download(chunk);
    }

    @Override
    public MetaFileVo meta(FileMeta fileMeta) {
        String url = config.getMetaServerAddr() + "/meta/generate";
        Object response = restTemplate.postForObject(
                url,
                fileMeta,
                Object.class
        );
        CommonResponse<MetaFile> commonResponse = mapper.convertValue(response,
                new TypeReference<CommonResponse<MetaFile>>() {
                });
        MetaFile metaFile = commonResponse.getData();
        return buildMetaFileVO(metaFile);
    }
    private MetaFileVo buildMetaFileVO(MetaFile metaFile) {
        MetaFileVo metaFileVO = new MetaFileVo();
        List<FileChunkMeta> originChunks = metaFile.getChunks();
        List<FileChunkMetaVo> fileChunkMetaVOS = new ArrayList<>();
        for (FileChunkMeta originChunk : originChunks) {
            FileChunkMetaVo fileChunkMetaVO = new FileChunkMetaVo();
            fileChunkMetaVO.setFilename(originChunk.getFilename())
                    .setChunkNo(originChunk.getChunkNo())
                    .setChunkStart(originChunk.getChunkStart().intValue())
                    .setChunkSize(originChunk.getChunkSize())
                    .setCompleted(originChunk.getCompleted());
            fileChunkMetaVOS.add(fileChunkMetaVO);
        }
        fileChunkMetaVOS =
                fileChunkMetaVOS.stream().distinct().collect(Collectors.toList());
        return metaFileVO.setChunks(fileChunkMetaVOS)
                .setFilename(metaFile.getFilename())
                .setBucketName(metaFile.getBucketName());
    }

    @Override
    public String uploadChunk(String bucketName, String filename, String md5, Integer chunkNo, MultipartFile file) {
        String metaServerAddress = config.getMetaServerAddr();
        String url = metaServerAddress + "/meta/chunk/info?bucketName={bucketName}&fileName={filename}&chunkNo={chunkNo}";
        Map<String, Object> params = new HashMap<>();
        params.put("filename", filename);
        params.put("bucketName", bucketName);
        params.put("chunkNo", chunkNo);
        Object resp = restTemplate.getForObject(url, Object.class, params);
        CommonResponse<List<FileChunkMeta>> chunkInfoResp = mapper.convertValue(resp, new TypeReference<CommonResponse<List<FileChunkMeta>>>() {
                        });
        List<FileChunkMeta> chunks = chunkInfoResp.getData();
        String realMd5 = Md5Util.getMd5(file);
        if (!Objects.equals(md5, realMd5)) {
            throw new BusinessException(EnumClientException.THE_SHARD_FILE_IS_INCOMPLETE);
        }
        chunks = chunks.stream().filter(c -> c.getChunkNo().equals(chunkNo)).collect(Collectors.toList());
        chunks.forEach(c -> {
            int chunkSize = c.getChunkSize();
            byte[] buffer = new byte[chunkSize];
            try (InputStream inputStream = file.getInputStream()) {
                inputStream.read(buffer);
                FileChunkBO fileChunkBO = new FileChunkBO();
                fileChunkBO.setFilename(c.getFilename())
                        .setChunkNo(c.getChunkNo())
                        .setExtension(c.getExtension())
                        .setChunkSize(c.getChunkSize())
                        .setBucketName(c.getBucketName())
                        .setBytes(buffer);
                String address = chunkAddrStrategy.get(c);
                Object response = restTemplate.postForObject(address + "/file/write", fileChunkBO, Object.class);
                if (Objects.isNull(response)) {
                    throw new RuntimeException(MessageFormat.format("第 {} 分片上 传失败", c.getChunkNo()));
                }
                CommonResponse<String> commonResponse =
                        mapper.convertValue(response, new TypeReference<CommonResponse<String>>() {
                        });
                String serverMd5 = commonResponse.getData();
                if (!Objects.equals(serverMd5, realMd5)) {
                    throw new RuntimeException(MessageFormat.format("第 {} 分片不 完整", c.getChunkNo()));
                }
                CompleteChunkBo completeChunkFileBO = new
                        CompleteChunkBo();
                completeChunkFileBO.setFilename(c.getFilename())
                        .setChunkNo(c.getChunkNo())
                        .setSchema(c.getSchema())
                        .setMd5(md5)
                        .setAddress(c.getAddress());
                Object completeResp = restTemplate.postForObject(metaServerAddress + "/meta/chunk/complete",
                        completeChunkFileBO,
                        Object.class
                );
                if (Objects.isNull(completeResp)) {
                    throw new RuntimeException(MessageFormat.format("第 {} 分片状 态更新失败", c.getChunkNo()));
                }
            } catch (Exception e) {
                log.info("第 {} 分片上传失败，原因：", c.getChunkNo(), e);
                throw new BusinessException(EnumClientException.FAILED_TO_UPLOAD_CHUNK_FILE);
            }
        });
        return md5;

    }

    public void uploadChunks(MultipartFile file, MetaFile metaFile) throws IOException, ExecutionException, InterruptedException {
        List<FileChunkMeta> chunks = metaFile.getChunks();
        //根据chunkNo对chunks排序
        chunks = chunks.stream()
                .sorted(Comparator.comparing(FileChunkMeta::getChunkNo))
                .collect(Collectors.toList());

        if(chunks.isEmpty()) return;

        InputStream inputStream = file.getInputStream();
        //优化 利用并行的方式上传分片
        CompletableFuture<?>[] tasks = new CompletableFuture[chunks.size()];


        byte[] buffer = new byte[0];
        int preChunkNo = -1;
        for (int i = 0; i < chunks.size(); i++) {
            FileChunkMeta chunk = chunks.get(i);
            //上传未上传的分片
            FileChunkBO fileChunkBO = new FileChunkBO();
            Integer chunkSize = chunk.getChunkSize();

            if(chunk.getChunkNo() != preChunkNo){
                preChunkNo = chunk.getChunkNo();
                buffer = new byte[chunkSize];
                inputStream.read(buffer);
            }

            //创建异步任务
            byte[] finalBuffer = buffer;
            tasks[i] = CompletableFuture.runAsync(() ->{
                if(chunk.getCompleted()){
                    return;
                }
//                计算md5
                String md5 = Md5Util.getMd5(finalBuffer);

                fileChunkBO.setFilename(chunk.getFilename())
                        .setExtension(chunk.getExtension())
                        .setChunkNo(chunk.getChunkNo())
                        .setChunkSize(chunkSize)
                        .setBucketName(chunk.getBucketName())
                        .setBytes(finalBuffer);
                //利用策略模式拼接地址（协议）
                String address = chunkAddrStrategy.get(chunk);
                Object response = restTemplate.postForObject(address + "/file/write",
                        fileChunkBO,
                        Object.class);
                if(Objects.isNull(response)){
                    throw new BusinessException("第 " + chunk.getChunkNo() + " 分片上传失败",
                            EnumClientException.FAILED_TO_UPLOAD_CHUNK_FILE);

                }
                CommonResponse<String> md5Response = mapper.convertValue(response, new TypeReference<CommonResponse<String>>() {
                });

                if (!md5Response.getData().equals(md5)){
                    throw new BusinessException(EnumClientException.THE_SHARD_FILE_IS_INCOMPLETE);
                }
                //修改分片状态
                CompleteChunkBo completeChunkFileBO = new CompleteChunkBo();
                completeChunkFileBO.setFilename(chunk.getFilename())
                        .setAddress(chunk.getAddress())
                        .setSchema(chunk.getSchema())
                        .setChunkNo(chunk.getChunkNo())
                        .setMd5(md5);

                Object resp = restTemplate.postForObject(
                        config.getMetaServerAddr() + "/meta/chunk/complete",
                        completeChunkFileBO,
                        Object.class
                );

                if(Objects.isNull(resp)){
                    throw new
                            BusinessException(EnumClientException.FAILED_TO_UPDATE_CHUNK_FILE_COMPLETE_STATUS);
                }

                log.info("更新分片状态:{}",resp);
            }).whenComplete((o,throwable) ->{
                if(Objects.nonNull(throwable)){
                    throw new RuntimeException(MessageFormat.format("第 {} 分片上传失败",chunk.getChunkNo()));
                }
            });
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(tasks);
        CompletableFuture<?> anyException = new CompletableFuture<>();
        Arrays.stream(tasks).forEach( t -> t.exceptionally(throwable -> {
            anyException.completeExceptionally(throwable);
            return null;
        }));
        CompletableFuture.anyOf(allOf, anyException).get();
    }

    @Override
    public List<BucketVO> files() {
        String metaServerAddress = config.getMetaServerAddr();
        Object response = restTemplate.getForObject(metaServerAddress + "/meta/files", Object.class);
        CommonResponse<List<BucketVO>> commonResponse =
                mapper.convertValue(response, new TypeReference<CommonResponse<List<BucketVO>>>(){});
        return commonResponse.getData();
    }

    @Override
    public void delete(String bucketName, String fileName) {
        String metaServerAddress = config.getMetaServerAddr();
        String url = "%s/meta/%s/%s".formatted(metaServerAddress, bucketName,
                fileName);

        restTemplate.delete(url);
    }

}

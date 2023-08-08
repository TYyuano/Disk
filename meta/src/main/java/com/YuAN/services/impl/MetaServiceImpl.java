package com.YuAN.services.impl;

import com.YuAN.Do.FileChunkMeta;
import com.YuAN.Do.MetaFile;
import com.YuAN.Vo.BucketVO;
import com.YuAN.Vo.FileVO;
import com.YuAN.bo.ServerInfo;
import com.YuAN.config.MetaConfig;
import com.YuAN.dto.CompleteChunkDto;
import com.YuAN.dto.FileMeta;
import com.YuAN.errors.BusinessException;
import com.YuAN.errors.EnumMetaException;
import com.YuAN.services.DiscoverServer;
import com.YuAN.services.MetaService;
import com.YuAN.utils.FilenameGenerator;
import com.YuAN.utils.RequestUtil;
import com.YuAN.utils.ServerSelector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MetaServiceImpl implements MetaService {
    private final FilenameGenerator filenameGenerator;
    private final HttpServletRequest request;
    private final MetaConfig metaConfig;
    private final MongoTemplate mongoTemplate;
    private final DiscoverServer discoverServer;
    private final ServerSelector serverSelector;

    public MetaServiceImpl(FilenameGenerator filenameGenerator,
                           HttpServletRequest request, MetaConfig metaConfig, MongoTemplate mongoTemplate, DiscoverServer discoverServer, ServerSelector serverSelector) {
        this.filenameGenerator = filenameGenerator;
        this.request = request;
        this.metaConfig = metaConfig;
        this.mongoTemplate = mongoTemplate;
        this.discoverServer = discoverServer;
        this.serverSelector = serverSelector;
    }

    @Override
    public MetaFile generate(FileMeta fileMeta) {
        Long filesize = fileMeta.getFilesize();
        String extension = fileMeta.getExtension();
        String bucketName = fileMeta.getBucketName();
        String clientIpAddr = RequestUtil.getIpAddr(request);
        Integer chunkSize = metaConfig.getChunkSize();



        String filename = filenameGenerator.generate(fileMeta, clientIpAddr);
        MetaFile metaFile = mongoTemplate.findById(filename, MetaFile.class);
        if (Objects.nonNull(metaFile)){
            return metaFile;
        }
        metaFile = new MetaFile();



        int totalChunk = (int) Math.ceil((filesize * 1.0 / chunkSize));


        List<FileChunkMeta> chunks = createChunks(  filesize,
                                                    extension,
                                                    bucketName,
                                                    chunkSize,
                                                    filename,
                                                    totalChunk);


        metaFile.setFilename(filename)
                .setExtension(extension)
                .setFilesize(filesize)
                .setBucketName(bucketName)
                .setTotalChunk(totalChunk)
                .setChunks(chunks)
                .setCompleted(false);

        mongoTemplate.insert(metaFile);
        return metaFile;
    }

    @Override
    public synchronized void completeChunk(CompleteChunkDto completeChunk) {
        String filename = completeChunk.getFilename();
        MetaFile metaFile = mongoTemplate.findById(filename, MetaFile.class);
        if (Objects.isNull(metaFile)){
            throw new BusinessException(EnumMetaException.META_FILE_NOT_FOUND);
        }
        AtomicBoolean completed = new AtomicBoolean(true);
        metaFile.getChunks().forEach(c -> {
            if (c.getChunkNo().equals(completeChunk.getChunkNo())&&
                    c.getAddress().equals(completeChunk.getAddress())&&
                    c.getSchema().equals(completeChunk.getSchema())){
                c.setChunkMd5(completeChunk.getMd5());
                c.setCompleted(true);
            }
            if (!c.getCompleted()){
                completed.set(false);
            }
        });
        metaFile.setCompleted(completed.get());
        mongoTemplate.save(metaFile);
    }

    @Override
    public MetaFile meta(String bucketName, String filename) {
        MetaFile metaFile = mongoTemplate.findById(filename, MetaFile.class);
        if (Objects.isNull(metaFile)){
            throw new BusinessException(EnumMetaException.META_FILE_NOT_FOUND);
        }
        List<ServerInfo> serverInfos = discoverServer.aliveServers();
        Set<String> addressSet = serverInfos.stream().map(serverInfo ->
                serverInfo.getHost() + ":" + serverInfo.getPort())
                        .collect(Collectors.toSet());
        List<FileChunkMeta> chunks = metaFile.getChunks();
        chunks.forEach(c -> {
            String address = c.getAddress();
            if (!addressSet.contains(address)){
                c.setWeight(c.getWeight() > 0 ? c.getWeight()-1 : 0);
            }else {
                c.setWeight(metaConfig.getChunkInstanceMaxWeight());
            }
            if (!c.getCompleted()){
                throw new BusinessException(EnumMetaException.CHUNK_FILE_NOT_UPLOAD);
            }
        });
        mongoTemplate.save(metaFile);

        List<FileChunkMeta> chunkMetas = chunks.stream()
                .collect(Collectors.groupingBy(FileChunkMeta::getChunkNo))
                .values()
                .stream()
                .parallel()
                .map(fileChunkMetas ->
                        fileChunkMetas.
                                stream()
                                .peek(c -> {
                                    String address = c.getAddress();
                                    if (!addressSet.contains(address)) {
                                        c.setWeight(c.getWeight() > 0 ? c.getWeight() - 1 : 0);

                                    }
                                })
                                .max(Comparator.comparing(FileChunkMeta::getWeight))
                                .orElse(new FileChunkMeta())
                )
                .filter(e -> e.getWeight() > 0)
                .toList();
        if (chunkMetas.size()<metaFile.getTotalChunk()){
            Set<Integer> chunkNOSet = chunkMetas
                    .stream()
                    .map(FileChunkMeta::getChunkNo)
                    .collect(Collectors.toSet());
            List<Integer> lossChunkNo = new ArrayList<>();
            for (int i = 0; i < metaFile.getTotalChunk(); i++) {
                if (!chunkNOSet.contains(i)){
                    lossChunkNo.add(i);
                }
            }
            log.warn("文件{}存在分片不可用:{}",filename,lossChunkNo);
            throw new BusinessException(EnumMetaException.NO_CHUNK_META_AVAILABLE);
        }
        metaFile.setChunks(chunkMetas);
        return metaFile;
    }

    /**
     * 创建文件分片元数据
     *
     * @param filesize 文件大小
     * @param extension 文件后缀名
     * @param bucketName 文件存储桶名字
     * @param chunkSize 分片大小
     * @param filename 文件名字
     * @param totalChunk 分片数量
     * @return 分片元数据列表
     */
    @NotNull
    private List<FileChunkMeta> createChunks(Long filesize,
                                             String extension,
                                             String bucketName,
                                             Integer chunkSize,
                                             String filename,
                                             int totalChunk) {
        List<FileChunkMeta> chunks = new ArrayList<>();
        List<ServerInfo> aliveServers = discoverServer.aliveServers();
        if (aliveServers.size()==0){
            throw new BusinessException(EnumMetaException.NOT_ENOUGH_CHUNK_SERVER);
        }
        long start = 0;
        for (int i = 0; i < totalChunk; i++) {
            long currentChunkSize = chunkSize;
            if (filesize < (long) (i + 1) * chunkSize){
                currentChunkSize = filesize - (long) i * chunkSize;
            }
            List<ServerInfo> selectServers = serverSelector.select(aliveServers, metaConfig.getChunkInstanceCount());

            for (ServerInfo selectServer : selectServers) {
                String address = selectServer.getHost()+":"+selectServer.getPort();

                FileChunkMeta fileChunkMeta = new FileChunkMeta();
                fileChunkMeta
                        .setFilename(filename)
                        .setChunkNo(i)
                        .setBucketName(bucketName)
                        .setChunkStart(start)
                        .setChunkSize((int) currentChunkSize)
                        .setExtension(extension)
                        .setAddress(address)
                        .setSchema(selectServer.getSchema())
                        .setWeight(metaConfig.getChunkInstanceMaxWeight());
                chunks.add(fileChunkMeta);
            }
            start += currentChunkSize;
        }
        return chunks;
    }

    @Override
    public List<BucketVO> files() {
        List<MetaFile> allFilesMeta = mongoTemplate.findAll(MetaFile.class);
        Map<String, List<MetaFile>> bucketMap = allFilesMeta.stream()
                .collect(Collectors.groupingBy(MetaFile::getBucketName));
        return bucketMap.entrySet().stream().map(entry -> {
            List<FileVO> fileVOList = entry.getValue().stream()
                    .filter(MetaFile::getCompleted)
                    .map(metaFile -> new FileVO()
                            .setFilename(metaFile.getFilename())
                            .setExtension(metaFile.getExtension())
                            .setBucketName(metaFile.getBucketName())
                            .setFileSize(metaFile.getFilesize())).toList();
            return new BucketVO().setBucketName(entry.getKey()).setFiles(fileVOList);
        }).toList();
    }

    @Override
    public void delete(String bucketName, String fileName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("filename").is(fileName));
        mongoTemplate.remove(query, MetaFile.class);
    }

    @Override
    public List<FileChunkMeta> chunkInfo(String bucketName, String fileName,
                                         Integer chunkNo) {
        if (fileName.contains(".")) {
            fileName = fileName.split("\\.")[0];
        }
        MetaFile metaFile = mongoTemplate.findById(fileName, MetaFile.class);
        if (Objects.isNull(metaFile)) {
            throw new BusinessException(EnumMetaException.META_FILE_NOT_FOUND);
        }
        return metaFile.getChunks()
                .stream()
                .filter(chunk -> chunk.getChunkNo().equals(chunkNo))
                .collect(Collectors.toList());
    }

}

package com.YuAN.services.impl;

import com.YuAN.Do.FileChunkMeta;
import com.YuAN.Do.MetaFile;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
                .setChunks(chunks);
        mongoTemplate.insert(metaFile);
        return metaFile;
    }

    @Override
    public void completeChunk(CompleteChunkDto completeChunk) {
        String filename = completeChunk.getFilename();
        MetaFile metaFile = mongoTemplate.findById(filename, MetaFile.class);
        if (Objects.isNull(metaFile)){
            throw new BusinessException(EnumMetaException.META_FILE_NOT_FOUND);
        }
        metaFile.getChunks().forEach(c -> {
            if (c.getChunkNo().equals(completeChunk.getChunkNo())&&
                    c.getAddress().equals(completeChunk.getAddress())&&
                    c.getSchema().equals(completeChunk.getSchema())){
                c.setCompleted(true);
            }
        });
        mongoTemplate.save(metaFile);
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
//                if (metaConfig.getUseHttps()){
//                    address = "https://"+  address;
//                }else {
//                    address = "http://" + address;
//                }
                FileChunkMeta fileChunkMeta = new FileChunkMeta();
                fileChunkMeta
                        .setFilename(filename)
                        .setChunkNo(i)
                        .setBucketName(bucketName)
                        .setChunkStart(start)
                        .setChunkSize((int) currentChunkSize)
                        .setExtension(extension)
                        .setAddress(address)
                        .setSchema(selectServer.getSchema());
                chunks.add(fileChunkMeta);
            }
            start += currentChunkSize;
        }
        return chunks;
    }
}

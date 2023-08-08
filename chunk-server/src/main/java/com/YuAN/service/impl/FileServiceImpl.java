package com.YuAN.service.impl;

import com.YuAN.config.ChunkConfig;
import com.YuAN.entity.FileChunkDto;
import com.YuAN.errors.BusinessException;
import com.YuAN.errors.ChunkException;
import com.YuAN.service.FileService;
import com.YuAN.utils.Md5Util;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class FileServiceImpl implements FileService {
    private final ChunkConfig chunkConfig;
    private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();

    public FileServiceImpl(ChunkConfig chunkConfig) {
        this.chunkConfig = chunkConfig;
    }

    @Override
    public String write(FileChunkDto fileChunkDto) {
        String filename = fileChunkDto.getFilename();
        String bucketName = fileChunkDto.getBucketName();
        Integer chunkSize = fileChunkDto.getChunkSize();
        Integer chunkNo = fileChunkDto.getChunkNo();
        String extension = fileChunkDto.getExtension();
        byte[] bytes = fileChunkDto.getBytes();

        String chunkPath = buildChunkPath(bucketName, filename, chunkNo, extension);
        File chunkFile = new File(chunkPath);
        try (FileOutputStream fileOutputStream = new FileOutputStream(chunkFile)){
            reentrantReadWriteLock.writeLock().lock();
            if(chunkFile.getFreeSpace() < chunkSize){
                throw new BusinessException(ChunkException.DISK_SPACE_NOT_ENOUGH_MEMORY);
            }
            if (!chunkFile.exists()){
                boolean created = chunkFile.createNewFile();
                if (!created){
                    throw new BusinessException(ChunkException.FAILED_TO_CREATE_CHUNK_FILE);
                }
            }
            fileOutputStream.write(bytes);
            return Md5Util.getMd5(bytes);
        } catch(Exception e){
            throw new BusinessException(ChunkException.FAILED_TO_CREATE_CHUNK_FILE);
        } finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    @Override
    public byte[] read(String filename,
                       String extension,
                       Integer chunkNo,
                       String bucketName) {
        String buildChunkPath = buildChunkPath(bucketName, filename, chunkNo, extension);
        try{
            reentrantReadWriteLock.readLock().lock();
            return Files.readAllBytes(Paths.get(buildChunkPath));
        } catch(Exception e){
            throw new BusinessException(ChunkException.FAILED_TO_READ_CHUNK_FILE);
        } finally {
            reentrantReadWriteLock.readLock().unlock();
        }

    }

    private String buildChunkPath(String bucketName,
                                  String filename,
                                  Integer chunkNo,
                                  String extension){
        return "%s/%s_%s_%s%s".formatted(chunkConfig.getWorkspace(),
                                            bucketName,
                                            filename,
                                            chunkNo,
                                            extension);

    }
}

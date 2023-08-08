package com.YuAN.BO;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FileChunkMeta {
    private String filename;
    private String extension;
    private Integer chunkNo;
    private String bucketName;
    private Long chunkStart;
    private Integer chunkSize;
    private String address;
    private String chunkMd5;
    private String schema;
    private Boolean completed = false;

}

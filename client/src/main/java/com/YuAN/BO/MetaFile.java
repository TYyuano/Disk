package com.YuAN.BO;

import lombok.Data;
import lombok.experimental.Accessors;


import java.util.List;

@Data
@Accessors(chain = true)
public class MetaFile {
    private String filename;
    private String extension;
    private Long filesize;
    private String bucketName;
    private Integer totalChunk;
    private List<FileChunkMeta> chunks;

}

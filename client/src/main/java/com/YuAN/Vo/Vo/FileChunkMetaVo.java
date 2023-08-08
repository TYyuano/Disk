package com.YuAN.Vo.Vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FileChunkMetaVo {
    private String filename;
    private Integer chunkNo;
    private Integer chunkStart;
    private Integer chunkSize;
    private Boolean completed;
}

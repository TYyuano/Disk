package com.YuAN.BO;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CompleteChunkBo {
    private String filename;
    private Integer chunkNo;
    private String address;
    private String schema;
    private String md5;
}

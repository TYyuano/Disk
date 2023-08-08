package com.YuAN.Vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class MetaFileVo {
    private String filename;
    private String bucketName;
    List<FileChunkMetaVo> chunks;

}

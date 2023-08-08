package com.YuAN.Vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class FileVO {
    private String filename;
    private String bucketName;
    private Long fileSize;
    private String extension;

}

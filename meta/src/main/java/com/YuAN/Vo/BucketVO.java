package com.YuAN.Vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Data
public class BucketVO {
    private String bucketName;
    private List<FileVO> files;
}

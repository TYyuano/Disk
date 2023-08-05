package com.YuAN.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
public class FileMeta {
    @NotEmpty(message = "文件不为空")
    private Long filesize;
    @NotBlank(message = "文件后缀不为空")
    private String extension;
    @NotBlank(message = "存储桶不为空")
    private String bucketName;

}

package com.YuAN.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class FileMeta {
    @NotNull(message = "文件不为空")
    private Long filesize;
    @NotBlank(message = "文件后缀不为空")
    private String extension;
    @NotBlank(message = "存储桶不为空")
    private String bucketName;

}

package com.YuAN.Dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
@Accessors(chain = true)
public class FileMeta {
    @NotEmpty(message = "文件不为空")
    private Long filesize;
    @NotBlank(message = "文件后缀不为空")
    private String extension;
    @NotBlank(message = "存储桶不为空")
    private String bucketName;

}

package com.YuAN.Dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
public class FileMeta {
    @NotNull(message = "文件不为空")
    @Min(value = 0,message = "文件大小不得小于0")
    private Long filesize;
    @NotBlank(message = "文件后缀不为空")
    private String extension;
    @NotBlank(message = "存储桶不为空")
    private String bucketName;

}

package com.YuAN.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class CompleteChunkDto {
    @NotBlank(message = "文件名不为空")
    private String filename;
    @NotNull(message = "分片序号不为空")
    private Integer chunkNo;
    @NotBlank(message = "存储地址不得为空")
    private String address;
    @NotBlank(message = "通信协议不得为空")
    private String schema;
    @NotBlank(message = "文件MD5不得为空")
    private String md5;
}

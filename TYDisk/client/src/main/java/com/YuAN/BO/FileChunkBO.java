package com.YuAN.BO;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
public class FileChunkBO {
    @NotBlank(message = "文件名不为空")
    private String filename;
    @NotBlank(message = "文件扩展名不能为空")
    private String extension;
    @NotNull(message = "文件分片序号不为空")
    private Integer chunkNo;
    @NotNull(message = "文件大小不为空")
    private Integer chunkSize;
    @NotBlank(message = "文件存储桶不为空")
    private String bucketName;
    @NotNull(message = "文件内容不为空")
    private byte[] bytes;
}

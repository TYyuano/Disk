package com.YuAN.dto;

import lombok.Data;

@Data
public class CompleteChunkDto {
    private String filename;
    private Integer chunkNo;
    private String address;
    private String schema;
}

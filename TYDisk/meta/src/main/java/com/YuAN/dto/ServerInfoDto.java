package com.YuAN.dto;


import lombok.Data;

@Data
public class ServerInfoDto {
    private String ServerId;
    private String host;
    private Integer port;
    private String schema = "http";
}

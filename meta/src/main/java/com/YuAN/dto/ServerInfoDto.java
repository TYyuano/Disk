package com.YuAN.dto;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ServerInfoDto {
    @NotBlank(message = "服务ID不得为空")
    private String ServerId;
    @NotBlank(message = "主机IP不得为空")
    private String host;
    @NotNull(message = "主机端口不得为空")
    private Integer port;
    @NotBlank(message = "通信协议不得为空")
    private String schema = "http";
}

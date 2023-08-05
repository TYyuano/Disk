package com.YuAN.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("client")
public class ClientConfig {
    private String metaServerHost;
    private String metaServerPort;


    public String getMetaServerAddr(){
        return "%s:%s".formatted(metaServerHost,metaServerPort);
    }
}

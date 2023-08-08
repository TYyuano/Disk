package com.YuAN.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("chunk")
public class ChunkConfig {
    private String workspace;
}

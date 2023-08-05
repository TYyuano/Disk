package com.YuAN.config;

import com.YuAN.utils.FilenameGenerator;
import com.YuAN.utils.ServerSelector;
import com.YuAN.utils.impl.DefaultFilenameGenerate;
import com.YuAN.utils.impl.DefaultServerSelector;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("meta")
public class MetaConfig {

    private Integer chunkSize;
    private Integer chunkInstanceCount;
    private Boolean useHttps = false;
    @Bean
    @ConditionalOnMissingBean(value = FilenameGenerator.class)
    public FilenameGenerator filenameGenerator(){
        return new DefaultFilenameGenerate();
    }

    @Bean
    @ConditionalOnMissingBean(value = ServerSelector.class)
    public ServerSelector serverSelector(){
        return new DefaultServerSelector();
    }
}

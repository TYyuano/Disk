package com.YuAN.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
public class RestTemplateConfig {
    private final RestTemplateErrorHandler restTemplateErrorHandler;
    private final RestTemplateLoggingInterceptor restTemplateLoggingInterceptor;

    public RestTemplateConfig(RestTemplateErrorHandler restTemplateErrorHandler, RestTemplateLoggingInterceptor restTemplateLoggingInterceptor) {
        this.restTemplateErrorHandler = restTemplateErrorHandler;
        this.restTemplateLoggingInterceptor = restTemplateLoggingInterceptor;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder,
                                     ClientHttpRequestFactory clientHttpRequestFactory){
        RestTemplate restTemplate = builder.build();
        restTemplate.setInterceptors(Collections.singletonList(restTemplateLoggingInterceptor));
        restTemplate.setErrorHandler(restTemplateErrorHandler);
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(clientHttpRequestFactory));
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory(){
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setConnectTimeout(150000);
        simpleClientHttpRequestFactory.setReadTimeout(15000);
        return simpleClientHttpRequestFactory;
    }
}

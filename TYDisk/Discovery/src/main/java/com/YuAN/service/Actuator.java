package com.YuAN.service;

import com.YuAN.config.DiscoveryConfig;
import com.YuAN.utils.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class Actuator {
    @Value("${server.port}")
    private int port;
    private final DiscoveryConfig discoveryConfig;
    private final ScheduledExecutorService scheduledExecutorService;
    private final RestTemplate restTemplate;

    public Actuator(DiscoveryConfig discoveryConfig,
                    ScheduledExecutorService scheduledExecutorService,
                    RestTemplate restTemplate) {
        this.discoveryConfig = discoveryConfig;
        this.scheduledExecutorService = scheduledExecutorService;
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void register() {
        String serverAddress = discoveryConfig.getServerAddress();
        Map<String, Object> params = new HashMap<>();
        params.put("serverId", discoveryConfig.getServerId());
        params.put("host", RequestUtil.getLocalHostExactAddress());
        params.put("port", port);
        params.put("schema", discoveryConfig.getSchema());
        Map result =
                restTemplate.postForObject(discoveryConfig.getServerAddress() + "/register", params, Map.class);
        log.info("{}", result);
        if (Objects.isNull(result) || !result.get("code").equals(200)) {
            throw new RuntimeException("服务注册失败");
        }
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            restTemplate.put(discoveryConfig.getServerAddress() +
                            "/heartbeat", params);
        }, 0, 10, TimeUnit.SECONDS);
    }
}


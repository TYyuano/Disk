package com.YuAN.services.impl;

import com.YuAN.bo.ServerInfo;
import com.YuAN.dto.ServerInfoDto;
import com.YuAN.services.DiscoverServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DefaultDiscoverServer implements DiscoverServer {
    private final Map<String, List<ServerInfo>> SERVER_MAP = new HashMap<>();
    @Override
    public void register(ServerInfoDto serverInfo) {
        List<ServerInfo> serverInfos = SERVER_MAP.getOrDefault(serverInfo.getServerId(),
                new ArrayList<>());
        ServerInfo info = new ServerInfo();
        BeanUtils.copyProperties(serverInfo,info);
        info.setPreTimeStamp(System.currentTimeMillis());
        info.setAlive(true);
        if(!serverInfos.contains(info)){
            serverInfos.add(info);
        }
        SERVER_MAP.put(serverInfo.getServerId(), serverInfos);
    }

    @Override
    public void heartbeat(ServerInfoDto serverInfo) {
        List<ServerInfo> serverInfoList = SERVER_MAP.getOrDefault(serverInfo.getServerId(),new ArrayList<>());
        boolean exists = false;
        for (ServerInfo server : serverInfoList) {
            if(server.getHost().equals(serverInfo.getHost()) && server.getPort().equals(serverInfo.getPort())){
                server.setAlive(true);
                server.setPreTimeStamp(System.currentTimeMillis());
                exists = true;
            }
        }
        if (!exists){
            register(serverInfo);
        }

    }
    @Scheduled(cron = "*/10 * * * * *")
    private void checkAlive(){
        SERVER_MAP.forEach((serverId,serverList)->{
            serverList = serverList.stream().filter(server -> {
                Long preTimeStamp = server.getPreTimeStamp()/1000;
                long now = System.currentTimeMillis()/1000;
                if(now - preTimeStamp > 30){
                    server.setAlive(false);
                }
                return now - preTimeStamp < 60;
            }).collect(Collectors.toList());
            SERVER_MAP.put(serverId,serverList);
        });
        log.info("check server status end");
    }

    @Override
    public Map<String, List<ServerInfo>> server() {
        return SERVER_MAP;
    }

    @Override
    public List<ServerInfo> aliveServers() {
        List<ServerInfo> chunkServers = SERVER_MAP.getOrDefault("chunk-server",
                new ArrayList<>());
        return chunkServers
                .stream()
                .filter(ServerInfo::getAlive)
                .collect(Collectors.toList());

    }
}

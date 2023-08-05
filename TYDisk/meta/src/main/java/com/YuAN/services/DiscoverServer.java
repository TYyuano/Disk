package com.YuAN.services;

import com.YuAN.bo.ServerInfo;
import com.YuAN.dto.ServerInfoDto;

import java.util.List;
import java.util.Map;

public interface DiscoverServer {
    void register(ServerInfoDto serverInfo);

    void heartbeat(ServerInfoDto serverInfo);

    Map<String, List<ServerInfo>> server();

    List<ServerInfo> aliveServers();
}

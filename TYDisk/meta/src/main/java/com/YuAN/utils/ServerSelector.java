package com.YuAN.utils;

import com.YuAN.bo.ServerInfo;

import java.util.List;

public interface ServerSelector {
    List<ServerInfo> select(List<ServerInfo> aliveServers,int count);
}

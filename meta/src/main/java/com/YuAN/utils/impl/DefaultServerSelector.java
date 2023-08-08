package com.YuAN.utils.impl;

import com.YuAN.bo.ServerInfo;
import com.YuAN.errors.BusinessException;
import com.YuAN.errors.EnumMetaException;
import com.YuAN.utils.ServerSelector;

import java.util.ArrayList;
import java.util.List;

public class DefaultServerSelector implements ServerSelector {
    @Override
    public List<ServerInfo> select(List<ServerInfo> aliveServers, int count) {
        if (aliveServers.size() < count){
            throw new BusinessException("存活的服务数量少于分片存储数量",EnumMetaException.NOT_ENOUGH_CHUNK_SERVER);
        }
        int[] indexArray = new int[aliveServers.size()];
        for (int i = 0; i < indexArray.length; i++) {
            indexArray[i] = i;
        }
        for (int i = 0; i < count; i++) {
            int randomIndex = (int) (Math.random() * aliveServers.size());
            int temp = indexArray[randomIndex];
            indexArray[randomIndex] = indexArray[i];
            indexArray[i] = temp;
        }
        List<ServerInfo> selectServers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            selectServers.add(aliveServers.get(indexArray[i]));
        }
        return selectServers;
    }
}

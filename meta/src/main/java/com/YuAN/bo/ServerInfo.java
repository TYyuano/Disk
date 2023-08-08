package com.YuAN.bo;

import lombok.Data;

import java.util.Objects;

@Data
public class ServerInfo {
    private String ServerId;
    private String host;
    private Integer port;
    private Long preTimeStamp;
    private Boolean alive;
    private String schema;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerInfo that = (ServerInfo) o;
        return Objects.equals(ServerId, that.ServerId) && Objects.equals(host, that.host) && Objects.equals(port, that.port) && Objects.equals(schema, that.schema);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ServerId, host, port, schema);
    }
}

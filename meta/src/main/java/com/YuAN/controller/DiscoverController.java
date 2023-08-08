package com.YuAN.controller;

import com.YuAN.bo.ServerInfo;
import com.YuAN.dto.ServerInfoDto;
import com.YuAN.response.CommonResponse;
import com.YuAN.services.DiscoverServer;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/")
public class DiscoverController {
    private final DiscoverServer discoverServer;

    public DiscoverController(DiscoverServer discoverServer) {
        this.discoverServer = discoverServer;
    }

    @PostMapping("/register")
    public CommonResponse<?> register(@RequestBody @Valid ServerInfoDto serverInfo){
        discoverServer.register(serverInfo);
        return CommonResponse.success();
    }
    @PutMapping("/heartbeat")
    public void heartbeat(@RequestBody @Valid ServerInfoDto serverInfo){
        discoverServer.heartbeat(serverInfo);
    }
    @PostMapping("/server")
    public CommonResponse<?> server(){
        Map<String, List<ServerInfo>> map = discoverServer.server();
        return CommonResponse.success(map);
    }
}

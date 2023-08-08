package com.YuAN.utils;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

@Slf4j
public class RequestUtil {
    private static final String[] HEADER_CONFIG = {
            "x-forwarded-for",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
    };
    private static String serverIp;
    static {
        InetAddress ia = null;
        try {
            ia = InetAddress.getLocalHost();
            serverIp = ia.getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String getIpAddr(HttpServletRequest request) {
        String ip = null;
        for (String config : HEADER_CONFIG) {
            ip = request.getHeader(config);
            if (ip == null || ip.length() == 0 ||
                    "unknown".equalsIgnoreCase(ip)) {
                break;
            }
        }
        if (ip == null || ip.length() == 0 ||
                "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = serverIp;
        }
        return ip;
    }
    public static InetAddress getLocalHostExactAddress() {
        try {
            InetAddress candidateAddress = null;
            Enumeration<NetworkInterface> networkInterfaces =
                    NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface iface = networkInterfaces.nextElement();

                for (Enumeration<InetAddress> inetAddrs =
                     iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = inetAddrs.nextElement();

                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            return inetAddr;
                        }
                        if (candidateAddress == null) {
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            return candidateAddress == null ? InetAddress.getLocalHost() :
                    candidateAddress;
        } catch (Exception e) {
            throw new RuntimeException("获取本机 ip 失败");
        }
    }
    public static String getLocalHost() {
        return getLocalHostExactAddress().getHostAddress();
    }
}


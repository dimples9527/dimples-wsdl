package com.dimples.wsdl.util;

import com.dimples.wsdl.info.ServiceInfo;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 将wsdl解析结果缓存
 *
 * @author zhongyj <1126834403@qq.com><br/>
 * @date 2021/8/26
 */
public class CacheUtil {

    private static final ConcurrentHashMap<String, ServiceInfo> SERVICE_INFOS = new ConcurrentHashMap<>();

    /**
     * 查看 该uri是否已经缓存
     *
     * @param uri String
     * @return boolean
     */
    public static boolean contains(String uri) {
        return SERVICE_INFOS.containsKey(uri);
    }

    /**
     * 缓存 解析结果
     *
     * @param uri         String
     * @param serviceInfo ServiceInfo
     */
    public static void put(String uri, ServiceInfo serviceInfo) {
        SERVICE_INFOS.put(uri, serviceInfo);
    }

    /**
     * 从缓存中 获取serverInfo
     *
     * @param uri String
     * @return ServiceInfo
     */
    public static ServiceInfo get(String uri) {
        return SERVICE_INFOS.get(uri);
    }

    public static ConcurrentHashMap<String, ServiceInfo> getAll() {
        return SERVICE_INFOS;
    }

    public static ServiceInfo getServiceInfo(String serverName) {
        for (Entry<String, ServiceInfo> entry : SERVICE_INFOS.entrySet()) {
            ServiceInfo serviceInfo = entry.getValue();
            if (serviceInfo.getName().equals(serverName)) {
                return serviceInfo;
            }
        }
        return null;
    }
}

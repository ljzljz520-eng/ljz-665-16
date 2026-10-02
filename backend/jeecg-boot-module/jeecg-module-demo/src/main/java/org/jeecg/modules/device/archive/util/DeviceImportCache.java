package org.jeecg.modules.device.archive.util;

import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备导入暂存缓存（内存，带30分钟过期）。
 * <p>
 * 单体部署下足够使用；如为集群部署可替换为 Redis 实现，接口保持不变。
 *
 * @author jeecg-boot
 */
@Component
public class DeviceImportCache {

    private final Map<String, DeviceImportSessionHolder> CACHE = new ConcurrentHashMap<>();

    public void put(DeviceImportSessionHolder holder) {
        evictExpired();
        CACHE.put(holder.getSessionId(), holder);
    }

    public DeviceImportSessionHolder get(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        DeviceImportSessionHolder holder = CACHE.get(sessionId);
        if (holder == null) {
            return null;
        }
        if (holder.isExpired()) {
            CACHE.remove(sessionId);
            return null;
        }
        return holder;
    }

    public DeviceImportSessionHolder remove(String sessionId) {
        return CACHE.remove(sessionId);
    }

    private void evictExpired() {
        Iterator<Map.Entry<String, DeviceImportSessionHolder>> it = CACHE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, DeviceImportSessionHolder> entry = it.next();
            if (entry.getValue().isExpired()) {
                it.remove();
            }
        }
    }
}

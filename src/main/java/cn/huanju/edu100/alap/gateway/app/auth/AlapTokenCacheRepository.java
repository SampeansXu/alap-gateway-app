package cn.huanju.edu100.alap.gateway.app.auth;

import org.springframework.cache.annotation.Cacheable;

/**
 * @Description: 说明
 * @Author: xushengbin@hqwx.com
 * @Date: 2021-08-23
 */
public interface AlapTokenCacheRepository {
    /**
     * 从缓存中获取 token
     * @param key
     * @return
     */
    String getToken(String key);

    /**
     * 将token放到redis cache中去
     * @param key
     * @param appId
     * @param jwtToken
     */
    void setToken(String key, String appId, String jwtToken);
}

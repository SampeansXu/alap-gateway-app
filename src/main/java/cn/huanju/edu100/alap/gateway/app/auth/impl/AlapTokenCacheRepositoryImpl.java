package cn.huanju.edu100.alap.gateway.app.auth.impl;

import cn.huanju.edu100.alap.gateway.app.auth.AlapTokenCacheRepository;
import cn.huanju.edu100.alap.gateway.app.util.RedisUtil;
import cn.huanju.edu100.alaplat.core.shared.jwt.JwtTokenCache;
import cn.hutool.json.JSONUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * @Description: 说明
 * @Author: xushengbin@hqwx.com
 * @Date: 2021-08-23
 */
@Repository
public class AlapTokenCacheRepositoryImpl implements AlapTokenCacheRepository {
    private static final String CacheKey_Prefix = "alap:token:app:";

    /**
     * jwt在redis缓存中的过期时长(秒)
     */
    @Value("${hqgateway.token-cache.expire-seconds:7200}")
    protected Long tokenCacheExpireSeconds;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public String getToken(String key) {
        String strKey = CacheKey_Prefix + key;
        String strVal = redisUtil.get(strKey);
        if (StringUtils.isBlank(strVal)) {
            return "";
        }

        JwtTokenCache tokenCache = JSONUtil.toBean(strVal, JwtTokenCache.class);
        long intervalTime = System.currentTimeMillis() - tokenCache.getRefreshTime();
        long cacheRefreshTime = 60 * 1000L;
        if (intervalTime > cacheRefreshTime) {
            tokenCache.setRefreshTime(System.currentTimeMillis());
            strVal = JSONUtil.toJsonStr(tokenCache);
            redisUtil.set(strKey, strVal, this.tokenCacheExpireSeconds);
        }

        return tokenCache.getJwtToken();
    }

    @Override
    public void setToken(String key, String appId, String jwtToken) {
        String strKey = CacheKey_Prefix + key;
        JwtTokenCache tokenCache = new JwtTokenCache(appId, jwtToken);
        String strVal = JSONUtil.toJsonStr(tokenCache);
        redisUtil.set(strKey, strVal, this.tokenCacheExpireSeconds);
    }
}

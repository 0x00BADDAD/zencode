package com.zencode.app.services;


import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.zencode.app.web.RespClass;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.aop.framework.AopContext;






@Service
@EnableAspectJAutoProxy(exposeProxy = true)
public class RedisCacheService {

    private static final Logger logger = LogManager.getLogger(RedisCacheService.class);


    private RedisCacheManager cacheManager;

    public RedisCacheService(@Qualifier("redisCacheManager") RedisCacheManager redisCacheManager){
        this.cacheManager = redisCacheManager;
    }

    public void setRefreshToken(String sessionId, String token){
            Cache myCache1 = cacheManager.getCache("refTok");
            myCache1.put(sessionId, token);
    }

    public void setAccessToken(String sessionId, String token){
            Cache myCache1 = cacheManager.getCache("accTok");
            myCache1.put(sessionId, token);
    }


    public void setSessionId(String sessionId){
        Cache sessionCache = cacheManager.getCache("sessions");
        sessionCache.put(sessionId, "true"); // it gets auotmatically removed when TTL completes.
    }

    //@Cacheable(value = "sessions", key = "#a0")
    public boolean checkSessionId(String sessionId){
        Cache sessionCache = cacheManager.getCache("sessions");
        String val = sessionCache.get(sessionId, String.class);
        return val != null;

    }

    @Cacheable(value = "refTok", key = "#a0")
    public String getRefreshToken(String sessionId){
        logger.debug("Something went wrong, we are here in the RedisCacheService... session_id was "+ sessionId);
        return ""; // shouldn't come here

    }

    //@Cacheable(value = "deviceId", key = "#a0")
    public String getDeviceId(String sessionId){
        RedisCacheService proxy = (RedisCacheService) AopContext.currentProxy();
        String accessToken = proxy.getAccessToken(sessionId);
        RestClient restClient = RestClient.create();

        String authHeader = "Bearer " + accessToken;

        JsonNode root = restClient.get()
            .uri("https://api.spotify.com/v1/me/player")
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", authHeader)
            .retrieve()
            .body(JsonNode.class);

        if(root == null){
            logger.debug("Playback is inactive right now. may be open the spotify app!");
            return "";
        }
        String deviceId = root.path("device").path("id").asText();
        boolean activeStatus = root.path("device").path("is_active").asBoolean();
        logger.debug("got the device id of user and it is: " + deviceId + " active status is: " + activeStatus);
        return deviceId;
    }

    @Cacheable(value = "accTok", key = "#a0")
    public String getAccessToken(String sessionId){
            RedisCacheService proxy = (RedisCacheService) AopContext.currentProxy();
            String refreshToken = proxy.getRefreshToken(sessionId);

            RestClient restClient = RestClient.create();
            String clientId = "9469751d45ca49cea94be50c071a3c65";
            String clientSecret = "6139b2de2c564d9a977f34c3b27fbda4";



            String inputString = clientId + ":" + clientSecret;

            byte[] utf8Bytes = inputString.getBytes(StandardCharsets.UTF_8);
            String base64String = Base64.getEncoder().encodeToString(utf8Bytes);
            String authHeader = "Basic " + base64String;
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

            formData.add("grant_type", "refresh_token");
            formData.add("refresh_token", refreshToken);


            RespClass resp = restClient.post()
                .uri("https://accounts.spotify.com/api/token")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(RespClass.class);
            logger.debug("---->scheduler [ref token] worked... resp is: " + resp.toString());
            return resp.getAccessToken();


    }

}





package com.zencode.app.services;

import org.springframework.http.HttpStatusCode;
import com.zencode.app.web.RespClass;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.security.SecureRandom;
import java.net.URI;
import java.util.Base64;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.web.client.RestClient;

import org.springframework.beans.factory.annotation.Autowired;

import com.zencode.app.web.RespClass;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.cache.CacheManager;
import com.zencode.app.web.RespClass;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.caffeine.CaffeineCache;



@Service
public class CacheService {

    private static final Logger logger = LogManager.getLogger(CacheService.class);

    @Autowired
    private CacheManager cacheManager;


    public void setRefreshToken(String email, String token){

            CaffeineCache myCache1 = (CaffeineCache) cacheManager.getCache("refreshTokenCache");
            //String email = root.path("email").asText();
            Cache<Object, Object> nativeCache = myCache1.getNativeCache();
            nativeCache.put(email, token);


    }

    public void setAccessToken(String email, String token){
            CaffeineCache myCache1 = (CaffeineCache) cacheManager.getCache("accessTokenCache");
            //String email = root.path("email").asText();
            Cache<Object, Object> nativeCache = myCache1.getNativeCache();
            nativeCache.put(email, token);

    }

    @Cacheable("refreshTokenCache")
    public String getRefreshToken(String email){
        // logic to fetch the refresh token from spotify backend with help of auth code
        if (email.equals("admin")){
            return "AQCvmM6MO6ZOK9diYgAKVTMm5aDFR9CYbNIaqHG8BaAb3xPZCjJsrsk5rOwsqg7DEBYsQEv-Q_syPBblSW971XahKWnu0Ch6DIs5-n85umtnRPpT36FN1PQ4rDK43a-KDcw";
        }
        // should never return from this statement...
        return "";

    }

    @Cacheable("accessTokenCache")
    public String getAccessToken(String email){
            String refreshToken = getRefreshToken(email);

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


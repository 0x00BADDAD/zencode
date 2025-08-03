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
import org.springframework.web.reactive.function.client.ClientResponse;


@Service
public class CachePreloadService {
    @Autowired
    private Cache<String, Object> myCache;

    private static final Logger logger = LogManager.getLogger(CachePreloadService.class);

    public RespClass convertResponse(ClientResponse response) {
        return response.body(RespClass.class);
    }


    @PostConstruct // Called after the bean is constructed
    public void loadInitialData() {

        String clientId = "9469751d45ca49cea94be50c071a3c65";
        String redirectUri = "http://127.0.0.1:3000/api/spotify_login_success";
        SecureRandom sr = new SecureRandom();
        byte[] bytes = new byte[16];
        sr.nextBytes(bytes);
        String csrfToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RestClient restClient = RestClient.create();

        try {
            // Example API endpoint
            String url = "https://accounts.spotify.com/authorize?client_id=" +clientId+ "&response_type=code" + "&redirect_uri="+redirectUri+"&scope=user-read-playback-state user-read-currently-playing"+"&state="+csrfToken;

            RespClass resp = restClient.get()
                .uri(url)
                .exchange((request, response) -> {
                
                        logger.debug("Initial status code: " + response.getStatusCode());
        
                        if (response.getStatusCode().is3xxRedirection()) {
                            URI redirect_Uri = response.getHeaders().getLocation();
                            logger.debug("Redirecting to: " + redirect_Uri);
        
                            // Follow manually
                            return restClient.get()
                                .uri(redirect_Uri)
                                .exchange((req, res) -> {
                                    logger.debug("Initial status code[second time]: " + res.getStatusCode());

                                    if(res.getStatusCode().is3xxRedirection()) {
                                        URI redirect_Uri_ = res.getHeaders().getLocation();
                                        logger.debug("Again Redirecting to: "+ redirect_Uri_);

                                        return restClient.get()
                                            .uri(redirect_Uri_)
                                            .retrieve()
                                            .body(RespClass.class);
                                    }
                                    logger.debug("No redirect second time");
                                    return convertResponse(res);
                                });
                        }
                        return new RespClass();
                        //return response.body(RespClass.class);
                });

            myCache.put("accessToken", resp.getAccessToken());
            myCache.put("refreshToken", resp.getRefreshToken());
            logger.debug("preloaded cache  with: "+ resp.getAccessToken() + " and " + resp.getRefreshToken());
        } catch (Exception e) {
            logger.debug("Whoops! some error occured while preloading the cache!");
            System.err.println("Failed to preload cache: " + e.getMessage());
        }
    }
}


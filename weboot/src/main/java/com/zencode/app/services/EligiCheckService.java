package com.zencode.app.services;

import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import com.zencode.app.services.RedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;



@Service
public class EligiCheckService{
    private static final Logger logger = LogManager.getLogger(EligiCheckService.class);

    @Autowired
    private RedisCacheService cacheService;

    public boolean check(String sessionId){
        String accessToken = cacheService.getAccessToken(sessionId);
        RestClient restClient = RestClient.create();

        String authHeader = "Bearer " + accessToken;

        JsonNode resp = restClient.get()
            .uri("https://api.spotify.com/v1/me")
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", authHeader)
            .retrieve()
            .body(JsonNode.class);

        if(resp != null){
            String product = resp.path("product").asText();
            if (product.equals("premium")){return true;}
            return false;
        }else{
            logger.debug("the response was empty when trying to check the eligibilty!");
        }
        return false;
    }


}


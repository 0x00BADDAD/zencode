package com.zencode.app.schedulers.tasks;

// upon app startup- get refresh token and store it, this process happens only once
// then periodically use refresh token to get a new access token, this is scheduled for every 30 mins
// every second hit the current playing track api using the stored access token
// i will use a in-memory key-value cache which will be preloaded by access token and refresh token
// on application startup
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Optional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import java.util.Base64;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;

import com.zencode.app.web.RespClass;
import com.github.benmanes.caffeine.cache.Cache;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.ArrayList;



@Component
public class SpotifyTasks {
    @Autowired
    private Cache myCache;

    private static final Logger logger = LogManager.getLogger(SpotifyTasks.class);

    private static final long THIRTY_MINUTES = 1000 * 60 * 30;

    @Scheduled(fixedRate = THIRTY_MINUTES)
    public void fetchAccessToken() {
            RestClient restClient = RestClient.create();
            Optional<Object> cachedRefreshToken = Optional.ofNullable(myCache.getIfPresent("refreshToken"));
            if (cachedRefreshToken.isPresent()){
                String clientId = "9469751d45ca49cea94be50c071a3c65";
                String clientSecret = "6139b2de2c564d9a977f34c3b27fbda4";



                String inputString = clientId + ":" + clientSecret;

                // Step 2: Get the UTF-8 encoded bytes of the string.
                // This is equivalent to the JavaScript TextEncoder().
                byte[] utf8Bytes = inputString.getBytes(StandardCharsets.UTF_8);

                // Step 3: Encode the UTF-8 bytes to Base64.
                // This is equivalent to the JavaScript btoa() function.
                String base64String = Base64.getEncoder().encodeToString(utf8Bytes);
                String authHeader = "Basic " + base64String;
                MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

                formData.add("grant_type", "refresh_token");
                formData.add("refresh_token", String.valueOf(cachedRefreshToken.get()));


                RespClass resp = restClient.post()
                    .uri("https://accounts.spotify.com/api/token")
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", authHeader)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(RespClass.class);
                logger.debug("---->scheduler [ref token] worked... resp is: %s".format(resp.toString()));
                myCache.put("accessToken", resp.getAccessToken());

           }else{
               logger.debug("Cache doesn't have refresh token!");
           }
    }

    @Scheduled(fixedRate = 1000)
    public void fetchCurrSong(){
            RestClient restClient = RestClient.create();
            Optional<Object> cachedAccessToken = Optional.ofNullable(myCache.getIfPresent("accessToken"));
           // TODO: proper error handling on all scheduled tasks 
            if (cachedAccessToken.isPresent()){
                String accessToken = String.valueOf(cachedAccessToken.get());
                String authHeader = "Bearer " + accessToken;
                JsonNode root = restClient.get()
                    .uri("https://api.spotify.com/v1/me/player/currently-playing")
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", authHeader)
                    .retrieve()
                    .body(JsonNode.class);

                String trackUri = root.path("item").path("href").asText();

                JsonNode root_ = restClient.get()
                   .uri(trackUri)
                   .accept(MediaType.APPLICATION_JSON)
                   .header("Authorization", authHeader)
                   .retrieve()
                   .body(JsonNode.class);

                String songName = root_.path("name").asText();
                List<String> artistsAll = new ArrayList<>();

                JsonNode artists = root_.path("artists");
                if (artists.isArray()){
                    for (JsonNode artist: artists){
                        String artistName = artist.path("name").asText();
                        artistsAll.add(artistName);
                    }
                }
                logger.debug("Song Name: "+ songName + " Artists: "+ artistsAll.toString());
            }

    }
    //@Scheduled(cron = "0 */1 * * * *")
    //public void runEveryMinute() {
    //    System.out.println("Task every minute: " + Thread.currentThread().getName());
    //}
}

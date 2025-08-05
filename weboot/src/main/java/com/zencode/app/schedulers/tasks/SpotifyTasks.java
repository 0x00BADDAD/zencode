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

import com.zencode.app.ws.handlers.beans.TrackMetadataBean;
import com.zencode.app.ws.handlers.MyHandler;
import com.zencode.app.services.CacheService;



@Component
public class SpotifyTasks {

    @Autowired
    private CacheService cacheService;

    private static final Logger logger = LogManager.getLogger(SpotifyTasks.class);


    @Autowired
    private MyHandler myHandler;


    @Scheduled(fixedRate = 1000)
    public void fetchCurrSong(){
            String accessToken = cacheService.getAccessToken("admin");
            RestClient restClient = RestClient.create();
           // TODO: proper error handling on all scheduled tasks 
            String authHeader = "Bearer " + accessToken;
            //logger.debug("requesting track metadata from spotify!!");
            JsonNode root = restClient.get()
                .uri("https://api.spotify.com/v1/me/player/currently-playing")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", authHeader)
                .retrieve()
                .body(JsonNode.class);
            if (root != null){
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
                TrackMetadataBean trackMetadataBean = new TrackMetadataBean(songName, artistsAll);
                //logger.debug("Song Name: "+ songName + " Artists: "+ artistsAll.toString());
                myHandler.broadcast(trackMetadataBean);
            }else{
                //logger.debug("No song playing right now!");
                myHandler.broadcast(new TrackMetadataBean("No music playing right now!", List.of()));
            }

    }
}

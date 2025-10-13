package com.zencode.app.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import com.zencode.app.services.RedisCacheService;
import com.zencode.app.ws.handlers.beans.TrackMetadataBean;
import com.zencode.app.ws.handlers.MyHandler;

import com.zencode.app.sessions.UserSession;

@Service
public class SyncTracksService {

    @Autowired
    private RedisCacheService cacheService;

    @Autowired
    private CacheService oldCacheService;


    @Async
    public CompletableFuture<String> syncTracks(String sessionId, String deviceId, boolean syncing, TrackMetadataBean currTrackMetaData){

            if(syncing){
                String resultStr = "Syncing Done!";
                return CompletableFuture.completedFuture(resultStr);
            }


            String accessToken = cacheService.getAccessToken(sessionId);
            String accessTokenTrack = oldCacheService.getAccessToken("admin");

            String authHeader_ = "Bearer " + accessTokenTrack;
            String authHeader = "Bearer " + accessToken;

            String resource_uri = currTrackMetaData.getResourceUri();
            String trackUri = currTrackMetaData.getTrackUri();
            Integer position_ms = currTrackMetaData.getProgressMs();

            //String deviceId = cacheService.getDeviceId(sessionId);
            boolean is_playing = currTrackMetaData.getIsPlaying();

            // map for json body
            Map<String, Object> bodyJson = new HashMap<>();
            // List<String> uris = List.of(trackUri);
            bodyJson.put("context_uri", resource_uri);
            bodyJson.put("offset", Map.ofEntries(Map.entry("uri", trackUri)));
            bodyJson.put("position_ms", position_ms);

            UriComponents uriComponents = UriComponentsBuilder
                    .fromUriString("https://api.spotify.com/v1/me/player/play")
                    .queryParam("device_id", "{device_id}")
                    .encode()
                    .build();

            URI uri_ = uriComponents.expand(deviceId).toUri();

            // now do the actual PUT request to the spotify API
            RestClient restClient = RestClient.create();

            restClient.put()
                .uri(uri_)
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .body(bodyJson)
                .retrieve()
                .toBodilessEntity();

            // in case is_playing is false we have to make another request to pause thetrack
            if(!is_playing){
                restClient.put()
                    .uri("https://api.spotify.com/v1/me/player/pause")
                    .header("Authorization", authHeader)
                    .retrieve()
                    .toBodilessEntity();
            }

            //RestClient restClient = RestClient.create();

            //Map<String, Object> bodyJson = new HashMap<>();
            //// List<String> uris = List.of(trackUri);
            //bodyJson.put("device_ids", List.of(deviceId));
            //restClient.put()
            //    .uri("https://api.spotify.com/v1/me/player")
            //    .header("Authorization", authHeader_)
            //    .contentType(MediaType.APPLICATION_JSON)
            //    .body(bodyJson)
            //    .retrieve()
            //    .toBodilessEntity();


            String resultStr = "Syncing Done!";
            return CompletableFuture.completedFuture(resultStr);


    }
}

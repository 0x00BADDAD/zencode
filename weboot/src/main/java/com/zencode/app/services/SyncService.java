package com.zencode.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.HashMap;
import java.util.Map;
import com.zencode.app.services.CacheService;

import com.zencode.app.ws.handlers.beans.TrackMetadataBean;
import com.zencode.app.ws.handlers.beans.EmailBean;
import com.zencode.app.shared.SharedTrackMetaDataHolder;




@Service
public class SyncService{

    @Autowired
    CacheService cacheService;

    @Autowired
    private SharedTrackMetaDataHolder trackMetaDataHolder;


    public void syncTrack(EmailBean email_){
        String email = email_.getEmail();

        TrackMetadataBean bean = trackMetaDataHolder.getData();

        String accessToken = cacheService.getAccessToken(email);
        String authHeader = "Bearer " + accessToken;

        // map for json body
        Map<String, Object> bodyJson = new HashMap<>();
        //List<String> uris = List.of(trackUri);
        bodyJson.put("context_uri", bean.getTrackUri());
        bodyJson.put("offset", Map.ofEntries(Map.entry("uri", bean.getResourceUri())));
        bodyJson.put("position_ms", bean.getProgressMs());


        // now do the actual PUT request to the spotify API
        RestClient restClient = RestClient.create();

        restClient.put()
            .uri("https://api.spotify.com/v1/me/player/play")
            .header("Authorization", authHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .body(bodyJson)
            .retrieve()
            .toBodilessEntity();

        // in case is_playing is false we have to make another request to pause thetrack
        if(!bean.getIsPlaying()){
            restClient.put()
                .uri("https://api.spotify.com/v1/me/player/pause")
                .header("Authorization", authHeader)
                .retrieve()
                .toBodilessEntity();
        }


    }


}

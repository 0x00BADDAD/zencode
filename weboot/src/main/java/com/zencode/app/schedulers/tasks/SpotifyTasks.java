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
import com.zencode.app.services.RedisCacheService;

import org.springframework.web.bind.annotation.PathVariable;
import com.zencode.app.shared.SharedTrackMetaDataHolder;
import org.springframework.kafka.core.KafkaTemplate;
import java.util.UUID;

import java.io.PrintWriter;
import java.io.StringWriter;


@Component
public class SpotifyTasks {

    @Autowired
    private SharedTrackMetaDataHolder trackMetaDataHolder;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private KafkaTemplate<String, TrackMetadataBean> KafkaTemplate;



    private static final Logger logger = LogManager.getLogger(SpotifyTasks.class);


    @Autowired
    private MyHandler myHandler;


    @Scheduled(fixedRate = 1200)
    public void fetchCurrSong(){
        try{

            while(!myHandler.getFreshSessions().isEmpty()){
                myHandler.getFreshSessions().forEach((key, value)-> {
                    logger.debug("***************startingTask for sessionId: {}", value);
                    myHandler.startTask(value);
                    myHandler.getFreshSessions().remove(key);
                });
            }


            String accessToken = cacheService.getAccessToken("admin");
            RestClient restClient = RestClient.create();
           // TODO: proper error handling on all scheduled tasks
            String authHeader = "Bearer " + accessToken;
            logger.debug("requesting track metadata from spotify!!");
            JsonNode root = restClient.get()
                .uri("https://api.spotify.com/v1/me/player/currently-playing")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", authHeader)
                .retrieve()
                .body(JsonNode.class);
            if (root != null){

                String context = root.path("context").path("type").asText();
                boolean isShow = true;
                if(context != null){
                    isShow = context.equals("show");
                    logger.debug("---------------the context of spotify track is: {}", context);
                }
                if(isShow){
                    TrackMetadataBean showBean = new TrackMetadataBean("It seems Atharv is listening to a podcast!", List.of(), "No-track", "No-resource", 0, 0, false, 0, true, "No-device-active", "No-img-url", false, false, false, "");
                    trackMetaDataHolder.setData(showBean);
                    myHandler.broadcast(showBean);
                    return;
                }

                String trackHref = root.path("item").path("href").asText();
                String resourceUri = root.path("item").path("album").path("uri").asText();
                String trackUri = root.path("item").path("uri").asText();
                Integer discNumber = root.path("item").path("track_number").asInt();
                Integer duration_ms = root.path("item").path("duration_ms").asInt();
                Integer progress_ms = root.path("progress_ms").asInt();
                boolean isPlaying = root.path("is_playing").asBoolean();
                String deviceId = ""; // not needed
                String songName = root.path("item").path("name").asText();

                String[] uriParts = trackUri.split(":");
                logger.debug("the songName is: {}", songName);
                logger.debug("The type of Spotify URI received is: " + trackUri);
                if(trackUri.equals("")){
                    TrackMetadataBean showBean = new TrackMetadataBean("It seems Atharv is listening to a podcast!", List.of(), "No-track", "No-resource", 0, 0, false, 0, true, "No-device-active", "No-img-url", false, false, false, "");
                    trackMetaDataHolder.setData(showBean);
                    myHandler.broadcast(showBean);
                    return;
                }

               // String songName = root_.path("name").asText();
                List<String> artistsAll = new ArrayList<>();

               // JsonNode artists = root_.path("artists");
                JsonNode artists = root.path("item").path("artists");

                if (artists.isArray()){
                    for (JsonNode artist: artists){
                        String artistName = artist.path("name").asText();
                        artistsAll.add(artistName);
                    }
                }

                JsonNode images = root.path("item").path("album").path("images");
                // we take the first image only
                String imgUrl = images.get(0).path("url").asText();
                logger.debug("Got the album image url and it is: " + imgUrl);

                TrackMetadataBean trackMetadataBean = new TrackMetadataBean(songName, artistsAll, trackUri, resourceUri, progress_ms, duration_ms, isPlaying, discNumber, true, deviceId, imgUrl, false, false, false, "");
                //logger.debug("Song Name: "+ songName + " Artists: "+ artistsAll.toString());
                logger.debug("Bean from spotify is: " + trackMetadataBean.toString());
                trackMetaDataHolder.setData(trackMetadataBean);
                myHandler.broadcast(trackMetadataBean);
                //UUID uniqueId = UUID.randomUUID();
                //KafkaTemplate.send("spotify-track-topic", uniqueId.toString(), trackMetadataBean);

            }else{
                logger.debug("No song playing right now!");
                TrackMetadataBean emptyBean = new TrackMetadataBean("No music playing right now!", List.of(), "No-track", "No-resource", 0, 0, false, 0, true, "No-device-active", "No-img-url", false, false, false, "");
                trackMetaDataHolder.setData(emptyBean);
                myHandler.broadcast(emptyBean);
                //UUID uniqueId = UUID.randomUUID();
                //KafkaTemplate.send("spotify-track-topic", uniqueId.toString(), emptyBean);

            }
        }catch (Exception e){
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();
            // after error occurs when trying to fetch the remote playback state, we broadcast a err bean
            TrackMetadataBean errBean = new TrackMetadataBean("No music playing right now!", List.of(), "No-track", "No-resource", 0, 0, false, 0, true, "No-device-active", "No-img-url", false, false, true, stackTrace);
            trackMetaDataHolder.setData(errBean);
            myHandler.broadcast(errBean);

        }
    }

}

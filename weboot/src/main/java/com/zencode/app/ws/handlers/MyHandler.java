package com.zencode.app.ws.handlers;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.zencode.app.shared.SharedTrackMetaDataHolder;
import com.zencode.app.ws.handlers.beans.TrackMetadataBean;
import com.zencode.app.ws.handlers.beans.EmailBean;

import com.zencode.app.services.SyncService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import java.util.Base64;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.ArrayList;

import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;

import com.zencode.app.services.RedisCacheService;


public class MyHandler extends TextWebSocketHandler {

    @Autowired
    SyncService syncService;

    //@Autowired
    //DynamicTaskService dynamicTaskService;

    //@Autowired
    //FetchRemotePlaybackService fetchRemotePlaybackService;

    @Autowired
    private SharedTrackMetaDataHolder trackMetaDataHolder;

    @Autowired
    private RedisCacheService cacheService;


    @Autowired
    private ThreadPoolTaskScheduler scheduler;

    private final Map<String, ScheduledFuture<?>> activeTasks = new ConcurrentHashMap<>();


    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(MyHandler.class);



    // Start a new repeating task with an ID
    public void startTask(String sessionId) {
        if (activeTasks.containsKey(sessionId)) {
            System.out.println("Task " + sessionId + " already running.");
            return;
        }

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                () -> fetchRemotePlaybackTask(sessionId),
                TimeUnit.SECONDS.toMillis(2) // run every 2s
        );

        activeTasks.put(sessionId, future);
        System.out.println("Task " + sessionId + " started.");
    }

    // Stop and remove a task
    public void stopTask(String sessionId) {
        ScheduledFuture<?> future = activeTasks.remove(sessionId);
        if (future != null) {
            future.cancel(true);
            System.out.println("Task " + sessionId + " stopped.");
        } else {
            System.out.println("Task " + sessionId + " not found.");
        }
    }


    public void fetchRemotePlaybackTask(String sessionId){
            logger.debug("sessionId used for fetchRemotePlaybackTask is " + sessionId);
            String accessToken = cacheService.getAccessToken(sessionId);
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
                }

                if(isShow){
                    TrackMetadataBean showBean = new TrackMetadataBean("It seems Atharv is listening to a podcast!", List.of(), "No-track", "No-resource", 0, 0, false, 0, true, "No-device-active", "No-img-url", false, false);
                    broadcast(showBean);
                    return;
                }

                String trackHref = root.path("item").path("href").asText();
                String resourceUri = root.path("item").path("album").path("uri").asText();
                String trackUri = root.path("item").path("uri").asText();
                Integer discNumber = root.path("item").path("track_number").asInt();
                Integer bulkNumber = root.path("item").path("disc_number").asInt();
                Integer duration_ms = root.path("item").path("duration_ms").asInt();
                Integer progress_ms = root.path("progress_ms").asInt();
                boolean isPlaying = root.path("is_playing").asBoolean();


                boolean isPlaylist = context.equals("playlist");
                boolean isAlbum = context.equals("album");
                boolean isArtist = context.equals("artist");

               boolean canSkipPrev = false;
                if(isAlbum){
                    logger.debug("%%%%%%%%%%   was here in the isAlbum path");
                    canSkipPrev = !(bulkNumber.equals(1) && discNumber.equals(1));
                }
                if(isPlaylist || isArtist){
                    // get the first page of the playlist tracks
                    // unfortunately spotify public web api doesn't allow to access the playlist data
                    // that are generated by spotify recommendation system. so i am disbaling
                    // skip previous on every playlist track. this is a spotify API pitfall! not mine!
                    // had to say it.
                    canSkipPrev = false;
                }

                //String deviceId = root.path("device").path("id").asText();
                String deviceId = cacheService.getDeviceId(sessionId);

                String name = root.path("device").path("name").asText();
                String songName = root.path("item").path("name").asText();

                String[] uriParts = trackUri.split(":");
                logger.debug("The type of Spotify URI for the old playback received is: " + uriParts[1] + " and the device_id is: " + deviceId + " and the name value is " + name);

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

                TrackMetadataBean currTrackMetaData = trackMetaDataHolder.getData();

                boolean isInSync = (currTrackMetaData.getTrackUri().equals(trackUri) && (currTrackMetaData.getProgressMs().intValue() - progress_ms.intValue()) < 6000 && currTrackMetaData.getIsPlaying() == isPlaying);

                TrackMetadataBean trackMetadataBean = new TrackMetadataBean(songName, artistsAll, trackUri, resourceUri, progress_ms, duration_ms, isPlaying, discNumber, false, deviceId, imgUrl, canSkipPrev, isInSync);
                //logger.debug("Song Name: "+ songName + " Artists: "+ artistsAll.toString());
                logger.debug("Bean from spotify is: " + trackMetadataBean.toString());
                //trackMetaDataHolder.setData(trackMetadataBean);
                broadcast(trackMetadataBean);
            }else{
                logger.debug("No song playing right now!");
                TrackMetadataBean emptyBean = new TrackMetadataBean("No music playing right now!", List.of(), "No-track", "No-resource", 0, 0, false, 0, false, "No-device-active", "No-img-url", false, false);
                //trackMetaDataHolder.setData(emptyBean);
                broadcast(emptyBean);
            }
            // try to get the array of all available devices for the current user...
            JsonNode root_ = restClient.get()
                .uri("https://api.spotify.com/v1/me/player/devices")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", authHeader)
                .retrieve()
                .body(JsonNode.class);
            JsonNode devices = root_.path("devices");
            List<String> allDeviceIds = new ArrayList<>();
            for(JsonNode device: devices){
                String id = device.path("id").asText();
                allDeviceIds.add(id);
            }
            logger.debug("The device ids are: " + allDeviceIds);

    }



    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sessionId = (String) session.getAttributes().get("session_id");

        logger.debug("got a connection to the web socket endpoint!");
        WebSocketSession safeSession = new ConcurrentWebSocketSessionDecorator(
            session,
            20_000,  // send timeout in ms
            1024     // buffer size in bytes
        );
        if(sessionId != null){
            logger.debug("conn setup for session_id: " + sessionId);
            startTask(sessionId);
        }
        sessions.put(session.getId(), safeSession);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = (String) session.getAttributes().get("session_id");
        // Remove session
        sessions.remove(session.getId());
        if(sessionId != null){
            stopTask(sessionId);
        }
        logger.debug("Session removed: " + session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message){
        logger.debug("Recd a message from client on session: "+ session.getId() + " " + message.getPayload());
        String json = message.getPayload();

        try{
            EmailBean email =  objectMapper.readValue(json , EmailBean.class);
            syncService.syncTrack(email);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public void broadcast(TrackMetadataBean message) {
        //logger.debug("broadcasting message to clients with count " + sessions.size());

        for (WebSocketSession session : sessions.values()) {
            try {
                String json = objectMapper.writeValueAsString(message);
                if (session.isOpen()){
                    session.sendMessage(new TextMessage(json)); // no need to synchronize
                }else{
                    logger.debug("Session is stale!!!");
                }
            } catch (IOException e) {
                // log and remove dead sessions
            }
        }
    }
}


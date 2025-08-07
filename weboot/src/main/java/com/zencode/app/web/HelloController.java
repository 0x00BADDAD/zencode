package com.zencode.app.web;

import com.zencode.app.web.RespClass;
import com.zencode.app.web.TokRespClass;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import com.zencode.app.services.ActorService;
import com.zencode.app.dao.beans.Actor;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.SecureRandom;
import java.util.Base64;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.web.bind.support.SessionStatus;

import com.fasterxml.jackson.annotation.JsonView;
import java.util.concurrent.Callable;

import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.zencode.app.web.UserData;
import com.github.benmanes.caffeine.cache.Cache;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import com.zencode.app.services.CacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import com.zencode.app.web.ReqBody;
import java.util.List;
import java.util.ArrayList;


import com.zencode.app.ws.handlers.beans.TrackMetadataBean;
import org.springframework.web.bind.annotation.PathVariable;
import com.zencode.app.shared.SharedTrackMetaDataHolder;



@Controller
@SessionAttributes("csrfToken")
public class HelloController {
    //@Autowired
    //private ActorService actorService;

    @Autowired
    private SharedTrackMetaDataHolder trackMetaDataHolder;

    @Autowired
    private CacheService cacheService;

    private static final Logger logger = LogManager.getLogger(HelloController.class);


    @GetMapping("/api/hello")
    public String handleHello(Model model) {
        return "hello-world";  // resolved as hello.html in templates directory
    }

    @GetMapping("/api/spotify_login_once")
    public String spotifyLoginOnce(@SessionAttribute(value = "csrfToken", required= false) String csrfToken, @RequestParam(value = "code", required = false) String code, @RequestParam(value = "state", required = false) String csrfTokenRecd, Model model, SessionStatus status){
        if (csrfToken == null){
            // to redirect the client to the spotify API
            String clientId = "9469751d45ca49cea94be50c071a3c65";
            String redirectUri = "http://127.0.0.1:3000/api/spotify_login_once";
            SecureRandom sr = new SecureRandom();
            byte[] bytes = new byte[16];
            sr.nextBytes(bytes);
            String csrfTokenProd = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            model.addAttribute("csrfToken", csrfTokenProd);

            return "redirect:" + "https://accounts.spotify.com/authorize?client_id=" +clientId+ "&response_type=code" + "&redirect_uri="+redirectUri+"&scope=user-read-email user-modify-playback-state user-read-playback-state user-read-currently-playing streaming user-read-private"+"&state="+csrfTokenProd;
            
        }else {
            logger.debug("redirected to the spotify_login_once once again!");

            if (!csrfToken.equals(csrfTokenRecd)){
                // the csrf token recd back from spotify server is not same as generated at my backend.
                status.setComplete();
                return "error-page"; // TODO: Implement this template
            }
            status.setComplete(); // clearing session of the temp csrfToken

            String clientId = "9469751d45ca49cea94be50c071a3c65";
            String clientSecret = "6139b2de2c564d9a977f34c3b27fbda4";



            String inputString = clientId + ":" + clientSecret;
            byte[] utf8Bytes = inputString.getBytes(StandardCharsets.UTF_8);
            String base64String = Base64.getEncoder().encodeToString(utf8Bytes);
            String authHeader = "Basic " + base64String;


            RestClient restClient = RestClient.create();

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("code", code);
            formData.add("grant_type", "authorization_code");
            formData.add("redirect_uri", "http://127.0.0.1:3000/api/spotify_login_once");

            RespClass resp = restClient.post()
                .uri("https://accounts.spotify.com/api/token")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(RespClass.class);
            logger.debug("resp is retrived and is: %s".format(resp.toString()));
            // To store the refresh token and access token for this user.
            String accessToken = resp.getAccessToken();
            String refreshToken = resp.getRefreshToken();
            logger.debug("Refresh Token with playback rights!!!!! copy this ASAP    " + refreshToken);

            String accessTokenHeader = "Bearer " + accessToken;

            JsonNode root = restClient.get()
                .uri("https://api.spotify.com/v1/me")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", accessTokenHeader)
                .retrieve()
                .body(JsonNode.class);

            String email = root.path("email").asText();

            cacheService.setRefreshToken(email, refreshToken);
            cacheService.setAccessToken(email, accessToken);



            model.addAttribute("userGrantedPermission", true);
            model.addAttribute("accessToken", accessToken);
            model.addAttribute("userEmail", email);
            TrackMetadataBean initialTrackMetaData = trackMetaDataHolder.getData();
            logger.debug("intialTrackMetaData is: " +initialTrackMetaData.toString());



//            // TODO: Very verbose I am making yet another network call in order to get the initial Track data
//            accessToken = cacheService.getAccessToken("admin");
//            //RestClient restClient = RestClient.create();
//            // TODO: proper error handling on all scheduled tasks
//            authHeader = "Bearer " + accessToken;
//            logger.debug("requesting track metadata from spotify in order to include it in front end!!");
//            root = restClient.get()
//                .uri("https://api.spotify.com/v1/me/player/currently-playing")
//                .accept(MediaType.APPLICATION_JSON)
//                .header("Authorization", authHeader)
//                .retrieve()
//                .body(JsonNode.class);
//
//
//            if (root != null){
//                String trackHref = root.path("item").path("href").asText();
//                String trackUri = root.path("context").path("uri").asText();
//                Integer discNumber = root.path("item").path("disc_number").asInt();
//                Integer progress_ms = root.path("progress_ms").asInt();
//                boolean isPlaying = root.path("is_playing").asBoolean();
//
//
//
//                String[] uriParts = trackUri.split(":");
//                logger.debug("The type of Spotify URI received is: " + uriParts[1]);
//
//                JsonNode root_ = restClient.get()
//                   .uri(trackHref)
//                   .accept(MediaType.APPLICATION_JSON)
//                   .header("Authorization", authHeader)
//                   .retrieve()
//                   .body(JsonNode.class);
//
//                String songName = root_.path("name").asText();
//                List<String> artistsAll = new ArrayList<>();
//
//                JsonNode artists = root_.path("artists");
//                if (artists.isArray()){
//                    for (JsonNode artist: artists){
//                        String artistName = artist.path("name").asText();
//                        artistsAll.add(artistName);
//                    }
//                }
//                TrackMetadataBean trackMetadataBean = new TrackMetadataBean(songName, artistsAll, trackUri, progress_ms, isPlaying, discNumber);
//                logger.debug("Song Name: "+ songName + " Artists: "+ artistsAll.toString());
//                model.addAttribute("initialTrackMetaData", trackMetadataBean);
//                //myHandler.broadcast(trackMetadataBean);
//            }else{
//                logger.debug("No song playing right now! hence passing a rather empty object to frontend");
//                model.addAttribute("initialTrackMetaData", new TrackMetadataBean("No music playing right now!", List.of(), "No-track", 0, false, 0));
//            }

            model.addAttribute("initialTrackMetaData", initialTrackMetaData);

            return "hello-world";
        }
}

    @GetMapping("/api/fresh_token")
    @ResponseBody
    @JsonView(TokRespClass.TokRespClassView.class)
    public TokRespClass handleFetchAccessToken(@RequestParam("email") String email){
            String accessToken = cacheService.getAccessToken(email);
            return new TokRespClass(accessToken);
    }



    @PostMapping("/api/transfer_playback")
    @ResponseBody
    @JsonView(ReqBody.ReqBodyView.class)
    public ReqBody handleTransferPlayback(@RequestHeader("X-Token") String accessToken, @RequestBody ReqBody reqBody){
        RestClient restClient = RestClient.create();
        String authHeader = "Bearer " + accessToken;
        logger.debug("the req body recd from frontend is: " + reqBody.toString());

        restClient.put()
            .uri("https://api.spotify.com/v1/me/player")
            .header("Authorization", authHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .body(reqBody)
            .retrieve()
            .toBodilessEntity();
        return reqBody;

    }

    @GetMapping("/api/play_track")
    public ResponseEntity<Void> handlePlayTrack(@RequestParam("track_uri") String trackUri, @RequestParam("position") Integer position_ms, @RequestParam("email") String email, @RequestParam("is_playing") boolean is_playing, @RequestParam("disc_number") Integer discNumber){
        String accessToken = cacheService.getAccessToken(email);
        String authHeader = "Bearer " + accessToken;

        // map for json body
        Map<String, Object> bodyJson = new HashMap<>();
        //List<String> uris = List.of(trackUri);
        bodyJson.put("context_uri", trackUri);
        bodyJson.put("offset", Map.ofEntries(Map.entry("position", discNumber-1)));
        bodyJson.put("position_ms", position_ms);


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
        if(!is_playing){
            restClient.put()
                .uri("https://api.spotify.com/v1/me/player/pause")
                .header("Authorization", authHeader)
                .retrieve()
                .toBodilessEntity();
        }

        return ResponseEntity.ok()
                .build(); // empty body response

    }

    @GetMapping("/api/next_track")
    public ResponseEntity<Void> handleNextTrack(@RequestParam("email") String email){
        logger.debug("The email received in reuqest params is: "+ email);
        RestClient restClient = RestClient.create();
        String accessToken = cacheService.getAccessToken(email);
        restClient.post()
            .uri("https://api.spotify.com/v1/me/player/next")
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .toBodilessEntity();


        return ResponseEntity.ok()
                .build();
    }


    @GetMapping("/api/{id}")
    public ResponseEntity<Map<String, Object>> handleIdGen(@PathVariable String id, @RequestParam("email") String email){
        String accessToken = cacheService.getAccessToken(email);


        RestClient restClient = RestClient.create();
        JsonNode root = restClient.get()
                            .uri("https://api.spotify.com/v1/tracks/" + id )
                            .header("Authorization", "Bearer " + accessToken)
                            .accept(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .body(JsonNode.class);

        Integer track_number = root.path("track_number").asInt();

        Map<String, Object> jsonBody = new HashMap<>();
        jsonBody.put("disc_number", track_number);

        return ResponseEntity.ok(jsonBody);
    }



   // @GetMapping("/api/pause_track")
   // public ResponseEntity<Void> handlePlayTrack(){
   //     String accessToken = cacheService.getAccessToken(email);
   //     String authHeader = "Bearer " + accessToken;

   //     // map for json body
   //     Map<String, Object> bodyJson = new HashMap<>();
   //     bodyJson.put("context_uri", trackUri);
   //     bodyJson.put("postion_ms", position_ms);


   //     // now do the actual PUT request to the spotify API
   //     RestClient restClient = RestClient.create();

   //     restClient.put()
   //         .uri("https://api.spotify.com/v1/me/player/play")
   //         .header("Authorization", authHeader)
   //         .contentType(MediaType.APPLICATION_JSON)
   //         .body(bodyJson)
   //         .retrieve()
   //         .toBodilessEntity();

   //     return ResponseEntity.ok()
   //             .build(); // empty body response

   // }


    @GetMapping("/api/spotify_login_success")
    public RespClass handleSpotifyLoginSuccess(@SessionAttribute String csrfToken, @RequestParam("code") String authCode, @RequestParam("state") String csrfTokenRecd, SessionStatus status, Model model){
        if (!csrfToken.equals(csrfTokenRecd)){
            // the csrf token recd back from spotify server is not same as generated at my backend.
            status.setComplete();
            return new RespClass(); // TODO: Implement this template
        }
        status.setComplete(); // clearing session of the temp csrfToken
        

        String clientId = "9469751d45ca49cea94be50c071a3c65";
        String clientSecret = "6139b2de2c564d9a977f34c3b27fbda4";



        String inputString = clientId + ":" + clientSecret;
        byte[] utf8Bytes = inputString.getBytes(StandardCharsets.UTF_8);
        String base64String = Base64.getEncoder().encodeToString(utf8Bytes);
        String authHeader = "Basic " + base64String;

        RestClient restClient = RestClient.create();


        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", authCode);
        formData.add("grant_type", "authorization_code");
        formData.add("redirect_uri", "http://127.0.0.1:3000/api/spotify_login_success");

        RespClass resp = restClient.post()
            .uri("https://accounts.spotify.com/api/token")
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", authHeader)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(formData)
            .retrieve()
            .body(RespClass.class);
        logger.debug("resp is retrived and is: %s".format(resp.toString()));
    

        return resp;
    }

    @PostMapping("/api/token")
    @ResponseBody
    @JsonView(RespClass.TokRespJsonView.class)
    public RespClass handleTokenRequest(@RequestParam("code") String code, @RequestParam("grant_type") String grant_type, @RequestParam("redirect_uri") String redirect_uri, @RequestHeader("Authorization") String authString){
            RestClient restClient = RestClient.create();


            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("code", code);
            formData.add("grant_type", grant_type);
            formData.add("redirect_uri", redirect_uri);

            RespClass resp = restClient.post()
                .uri("https://accounts.spotify.com/api/token")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", authString)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(RespClass.class);
            logger.debug("resp is retrived and is: %s".format(resp.toString()));
            return resp;
    }

}


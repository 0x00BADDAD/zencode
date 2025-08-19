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
import org.springframework.cache.CacheManager;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import com.zencode.app.services.CacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import com.zencode.app.web.ReqBody;
import java.util.List;
import java.util.ArrayList;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;

import com.zencode.app.ws.handlers.beans.TrackMetadataBean;
import org.springframework.web.bind.annotation.PathVariable;
import com.zencode.app.shared.SharedTrackMetaDataHolder;
import com.zencode.app.services.RedisCacheService;


@Controller
@SessionAttributes("csrfToken")
public class HelloController {

    @Autowired
    private SharedTrackMetaDataHolder trackMetaDataHolder;

    @Autowired
    private RedisCacheService cacheService;


    private static final Logger logger = LogManager.getLogger(HelloController.class);


    @GetMapping("/api/hello")
    public String handleHello(HttpServletRequest request, Model model) {
        // if I do have the required cookies should redirect to the spotify login once
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            return "redirect:/api/spotify_login_once";
        }

        return "hello-world";  // resolved as hello.html in templates directory
    }

    @GetMapping("/api/spotify_login_once")
    public String spotifyLoginOnce(HttpServletRequest request, HttpServletResponse response, @SessionAttribute(value = "csrfToken", required= false) String csrfToken, @RequestParam(value = "code", required = false) String code, @RequestParam(value = "state", required = false) String csrfTokenRecd, Model model, SessionStatus status){

            // check if we got the cookies set and if we do, we also do (may be) a redis check
            Cookie[] cookies = request.getCookies();
            if(cookies != null){
                logger.debug("cookies weren't null????");
                String sessionId = null;
                String email = null;
                for (Cookie cookie : cookies) {
                    if ("session_id".equals(cookie.getName())) {
                        sessionId = cookie.getValue();
                    }
                    //if("email".equals(cookie.getName())){
                    //    email = cookie.getValue();
                    //}
                }

                if(sessionId != null){
                    if(cacheService.checkSessionId(sessionId)){
                        // sessionId is present in redis
                        String accessToken = cacheService.getAccessToken(sessionId);
                        model.addAttribute("userGrantedPermission", true);
                        model.addAttribute("accessToken", accessToken);
                        model.addAttribute("sessionId", sessionId);
                        TrackMetadataBean initialTrackMetaData = trackMetaDataHolder.getData();
                        logger.debug("intialTrackMetaData when session exists!! is: " +initialTrackMetaData.toString());
                        model.addAttribute("initialTrackMetaData", initialTrackMetaData);
                        return "hello-world";
                    }
                }
            }


        if (csrfToken == null){
            // to redirect the client to the spotify API
            String clientId = "9469751d45ca49cea94be50c071a3c65";
            String redirectUri = "http://127.0.0.1:3000/api/spotify_login_once";
            SecureRandom sr = new SecureRandom();
            byte[] bytes = new byte[16];
            sr.nextBytes(bytes);
            String csrfTokenProd = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            model.addAttribute("csrfToken", csrfTokenProd);

            return "redirect:https://accounts.spotify.com/authorize?client_id=" +clientId+ "&response_type=code" + "&redirect_uri="+redirectUri+"&scope=user-read-email user-modify-playback-state user-read-playback-state user-read-currently-playing streaming user-read-private"+"&state="+csrfTokenProd;
            
        } else {
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

            // creating and storing the sessionId in the redis store/cache
            String sessionId = csrfToken + "@" + email;
            cacheService.setSessionId(sessionId);

            cacheService.setRefreshToken(sessionId, refreshToken);
            cacheService.setAccessToken(sessionId, accessToken);

            // set cookies here with max-age 5 days => 5 * 24 * 3600 sec
            Cookie cookie = new Cookie("session_id", sessionId);
            cookie.setMaxAge(5 * 24 * 3600);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

            // set cookies here with max-age 5 days => 5 * 24 * 3600 sec
            //Cookie cookie_ = new Cookie("email", email);
            //cookie_.setMaxAge(5 * 24 * 3600);
            //cookie_.setPath("/");
            //cookie_.setHttpOnly(true);
            //response.addCookie(cookie_);

            // set model attributes
            model.addAttribute("userGrantedPermission", true);
            model.addAttribute("accessToken", accessToken);
            model.addAttribute("sessionId", sessionId);
            TrackMetadataBean initialTrackMetaData = trackMetaDataHolder.getData();
            logger.debug("intialTrackMetaData is: " +initialTrackMetaData.toString());
            model.addAttribute("initialTrackMetaData", initialTrackMetaData);

            return "hello-world";
        }
}

    @GetMapping("/api/fresh_token")
    @ResponseBody
    @JsonView(TokRespClass.TokRespClassView.class)
    public TokRespClass handleFetchAccessToken(@RequestParam("session_id") String sessionId){
            String accessToken = cacheService.getAccessToken(sessionId);
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
    public ResponseEntity<Void> handlePlayTrack(@RequestParam("track_uri") String trackUri, @RequestParam("resource_uri") String resource_uri, @RequestParam("position") Integer position_ms, @RequestParam("session_id") String sessionId, @RequestParam("is_playing") boolean is_playing, @RequestParam("disc_number") Integer discNumber){

        String accessToken = cacheService.getAccessToken(sessionId);
        String authHeader = "Bearer " + accessToken;

        // map for json body
        Map<String, Object> bodyJson = new HashMap<>();
        //List<String> uris = List.of(trackUri);
        bodyJson.put("context_uri", trackUri);
        bodyJson.put("offset", Map.ofEntries(Map.entry("uri", resource_uri)));
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
    public ResponseEntity<Void> handleNextTrack(@RequestParam("session_id") String sessionId){
        logger.debug("The sessionID received in request params is: " + sessionId);
        RestClient restClient = RestClient.create();
        String accessToken = cacheService.getAccessToken(sessionId);
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


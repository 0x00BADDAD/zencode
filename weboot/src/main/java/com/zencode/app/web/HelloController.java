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
import com.zencode.app.services.EligiCheckService;

import com.zencode.app.web.TrackMetadataBeanWeb;

import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpStatus;


@Controller
@SessionAttributes("csrfToken")
public class HelloController {

    @Autowired
    private SharedTrackMetaDataHolder trackMetaDataHolder;

    @Autowired
    private RedisCacheService cacheService;

    @Autowired
    private EligiCheckService checkService;

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
                String isEligible = null;
                for (Cookie cookie : cookies) {
                    if ("session_id".equals(cookie.getName())) {
                        sessionId = cookie.getValue();
                    }
                    if("is_eligible".equals(cookie.getName())){
                       isEligible = cookie.getValue();
                    }
                }

                if(sessionId != null){
                    if(isEligible == null || isEligible.equals("false")){
                        boolean isEligibleNew = checkService.check(sessionId);
                        //model.addAttribute("isEligible", isEligibleNew);
                        isEligible = String.valueOf(isEligibleNew);

                        // updating the already set cookie on HttpServletResponse
                        Cookie cookie_ = new Cookie("is_eligible", String.valueOf(isEligibleNew));
                        cookie_.setMaxAge(5 * 24 * 3600);
                        cookie_.setPath("/");
                        cookie_.setHttpOnly(true);
                        response.addCookie(cookie_);
                    }

                    if(cacheService.checkSessionId(sessionId)){
                        logger.debug("was here sessionId is in redis...");
                        // sessionId is present in redis
                        String accessToken = cacheService.getAccessToken(sessionId);
                        String deviceId = cacheService.getDeviceId(sessionId);

                        model.addAttribute("userGrantedPermission", true);
                        model.addAttribute("accessToken", accessToken);
                        model.addAttribute("isEligible", Boolean.parseBoolean(isEligible));
                        model.addAttribute("sessionId", sessionId);
                        model.addAttribute("deviceId", deviceId);
                        TrackMetadataBean initialTrackMetaData = trackMetaDataHolder.getData();
                        logger.debug("intialTrackMetaData when session exists!! is: " +initialTrackMetaData.toString());
                        model.addAttribute("initialTrackMetaData", initialTrackMetaData);
                        return "hello-world";
                    }
                }else{
                    logger.debug("sessionId was null!?!?!?! Despite cookies being non null????");
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

            return "redirect:https://accounts.spotify.com/authorize?client_id=" +clientId+ "&response_type=code" + "&redirect_uri="+redirectUri+"&scope=user-read-email user-modify-playback-state user-read-playback-state user-read-currently-playing streaming user-read-private playlist-read-private playlist-read-collaborative"+"&state="+csrfTokenProd;

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
            logger.debug("Refresh Token with playback rights!!!!! copy this ASAP " + refreshToken);

            String accessTokenHeader = "Bearer " + accessToken;

            JsonNode root = restClient.get()
                .uri("https://api.spotify.com/v1/me")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", accessTokenHeader)
                .retrieve()
                .body(JsonNode.class);

            String email = root.path("email").asText();

            String product = root.path("product").asText();
            boolean isPremium = product.equals("premium");

            // creating and storing the sessionId in the redis store/cache
            String sessionId = csrfToken + "@" + email;
            cacheService.setSessionId(sessionId);

            cacheService.setRefreshToken(sessionId, refreshToken);
            cacheService.setAccessToken(sessionId, accessToken);

            String deviceId = cacheService.getDeviceId(sessionId);

            // set cookies here with max-age 5 days => 5 * 24 * 3600 sec
            Cookie cookie = new Cookie("session_id", sessionId);
            cookie.setMaxAge(5 * 24 * 3600);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

            Cookie cookie_ = new Cookie("is_eligible", String.valueOf(isPremium));
            cookie_.setMaxAge(5 * 24 * 3600);
            cookie_.setPath("/");
            cookie_.setHttpOnly(true);
            response.addCookie(cookie);
            // set cookies here with max-age 5 days => 5 * 24 * 3600 sec
            //Cookie cookie_ = new Cookie("email", email);
            //cookie_.setMaxAge(5 * 24 * 3600);
            //cookie_.setPath("/");
            //cookie_.setHttpOnly(true);
            //response.addCookie(cookie_);

            // set model attributes
            model.addAttribute("userGrantedPermission", true);
            model.addAttribute("isEligible", isPremium);
            model.addAttribute("accessToken", accessToken);
            model.addAttribute("sessionId", sessionId);
            model.addAttribute("deviceId", deviceId);
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



    @GetMapping("/api/transfer_playback")
    @ResponseBody
    @JsonView(ReqBody.ReqBodyView.class)
    public ReqBody handleTransferPlayback(@RequestParam("device_id") String deviceId, @CookieValue("session_id") String sessionId){
        logger.debug("Ahoy! transferrer the device id recd was: " + deviceId + " and the sessionId was " + sessionId);
       // String authHeader = "";
       // String deviceId = null;
        RestClient restClient = RestClient.create();
       // if(accessToken != null){
       //     authHeader = "Bearer " + accessToken;
       // }else if(sessionId != null){
       String accTok = cacheService.getAccessToken(sessionId);
       //     deviceId = cacheService.getDeviceId(sessionId);
       String authHeader = "Bearer " + accTok;
       // }
        //logger.debug("the req body recd from frontend is: " + reqBody.toString());
       // if(deviceId != null && !deviceId.equals("No-device-active")){
        Map<String, Object> reqBody = Map.of(
            "device_ids", List.of(deviceId),
            "play", false
        );
        restClient.put()
            .uri("https://api.spotify.com/v1/me/player")
            .header("Authorization", authHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .body(reqBody)
            .retrieve()
            .toBodilessEntity();
       //}else{
       //    logger.debug("NO deviceId for the user...................");
       //}
        return new ReqBody();

    }

    @GetMapping("/api/tracks")
    @ResponseBody
    @JsonView(RespClass.DurationView.class)
    public RespClass handleDurationTrack(@RequestParam("session_id") String sessionId, @RequestParam("track_id") String trackId){

        logger.debug("The trackId received is: " + trackId);
        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString("https://api.spotify.com/v1/tracks/{track_id}")
                .encode()
                .build();

        URI uri_ = uriComponents.expand(trackId).toUri();


        String accessToken = cacheService.getAccessToken(sessionId);
        String authHeader = "Bearer " + accessToken;

        RestClient restClient = RestClient.create();
        JsonNode resp = restClient.get()
            .uri(uri_)
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", authHeader)
            .retrieve()
            .body(JsonNode.class);

        Integer durationMs = resp.path("duration_ms").asInt();
        RespClass respJson = new RespClass();
        respJson.setDurationMs(durationMs);
        return respJson;
    }


    @GetMapping("/api/play_track")
    public ResponseEntity<Void> handlePlayTrack(@RequestParam("track_uri") String trackUri, @RequestParam("resource_uri") String resource_uri, @RequestParam("position") Integer position_ms, @RequestParam("session_id") String sessionId, @RequestParam("is_playing") boolean is_playing, @RequestParam("disc_number") Integer discNumber, @RequestParam("device_id") String deviceId){

        String accessToken = cacheService.getAccessToken(sessionId);
        String authHeader = "Bearer " + accessToken;

        // map for json body
        Map<String, Object> bodyJson = new HashMap<>();
        //List<String> uris = List.of(trackUri);
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

        return ResponseEntity.ok()
                .build(); // empty body response

    }

    @GetMapping("/api/next_track")
    @ResponseBody
    @JsonView(TrackMetadataBeanWeb.TrackMetadataJsonView.class)
    public TrackMetadataBeanWeb handleNextTrack(@RequestParam("session_id") String sessionId){
        logger.debug("The sessionID received in request params is: " + sessionId);
        RestClient restClient = RestClient.create();
        String accessToken = cacheService.getAccessToken(sessionId);
        //restClient.post()
        //    .uri("https://api.spotify.com/v1/me/player/next")
        //    .header("Authorization", "Bearer " + accessToken)
        //    .retrieve()
        //    .toBodilessEntity();
        try {
            restClient.post()
                .uri("https://api.spotify.com/v1/me/player/next")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            // This block catches all 4xx errors (400, 401, 403, 404, etc.)
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                logger.debug("Access denied: 403 Forbidden. Caught it! perhaps at the end of playlist/album");
            } else {
                logger.debug("A client error occurred: " + e.getStatusCode());
            }
            logger.debug("[HC] Something went wrong while doing next track returning empty bean!");
            TrackMetadataBeanWeb emptyBean = new TrackMetadataBeanWeb("No music playing right now!", List.of(), "No-track", "No-resource", 0, 0, false, 0, true, "No-device-active", "No-img-url");
            return emptyBean;
        }

        JsonNode root = restClient.get()
            .uri("https://api.spotify.com/v1/me/player/currently-playing")
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .body(JsonNode.class);


        if (root != null){

            String trackHref = root.path("item").path("href").asText();
            String resourceUri = root.path("item").path("album").path("uri").asText();
            String trackUri = root.path("item").path("uri").asText();
            Integer discNumber = root.path("item").path("track_number").asInt();
            Integer duration_ms = root.path("item").path("duration_ms").asInt();
            Integer progress_ms = root.path("progress_ms").asInt();
            boolean isPlaying = root.path("is_playing").asBoolean();
            String deviceId = ""; // not needed
            String songName = root.path("item").path("name").asText();

            logger.debug("The song name after NEXT TRACK: " +songName);
            String[] uriParts = trackUri.split(":");
            logger.debug("[HelloController] The type of Spotify URI received is: " + uriParts[1]);


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
            logger.debug("[HC after next track]Got the album image url and it is: " + imgUrl);

            TrackMetadataBeanWeb trackMetadataBean = new TrackMetadataBeanWeb(songName, artistsAll, trackUri, resourceUri, progress_ms, duration_ms, isPlaying, discNumber, true, deviceId, imgUrl);
            return trackMetadataBean;
            //logger.debug("Song Name: "+ songName + " Artists: "+ artistsAll.toString());
            //logger.debug("[HC]Bean from spotify is: " + trackMetadataBean.toString());
            //trackMetaDataHolder.setData(trackMetadataBean);
            //myHandler.broadcast(trackMetadataBean);
        }


        logger.debug("[HC] No song playing right now!");
        TrackMetadataBeanWeb emptyBean = new TrackMetadataBeanWeb("No music playing right now!", List.of(), "No-track", "No-resource", 0, 0, false, 0, true, "No-device-active", "No-img-url");
        return emptyBean;
        //trackMetaDataHolder.setData(emptyBean);
        //myHandler.broadcast(emptyBean);

    }

    @GetMapping("/api/prev_track")
    public ResponseEntity<Void> handlePrevTrack(@RequestParam("session_id") String sessionId){
        logger.debug("The sessionID received in request params is: " + sessionId);
        RestClient restClient = RestClient.create();
        String accessToken = cacheService.getAccessToken(sessionId);
        //restClient.post()
        //    .uri("https://api.spotify.com/v1/me/player/previous")
        //    .header("Authorization", "Bearer " + accessToken)
        //    .retrieve()
        //    .toBodilessEntity();
            try {
                restClient.post()
                    .uri("https://api.spotify.com/v1/me/player/previous")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .toBodilessEntity();
            } catch (HttpClientErrorException e) {
                // This block catches all 4xx errors (400, 401, 403, 404, etc.)
                if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                    System.err.println("Access denied: 403 Forbidden. while prev track. perhaps at the start of album");
                } else {
                    System.err.println("A client error occurred: " + e.getStatusCode());
                }
                return ResponseEntity.ok()
                        .build();
            }

        return ResponseEntity.ok()
                .build();
    }

    @GetMapping("/api/pause_track")
    public ResponseEntity<Void> handlePauseTrack(@RequestParam("session_id") String sessionId){
        logger.debug("The sessionID received in request params is: " + sessionId);
        RestClient restClient = RestClient.create();
        String accessToken = cacheService.getAccessToken(sessionId);
        restClient.put()
            .uri("https://api.spotify.com/v1/me/player/pause")
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .toBodilessEntity();

        return ResponseEntity.ok()
                .build();
    }

    @GetMapping("/api/resume_track")
    public ResponseEntity<Void> handleResumeTrack(@RequestParam("session_id") String sessionId, @RequestParam("device_id") String deviceId){
        logger.debug("The sessionID received in request params is: " + sessionId);
        RestClient restClient = RestClient.create();
        String accessToken = cacheService.getAccessToken(sessionId);

        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString("https://api.spotify.com/v1/me/player/play")
                .queryParam("device_id", "{device_id}")
                .encode()
                .build();

        URI uri_ = uriComponents.expand(deviceId).toUri();

        restClient.put()
            .uri(uri_)
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .toBodilessEntity();

        return ResponseEntity.ok()
                .build();
    }

    @GetMapping("/api/seek_track")
    public ResponseEntity<Void> handleSeekTrack(@RequestParam("session_id") String sessionId, @RequestParam("seek_ms") Integer seekMs){
        logger.debug("The sessionID received in request params is: " + sessionId);

        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString("https://api.spotify.com/v1/me/player/seek")
                .queryParam("position_ms", "{q}")
                .encode()
                .build();

        URI uri_ = uriComponents.expand(seekMs).toUri();

        logger.debug("uri for seeking into track: " + uri_.toString());

        RestClient restClient = RestClient.create();
        String accessToken = cacheService.getAccessToken(sessionId);
        restClient.put()
            .uri(uri_)
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .toBodilessEntity();

        return ResponseEntity.ok()
                .build();
    }



    @GetMapping("/api/exp")
    public String handleExpRequest(){
        return "exp-html";
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


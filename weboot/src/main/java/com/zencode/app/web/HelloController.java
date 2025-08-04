package com.zencode.app.web;

import com.zencode.app.web.RespClass;

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


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Controller
@SessionAttributes("csrfToken")
public class HelloController {
    @Autowired
    private ActorService actorService;


    private static final Logger logger = LogManager.getLogger(HelloController.class);


    @GetMapping("/api/hello")
    public String handleHello(Model model) {
     //  model.addAttribute("message", "Hello from here!");
     //  List<Actor> actors = actorService.getActors();
     //  model.addAttribute("actors", actors);
     //  model.addAttribute("isSuccess", false);
       return "hello-world";  // resolved as hello.html in templates directory
    }

    @GetMapping("/api/spotify_login_once")
    public String spotifyLoginOnce(Model model){
        // to redirect the client to the spotify API
        String clientId = "9469751d45ca49cea94be50c071a3c65";
        String redirectUri = "http://127.0.0.1:3000/api/spotify_login_success";
        SecureRandom sr = new SecureRandom();
        byte[] bytes = new byte[16];
        sr.nextBytes(bytes);
        String csrfToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        model.addAttribute("csrfToken", csrfToken);

        return "redirect:" + "https://accounts.spotify.com/authorize?client_id=" +clientId+ "&response_type=code" + "&redirect_uri="+redirectUri+"&scope=user-read-playback-state user-read-currently-playing"+"&state="+csrfToken;

   //     return "redirect:https://accounts.spotify.com/authorize?client_id=%s&response_type=code&redirect_uri=http://127.0.0.1:3000/api/spotify_login_success&scope=user-read-playback-state user-read-currently-playing&state=%s".formatted(
    //            clientId,
     //           csrfToken
   //);
}

    @GetMapping("/api/spotify_login_success")
    public RespClass handleSpotifyLoginSuccess(@SessionAttribute String csrfToken, @RequestParam("code") String authCode, @RequestParam("state") String csrfTokenRecd, SessionStatus status, Model model){
        if (!csrfToken.equals(csrfTokenRecd)){
            // the csrf token recd back from spotify server is not same as generated at my backend.
            status.setComplete();
            return new RespClass(); // TODO: Implement this template
        }
        status.setComplete(); // clearing session of the temp csrfToken
        
        // THIS IS NOT SAFE AT ALL, ONE MUST NEVER PASS THE CLIENT SECRET IN THE BROWSER!!
        //model.addAttribute("code", authCode);
        //model.addAttribute("client_id", "9469751d45ca49cea94be50c071a3c65");
        //model.addAttribute("client_secret", "6139b2de2c564d9a977f34c3b27fbda4");

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


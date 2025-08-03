package com.zencode.app.services;

import org.springframework.http.HttpStatusCode;
import com.zencode.app.web.RespClass;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.security.SecureRandom;
import java.net.URI;
import java.util.Base64;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.web.client.RestClient;

import org.springframework.beans.factory.annotation.Autowired;

import com.zencode.app.web.RespClass;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;



@Service
public class CachePreloadService {
    @Autowired
    private Cache<String, Object> myCache;

    private static final Logger logger = LogManager.getLogger(CachePreloadService.class);



    @PostConstruct // Called after the bean is constructed
    public void loadInitialData() {
        String clientSecret = "6139b2de2c564d9a977f34c3b27fbda4";

        String clientId = "9469751d45ca49cea94be50c071a3c65";
        String redirectUri = "http://127.0.0.1:3000/api/spotify_login_success";
        //SecureRandom sr = new SecureRandom();
        //byte[] bytes = new byte[16];
        //sr.nextBytes(bytes);
        //String csrfToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RestClient restClient = RestClient.create();

        try {
            // Example API endpoint
            //String url = "https://accounts.spotify.com/authorize?client_id=" +clientId+ "&response_type=code" + "&redirect_uri="+redirectUri+"&scope=user-read-playback-state user-read-currently-playing"+"&state="+csrfToken;

           // RespClass resp = restClient.get()
           //     .uri(url)
           //     .exchange((request, response) -> {
           //     
           //             logger.debug("Initial status code: " + response.getStatusCode());
        
           //             if (response.getStatusCode().is3xxRedirection()) {
           //                 URI redirect_Uri = response.getHeaders().getLocation();
           //                 logger.debug("Redirecting to: " + redirect_Uri);
        
           //                 // Follow manually
           //                 return restClient.get()
           //                     .uri(redirect_Uri)
           //                     .exchange((req, res) -> {
           //                         logger.debug("Initial status code[second time]: " + res.getStatusCode());

           //                         if(res.getStatusCode().is3xxRedirection()) {
           //                             URI redirect_Uri_ = res.getHeaders().getLocation();
           //                             logger.debug("Again Redirecting to: "+ redirect_Uri_);

           //                             return restClient.get()
           //                                 .uri(redirect_Uri_)
           //                                 .retrieve()
           //                                 .body(RespClass.class);
           //                         }
           //                         logger.debug("No redirect second time");
           //                         return convertResponse(res);
           //                     });
           //             }
           //             return new RespClass();
           //             //return response.body(RespClass.class);
           //     });
            //String accessToken = "BQC_f2oOFLmanJrbCYU1cwFhbRdYJDJa1tTLNRUsqFpjfloyJ-6bW7A6Jw6CQcoPt_hC9NB5J1aO1D9VMMabW4OpTufksrkpajhCBGNj3PjMzD4lBBBFafFU6BN_xs-BPN2__2V0q3T-UCdbXNjfC-Wi1I9eL4XqdFcGZSRxt-nzJYjCM0mgqQKAR-5N1z3eHnIZ3FFhmtSzIaeb723Uc4qw7PWgqyzTlmEAOKaJTUXekJDd0g";

            String refreshToken = "AQCvmM6MO6ZOK9diYgAKVTMm5aDFR9CYbNIaqHG8BaAb3xPZCjJsrsk5rOwsqg7DEBYsQEv-Q_syPBblSW971XahKWnu0Ch6DIs5-n85umtnRPpT36FN1PQ4rDK43a-KDcw";

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
                formData.add("refresh_token", refreshToken);


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


            myCache.put("refreshToken", refreshToken);
            logger.debug("preloaded cache  with: "+ resp.getAccessToken() + " and " + refreshToken);
        } catch (Exception e) {
            logger.debug("Whoops! some error occured while preloading the cache!");
            System.err.println("Failed to preload cache: " + e.getMessage());
        }
    }
}


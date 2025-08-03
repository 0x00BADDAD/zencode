package com.zencode.app.schedulers.tasks;

// upon app startup- get refresh token and store it, this process happens only once
// then periodically use refresh token to get a new access token, this is scheduled for every 30 mins
// every second hit the current playing track api using the stored access token
// i will use a in-memory key-value cache which will be preloaded by access token and refresh token
// on application startup
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Component;



@Component
public class SpotifyTasks {

    @Scheduled(fixedRate = 2000)
    public void fetchCurrentPlayingTrackMetadata() {
//            RestClient restClient = RestClient.create();
//            String authString = "Bearer " + accessToken;
//
//
//            RespClass resp = restClient.get()
//                .uri("https://api.spotify.com/v1/me/player/currently-playing")
//                .accept(MediaType.APPLICATION_JSON)
//                .header("Authorization", authString)
//                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
//                .retrieve()
//                .body(RespClass.class);
//            logger.debug("resp is retrived and is: %s".format(resp.toString()));
//            return resp;
    }

    //@Scheduled(cron = "0 */1 * * * *")
    //public void runEveryMinute() {
    //    System.out.println("Task every minute: " + Thread.currentThread().getName());
    //}
}

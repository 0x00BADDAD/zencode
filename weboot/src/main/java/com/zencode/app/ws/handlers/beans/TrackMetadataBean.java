package com.zencode.app.ws.handlers.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import java.util.List;
import java.util.ArrayList;




public class TrackMetadataBean {
    public interface TokRespJsonView {};

    @JsonProperty("name")
    private String songName;

    @JsonProperty("artists")
    private List<String> artists;

    @JsonProperty("track_uri")
    private String trackUri;

    @JsonProperty("resource_uri")
    private String resourceUri;

    @JsonProperty("progress_ms")
    private Integer progressMs;

    @JsonProperty("duration_ms")
    private Integer durationMs;

    @JsonProperty("is_playing")
    private boolean isPlaying;

    @JsonProperty("disc_number")
    private Integer discNumber;

    @JsonProperty("atharv_track")
    private boolean atharvTrack;

    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("img_url")
    private String imgUrl;

    @JsonProperty("can_skip_prev")
    private boolean canSkipPrev;

    @JsonProperty("is_in_sync")
    private boolean isInSync;

    @JsonProperty("err_found")
    private boolean errFound;

    @JsonProperty("err_found_stack_trace")
    private String errFoundStackTrace;

    public TrackMetadataBean() {
    }

    public TrackMetadataBean(String name, List<String> artists, String trackUri, String resourceUri, Integer progress_ms, Integer duration_ms, boolean isPlaying, Integer discNumber, boolean atharvTrack, String deviceId, String imgUrl, boolean canSkipPrev, boolean isInSync, boolean errFound, String errFoundStackTrace){
        this.songName = name;
        this.artists = artists;
        this.trackUri = trackUri;
        this.resourceUri = resourceUri;
        this.progressMs = progress_ms;
        this.durationMs = duration_ms;
        this.isPlaying = isPlaying;
        this.discNumber = discNumber;
        this.atharvTrack = atharvTrack;
        this.deviceId =  deviceId;
        this.imgUrl = imgUrl;
        this.canSkipPrev = canSkipPrev;
        this.isInSync = isInSync;
        this.errFound = errFound;
        this.errFoundStackTrace = errFoundStackTrace;
    }
    // --- Getters and Setters for all fields ---
    // These are also required by Jackson for populating the object's state.
    // Getters
    public String getSongName(){
        return songName;
    }


    public List<String> getArtists(){
        return artists;
    }

    public String getTrackUri(){
        return trackUri;
    }

    public String getResourceUri(){
        return resourceUri;
    }

    public Integer getProgressMs(){
        return progressMs;
    }

    public Integer getDurationMs(){
        return durationMs;
    }
    public boolean getIsPlaying(){
        return isPlaying;
    }

    public Integer getDiscNumber(){
        return discNumber;
    }

    public boolean getAtharvTrack(){
        return atharvTrack;
    }

    public String getDeviceId(){
        return deviceId;
    }

    public String getImgUrl(){
        return imgUrl;
    }

    public boolean getCanSkipPrev(){
        return canSkipPrev;
    }

    public boolean getIsInSync(){
        return isInSync;
    }

    public boolean getErrFound(){
        return errFound;
    }

    public String getErrFoundStackTrace(){
        return errFoundStackTrace;
    }

    // Setters
    public void setSongName(String name){
        this.songName = name;
    }

    public void setArtists(List<String> artists){
        this.artists = artists;
    }

    public void setTrackUri(String trackUri){
        this.trackUri = trackUri;
    }

    public void setResourceUri(String resourceUri){
        this.resourceUri = resourceUri;
    }

    public void setProgressMs(Integer prog){
        this.progressMs = prog;
    }

    public void setDurationMs(Integer prog){
        this.durationMs = prog;
    }

    public void setIsPlaying(boolean val){
        this.isPlaying = val;
    }

    public void setDiscNumber(Integer dn){
        this.discNumber = dn;
    }

    public void setAtharvTrack(boolean atharvTrack){
        this.atharvTrack = atharvTrack;
    }

    public void setDeviceId(String deviceId){
        this.deviceId = deviceId;
    }

    public void setImgUrl(String url){
        this.imgUrl = url;
    }

    public void setCanSkipPrev(boolean val){
        this.canSkipPrev = val;
    }

    public void setIsInSync(boolean val){
        this.isInSync = val;
    }

    public void setErrFound(boolean val){
        this.errFound = val;
    }

    public void setErrFoundStackTrace(String val){
        this.errFoundStackTrace = val;
    }

    @Override
    public String toString() {
        return "TrackMetadataBeanClass{" +
               ", songName='" + songName + '\'' +
               ", artists='" + artists.toString() + '\'' +
               ", trackUri: '" + trackUri + '\'' +
               ", resourceUri: '" + resourceUri + '\'' +
               ", progress_ms: '" + progressMs + '\'' +
               ", duration_ms: '" + durationMs + '\'' +
               ", isPlaying: '" + isPlaying + '\'' +
               ", disc_number: '" + discNumber + '\'' +
               ", atharv_track: '" + atharvTrack + '\'' +
               ", device_id: '" + deviceId + '\'' +
               ", img_url '" + imgUrl + '\'' +
               ", can_skip_prev '" + canSkipPrev + '\'' +
               ", is_in_sync '" + isInSync + '\'' +
               ", err_found '" + errFound + '\'' +
               ", err_found_stack_trace '" + errFoundStackTrace + '\'' +
               '}';
    }
}





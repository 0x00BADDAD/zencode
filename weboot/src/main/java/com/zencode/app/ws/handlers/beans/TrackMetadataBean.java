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

    @JsonProperty("is_playing")
    private boolean isPlaying;

    @JsonProperty("disc_number")
    private Integer discNumber;

    @JsonProperty("atharv_track")
    private boolean atharvTrack;

    @JsonProperty("device_id")
    private String deviceId;

    public TrackMetadataBean() {
    }

    public TrackMetadataBean(String name, List<String> artists, String trackUri, String resourceUri, Integer progress_ms, boolean isPlaying, Integer discNumber, boolean atharvTrack, String deviceId){
        this.songName = name;
        this.artists = artists;
        this.trackUri = trackUri;
        this.resourceUri = resourceUri;
        this.progressMs = progress_ms;
        this.isPlaying = isPlaying;
        this.discNumber = discNumber;
        this.atharvTrack = atharvTrack;
        this.deviceId =  deviceId;
    }
    // --- Getters and Setters for all fields ---
    // These are also required by Jackson for populating the object's state.
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

    public boolean getIsPlaying(){
        return isPlaying;
    }

    public Integer getDiscNumber(){
        return discNumber;
    }

    public boolean getAtharvTrack(){
        return this.atharvTrack;
    }

    public String getDeviceId(){
        return this.deviceId;
    }

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

    public void setProgress_ms(Integer prog){
        this.progressMs = prog;
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

    @Override
    public String toString() {
        return "TrackMetadataBeanClass{" +
               ", songName='" + songName + '\'' +
               ", artists='" + artists.toString() + '\'' +
               ", trackUri: '" + trackUri + '\'' +
               ", resourceUri: '" + resourceUri + '\'' +
               ", progress_ms: '" + progressMs + '\'' +
               ", isPlaying: '" + isPlaying + '\'' +
               ", disc_number: '" + discNumber + '\'' +
               ", atharv_track '" + atharvTrack + '\'' +
               ", device_id '" + deviceId + '\'' +
               '}';
    }
}





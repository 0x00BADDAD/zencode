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

    @JsonProperty("progress_ms")
    private Integer progressMs;

    @JsonProperty("is_playing")
    private boolean isPlaying;

    @JsonProperty("disc_number")
    private Integer discNumber;


    public TrackMetadataBean() {
    }

    public TrackMetadataBean(String name, List<String> artists, String trackUri, Integer progress_ms, boolean isPlaying, Integer discNumber){
        this.songName = name;
        this.artists = artists;
        this.trackUri = trackUri;
        this.progressMs = progress_ms;
        this.isPlaying = isPlaying;
        this.discNumber = discNumber;
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

    public Integer getProgressMs(){
        return progressMs;
    }

    public boolean getIsPlaying(){
        return isPlaying;
    }

    public Integer getDiscNumber(){
        return discNumber;
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

    public void setProgress_ms(Integer prog){
        this.progressMs = prog;
    }

    public void setIsPlaying(boolean val){
        this.isPlaying = val;
    }

    public void setDiscNumber(Integer dn){
        this.discNumber = dn;
    }

    @Override
    public String toString() {
        return "TrackMetadataBeanClass{" +
               ", songName='" + songName + '\'' +
               ", artists='" + artists.toString() + '\'' +
               ", trackUri: '" + trackUri + '\'' +
               ", progress_ms: '" + progressMs + '\'' +
               ", isPlaying: '" + isPlaying + '\'' +
               ", disc_number: '" + discNumber + '\'' +
               '}';
    }
}





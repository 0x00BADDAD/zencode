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

    public TrackMetadataBean() {
    }

    public TrackMetadataBean(String name, List<String> artists){
        this.songName = name;
        this.artists = artists;
    }
    // --- Getters and Setters for all fields ---
    // These are also required by Jackson for populating the object's state.
    public String getSongName(){
        return songName;
    }


    public List<String> getArtists(){
        return artists;
    }


    public void setSongName(String name){
        this.songName = name;
    }

    public void setArtists(List<String> artists){
        this.artists = artists;
    }

    @Override
    public String toString() {
        return "TrackMetadataBeanClass{" +
               "songName='" + songName + '\'' +
               ", artists='" + artists.toString() + '\'' +
               '}';
    }
}





package com.zencode.app.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import java.util.List;


public class ReqBody {
    public interface ReqBodyView {}

    @JsonProperty("device_ids")
    private List<String> deviceIds;

    @JsonProperty("play")
    private boolean play;


    public ReqBody() {
    }

    // --- Getters and Setters for all fields ---
    // These are also required by Jackson for populating the object's state.
    @JsonView(ReqBodyView.class)
    public List<String> getDeviceIds() {
        return deviceIds;
    }

    public void setDeviceIds(List<String> ids) {
        this.deviceIds = ids;
    }

    @JsonView(ReqBodyView.class)
    public boolean getPlay() {
        return play;
    }

    public void setPlay(boolean play){
        this.play= play;
    }

    @Override
    public String toString(){
        return "ReqBody{\n   " + "device_ids: " + deviceIds.toString() + "\n play: " + play + "\n}";
    }
}

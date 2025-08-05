package com.zencode.app.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

public class TokRespClass {
    public interface TokRespClassView {};

    @JsonProperty("access_token")
    private String accessToken;


    public TokRespClass() {

    }

    public TokRespClass(String token){
        this.accessToken = token;
    }

    // --- Getters and Setters for all fields ---
    // These are also required by Jackson for populating the object's state.
    @JsonView(TokRespClassView.class)
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String token){
        this.accessToken = token;
    }
}



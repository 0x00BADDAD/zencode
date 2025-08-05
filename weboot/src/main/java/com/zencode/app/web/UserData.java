package com.zencode.app.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

public class UserData {
    public interface TokRespJsonView {};

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("email")
    private String email;
    
    @JsonProperty("name")
    private String name;


    public UserData() {
    }

    public UserData(String accessToken, String refreshToken, String email, String name){
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = email;
        this.name = name;
    }

    // --- Getters and Setters for all fields ---
    // These are also required by Jackson for populating the object's state.
    @JsonView(TokRespJsonView.class)
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @JsonView(TokRespJsonView.class)
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @JsonView(TokRespJsonView.class)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @JsonView(TokRespJsonView.class)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return "RespClass{" +
               "accessToken='" + accessToken + '\'' +
               ", refreshToken='" + refreshToken + '\'' +
               ", email='" + email + '\'' +
               ", name='" + name + '\'' +
               '}';
    }
}



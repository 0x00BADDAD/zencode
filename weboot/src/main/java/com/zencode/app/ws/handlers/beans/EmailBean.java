package com.zencode.app.ws.handlers.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import java.util.List;
import java.util.ArrayList;


public class EmailBean{
    @JsonProperty("email")
    private String email;

    public EmailBean(){}

    public void setEmail(String mail){
        this.email = mail;
    }

    public String getEmail(){
        return email;
    }
}

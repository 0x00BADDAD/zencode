package com.zencode.app.dao.beans;


public class Actor {
    // a simple java bean for sql input-output
    // but with public fields! since these are also used in thymeleaf.
    public String firstName;
    public String lastName;

    public void setFirstName(String s){
        this.firstName = s;
    }

    public String getFirstName(String s){
        return this.firstName;
    }

    public void setLastName(String s){
        this.lastName = s;
    }

    public String getLastName(String s){
        return this.lastName;
    }
}

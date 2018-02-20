package com.sesnu.fireball.service;



public class WSMessage {
    private String content;

    public WSMessage(String key,String value) {
        this.content = key + "-" + value;
    }

    public String getContent() {
        return content;
    }
}

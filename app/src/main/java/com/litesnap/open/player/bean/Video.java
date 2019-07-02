package com.litesnap.open.player.bean;

import java.io.Serializable;
import java.util.UUID;

public class Video implements Serializable {
    private long uuid;
    private String url;
    private long current;

    public Video(String url){
        this.url = url;
        uuid = UUID.randomUUID().getLeastSignificantBits();
    }

    public long getUUID() {
        return uuid;
    }

    public String getUrl() {
        return url;
    }

    public long getCurrent() {
        return current;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCurrent(long current) {
        this.current = current;
    }
}

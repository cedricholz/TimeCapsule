package com.example.cedric.timecapsule.Imaging;

public class ImageCell {
    private String highresUrl;
    private String thumbUrl;

    public String getHighresUrl() {
        return highresUrl;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setImg(String highresUrl, String thumbUrl) {
        this.highresUrl = highresUrl;
        this.thumbUrl = highresUrl;
    }
}

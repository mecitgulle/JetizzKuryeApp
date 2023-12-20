package com.arasholding.jetizzkuryeapp.apimodels;

public class MobilDenemeRequest {
    private String Barkod;
    private String Konum;
    private String DeviceToken;
    private String Image;

    public String getBarkod() {
        return Barkod;
    }

    public void setBarkod(String barkod) {
        Barkod = barkod;
    }

    public String getKonum() {
        return Konum;
    }

    public void setKonum(String konum) {
        Konum = konum;
    }

    public String getDeviceToken() {
        return DeviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        DeviceToken = deviceToken;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }
}

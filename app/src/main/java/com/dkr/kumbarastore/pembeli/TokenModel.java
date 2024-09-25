package com.dkr.kumbarastore.pembeli;

public class TokenModel {

    private String deviceToken;

    public TokenModel() {
        // Constructor kosong diperlukan untuk Firestore
    }

    public TokenModel(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}

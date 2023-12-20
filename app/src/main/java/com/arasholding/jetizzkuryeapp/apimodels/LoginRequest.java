package com.arasholding.jetizzkuryeapp.apimodels;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("username")
    private String username;
    @SerializedName("password")
    private String password;
    @SerializedName("grant_type")
    private String grant_type;

    public LoginRequest(String username, String password,String grant_type) {
        this.username = username;
        this.password = password;
        this.grant_type = grant_type;
    }

}

package com.arasholding.jetizzkuryeapp.pref;

public interface SharedPreferencesHelper {
    void setLatitude(String latitude);
    String getLatitude();
    void setLongitude(String longitude);
    String getLongitude();
    void setPairedDeviceName(String device);
    String getPairedDeviceName();
}

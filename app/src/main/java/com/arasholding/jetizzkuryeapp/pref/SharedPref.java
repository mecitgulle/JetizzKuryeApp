package com.arasholding.jetizzkuryeapp.pref;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref implements SharedPreferencesHelper {
    private SharedPreferences sharedPref;

    public SharedPref(Context context) {
        this.sharedPref = context.getSharedPreferences("kuryePref",Context.MODE_PRIVATE);
    }

    @Override
    public void setLatitude(String latitude) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("latValue",latitude);
        editor.apply();
    }

    @Override
    public String getLatitude() {
        return sharedPref.getString("latValue","");
    }

    @Override
    public void setLongitude(String longitude) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("longValue",longitude);
        editor.apply();
    }

    @Override
    public String getLongitude() {
        return sharedPref.getString("longValue","");
    }

    @Override
    public void setPairedDeviceName(String device) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PrefKeys.PREF_KEY_SELECTED_BLUETOOTH_DEVICE,device);
        editor.apply();
    }

    @Override
    public String getPairedDeviceName() {
        return sharedPref.getString(PrefKeys.PREF_KEY_SELECTED_BLUETOOTH_DEVICE,"");
    }
}

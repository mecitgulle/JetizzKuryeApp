package com.arasholding.jetizzkuryeapp.pref;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.arasholding.jetizzkuryeapp.apimodels.LoginResponse;

public class Prefences {

    public static void saveToPreferences(Context context, LoginResponse login) {

        if (login == null) return;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(PrefKeys.TOKEN, login.getAccess_token());
        editor.apply();
    }
    public static void saveToPreferencesMAC(Context context, String mac) {

        if (mac == null) return;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(PrefKeys.MAC_ADDRESS, mac);
        editor.apply();
    }
    public static LoginResponse readFromPreferences(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

        LoginResponse login = new LoginResponse();

        login.setAccess_token(sharedPreferences.getString(PrefKeys.TOKEN, ""));

        return login;
    }
    public static String readFromPreferencesMAC(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return sharedPreferences.getString(PrefKeys.MAC_ADDRESS, "");
    }
    public static void saveToSmsKod(Context context, String smsKod) {

        if (smsKod == null) return;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(PrefKeys.SMS_KOD,smsKod);
        editor.apply();
    }
    public static String readFromPreferencesSmsKod(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return sharedPreferences.getString(PrefKeys.SMS_KOD, "");
    }
}

package com.arasholding.jetizzkuryeapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.apimodels.ApiResponseModel;
import com.arasholding.jetizzkuryeapp.apimodels.BaseUrl;
import com.arasholding.jetizzkuryeapp.apimodels.LoginRequest;
import com.arasholding.jetizzkuryeapp.apimodels.LoginResponse;
import com.arasholding.jetizzkuryeapp.helpers.Generator;
import com.arasholding.jetizzkuryeapp.pref.Prefences;
import com.arasholding.jetizzkuryeapp.service.JetizzService;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.security.cert.CertificateException;
//
//import javax.net.ssl.HostnameVerifier;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.SSLSession;
//import javax.net.ssl.SSLSocketFactory;
//import javax.net.ssl.TrustManager;
//import javax.net.ssl.X509TrustManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {
    private EditText edt_username;
    private EditText edt_password;
    private String newDeviceToken;

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BaseUrl.Url)
            .addConverterFactory(GsonConverterFactory.create())
//            .client(getUnsafeOkHttpClient().build())
            .build();
    JetizzService service = retrofit.create(JetizzService.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edt_username = findViewById(R.id.edtUsername);
        edt_password = findViewById(R.id.edtPassword);

        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(edt_username.getText().toString(), edt_password.getText().toString());
            }
        });

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult -> {
            newDeviceToken = instanceIdResult.getToken();
            Log.e("newDeviceToken", newDeviceToken);
        });
    }

    private void login(String username, String password) {

        String deviceName = getDeviceName();
//        Prefences.saveToSmsKod(getApplicationContext(),String.valueOf(smsKod));
//        String readSmsKod = Prefences.readFromPreferencesSmsKod(getApplicationContext());
//
//        Log.e("saveSmsKod", String.valueOf(smsKod));
//        Log.e("readSmsKod", readSmsKod);

        String newpass =BaseUrl.UserPasswordPrefix+"_"+password;
        String scope = ",,,"+newDeviceToken+","+deviceName;
        Call<LoginResponse> responseCall = service.login(username, newpass, "password",scope);

        responseCall.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    String token = response.body().getAccess_token();
                    LoginResponse loginResponse = new LoginResponse();
                    loginResponse.setAccess_token(token);
                    loginResponse.setExpires_in(response.body().getExpires_in());
                    loginResponse.setToken_type(response.body().getToken_type());

                    Prefences.saveToPreferences(getApplicationContext(), loginResponse);

                    Call<ApiResponseModel> responseCall2 = service.PhoneDeviceTokenUpdate("Bearer " + token,newDeviceToken);
                    responseCall2.enqueue(new Callback<ApiResponseModel>() {
                        @Override
                        public void onResponse(Call<ApiResponseModel> call, Response<ApiResponseModel> response) {
                            if (response.isSuccessful()) {


                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponseModel> call, Throwable t) {
//                            Toast.makeText(LoginActivity.this, "Bağlantıda hata oluştu", Toast.LENGTH_LONG).show();
                        }
                    });

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        String mes = jObjError.getString("error_description");
                        Toast.makeText(LoginActivity.this, jObjError.getString("error_description"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    }
//                    Toast.makeText(LoginActivity.this, "Kullanıcı adı veya şifre yanlış !", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + "" + model.replace(" ","");
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString().replace(" ","");
    }
//    public static OkHttpClient.Builder getUnsafeOkHttpClient() {
//        try {
//            // Create a trust manager that does not validate certificate chains
//            final TrustManager[] trustAllCerts = new TrustManager[]{
//                    new X509TrustManager() {
//                        @Override
//                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
//                        }
//
//                        @Override
//                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
//                        }
//
//                        @Override
//                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                            return new java.security.cert.X509Certificate[]{};
//                        }
//                    }
//            };
//
//            // Install the all-trusting trust manager
//            final SSLContext sslContext = SSLContext.getInstance("SSL");
//            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
//
//            // Create an ssl socket factory with our all-trusting manager
//            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
//
//            OkHttpClient.Builder builder = new OkHttpClient.Builder();
//            builder.sslSocketFactory(sslSocketFactory);
//            builder.hostnameVerifier(new HostnameVerifier() {
//                @Override
//                public boolean verify(String hostname, SSLSession session) {
//                    return true;
//                }
//            });
//            return builder;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
}

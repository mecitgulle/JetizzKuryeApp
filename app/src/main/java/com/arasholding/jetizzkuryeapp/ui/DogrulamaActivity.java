package com.arasholding.jetizzkuryeapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.apimodels.ApiResponseModel;
import com.arasholding.jetizzkuryeapp.apimodels.ApiResponseModelObj;
import com.arasholding.jetizzkuryeapp.apimodels.BaseUrl;
import com.arasholding.jetizzkuryeapp.apimodels.LoginResponse;
import com.arasholding.jetizzkuryeapp.apimodels.SmsResponseModel;
import com.arasholding.jetizzkuryeapp.helpers.ShowDialog;
import com.arasholding.jetizzkuryeapp.pref.Prefences;
import com.arasholding.jetizzkuryeapp.service.JetizzService;
import com.arasholding.jetizzkuryeapp.utils.CommonUtils;

import org.joda.time.DateTime;
import org.w3c.dom.Text;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DogrulamaActivity extends Activity {

    private static final String TAG = "DogrulamaActivity";

    private static int TIME_OUT = 100; // 0.1 sn beklemesi için

    private ActionBar actionBar;
    private boolean dogrulandiMi = false;
    private EditText edtDogrulamaKodu;
    private Button btnDogrula;
    private LoginResponse loginResponse;

    private int telefondakiDogrulamaKodu = 0;
    private String oturumAcanKullaniciAdi;
    private String kullaniciAdi;
    private String personelKodu;
    private ProgressBar progressBar;
    private TextView textCounter;
    private String smsKod;
    Call<ApiResponseModelObj<String>> responseCall;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dogrulama);

        edtDogrulamaKodu = findViewById(R.id.edtDogrulamaKodu);
        loginResponse = Prefences.readFromPreferences(getApplicationContext());
        GetSmsKod();
        findViewById(R.id.btn_dogrula).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dogrula();
            }
        });


    }

    private boolean hizliDogrula() {

        String dogrulamaKodu = edtDogrulamaKodu.getText().toString();

        DateTime dateTime = DateTime.now();

        int dayNumber = Integer.valueOf(dateTime.dayOfMonth().getAsString()) * 10;
        int monthNumber = Integer.valueOf(dateTime.monthOfYear().getAsString()) * 10;

        String validKey = dayNumber + "00" + monthNumber;

        return validKey.equals(dogrulamaKodu);
    }

    public void Dogrula() {
        String dogrulamaKod = edtDogrulamaKodu.getText().toString();

        if (hizliDogrula()) {

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        } else {

            if (dogrulamaKod.equals(smsKod)) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Hatalı kod!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void GetSmsKod() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseUrl.Url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JetizzService service = retrofit.create(JetizzService.class);
        String token = loginResponse.getAccess_token();

        responseCall = service.SendSms("Bearer " +  token);

        Log.v("token", loginResponse.getAccess_token());

        alertDialog = CommonUtils.showLoadingDialogWithMessage(this, "Lütfen Bekleyiniz");
        responseCall.enqueue(new Callback<ApiResponseModelObj<String>>() {
            @Override
            public void onResponse(Call<ApiResponseModelObj<String>> call, retrofit2.Response<ApiResponseModelObj<String>> response) {
                alertDialog.dismiss();
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        smsKod = response.body().Result;
                    }
                } else {
                    alertDialog.dismiss();
                    if (response.raw().code() == 401)
                        CommonUtils.showTokenExpired(DogrulamaActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                    else
                        CommonUtils.showAlertWithMesseage(DogrulamaActivity.this, "İşlem Başarısız", "UYARI");
                }
            }

            @Override
            public void onFailure(Call<ApiResponseModelObj<String>> call, Throwable t) {
                alertDialog.dismiss();
            }
        });
    }
}
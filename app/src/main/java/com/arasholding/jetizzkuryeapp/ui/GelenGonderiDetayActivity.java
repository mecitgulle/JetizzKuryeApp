package com.arasholding.jetizzkuryeapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.adapters.SpinnerAdapter;
import com.arasholding.jetizzkuryeapp.apimodels.ApiResponseModel;
import com.arasholding.jetizzkuryeapp.apimodels.BaseUrl;
import com.arasholding.jetizzkuryeapp.apimodels.ErrorMessage;
import com.arasholding.jetizzkuryeapp.apimodels.LoginResponse;
import com.arasholding.jetizzkuryeapp.apimodels.OnIadeRequest;
import com.arasholding.jetizzkuryeapp.apimodels.ReturnRequest;
import com.arasholding.jetizzkuryeapp.apimodels.SpinnerModel;
import com.arasholding.jetizzkuryeapp.helpers.ShowDialog;
import com.arasholding.jetizzkuryeapp.pref.Prefences;
import com.arasholding.jetizzkuryeapp.service.JetizzService;
import com.arasholding.jetizzkuryeapp.utils.CommonUtils;
import com.arasholding.jetizzkuryeapp.utils.OkutmaTipleri;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.security.cert.CertificateException;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//import static com.arasholding.jetizzkuryeapp.ui.LoginActivity.getUnsafeOkHttpClient;

public class
GelenGonderiDetayActivity extends BaseActivity {
    int atfId;
    String notBirakildi;
    MaterialSpinner spinner_oniadelist;
    List<SpinnerModel> spinnerOnIadeList;
    int onIadeTipiId = 0;
    private Unbinder unbinder;
    private LoginResponse loginResponse;
    SpinnerAdapter adapter;
    AlertDialog alertDialog;
    @BindView(R.id.txtTakipNo)
    TextView txtTakipNo;
    @BindView(R.id.txtMTakipNo)
    TextView txtMTakipNo;
    @BindView(R.id.txtDeliveryEndDate)
    TextView txtDeliveryEndDate;
    @BindView(R.id.txtGondericiAdi)
    TextView txtGondericiAdi;
    @BindView(R.id.txtGondericiAdres)
    TextView txtGondericiAdres;
    @BindView(R.id.txtGonderenTelefon)
    TextView txtGonderenTelefon;
    @BindView(R.id.txtAliciAdi)
    TextView txtAliciAdi;
    @BindView(R.id.txtAliciAdresi)
    TextView txtAliciAdresi;
    @BindView(R.id.txtAliciTelefon)
    TextView txtAliciTelefon;
    View parentLayout;
    @BindView(R.id.cardOnIadeIslemi)
    CardView cardOnIadeIslemi;

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BaseUrl.Url)
            .addConverterFactory(GsonConverterFactory.create())
//            .client(getUnsafeOkHttpClient().build())
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gelen_gonderi_detay);
        unbinder = ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loginResponse = Prefences.readFromPreferences(getApplicationContext());
        parentLayout = findViewById(android.R.id.content);
        Intent i = getIntent();
        txtAliciAdi.setText(Html.fromHtml("<b> Alıcı: </b> " + i.getStringExtra("tempAlici")));
        txtGondericiAdi.setText(Html.fromHtml("<b> Gönderen: </b> " + i.getStringExtra("tempGonderici")));
        atfId = i.getIntExtra("tempAtfId", 0);
        notBirakildi = i.getStringExtra("tempNotBirakildi");
        txtTakipNo.setText(Html.fromHtml("<b> TakipNo: </b> " + String.valueOf(atfId)));
        txtGondericiAdres.setText(Html.fromHtml("<b> Adres: </b> " + i.getStringExtra("tempGondericiAdres")));
        txtGonderenTelefon.setText(Html.fromHtml("<b> Telefon: </b> " + i.getStringExtra("tempGonderenTelefon")));
        txtAliciAdresi.setText(Html.fromHtml("<b> Adres: </b> " + i.getStringExtra("tempAliciAdres")));
        txtMTakipNo.setText(Html.fromHtml("<b> MTakip No: </b> " + i.getStringExtra("tempMTakipNo")));
        txtAliciTelefon.setText(Html.fromHtml("<b> Telefon: </b> " + i.getStringExtra("tempAliciTelefon")));
        txtDeliveryEndDate.setText(Html.fromHtml("<b> Olası Teslim Tarihi: </b> " + i.getStringExtra("tempDeliveryEndDate")));

        if(notBirakildi != null)
        {
            toolbar.setTitle("Not Bırakılan Gönderi Detay");
            cardOnIadeIslemi.setVisibility(View.GONE);
        }

        spinner_oniadelist = findViewById(R.id.spinner_oniadelist);

        loginResponse = Prefences.readFromPreferences(getApplicationContext());


        JetizzService service = retrofit.create(JetizzService.class);
        String token = loginResponse.getAccess_token();

        Call<List<SpinnerModel>> responseCall = service.GetOnIadeTipleri("Bearer " + token);
//        if(!((Activity) getApplicationContext()).isFinishing())
//            alertDialog = CommonUtils.showLoadingDialogWithMessage(getApplicationContext(), "Lütfen Bekleyiniz");
        responseCall.enqueue(new Callback<List<SpinnerModel>>() {
            @Override
            public void onResponse(Call<List<SpinnerModel>> call, Response<List<SpinnerModel>> response) {
                if (response.isSuccessful()) {
//                    alertDialog.dismiss();
                    spinnerOnIadeList = response.body();

                    adapter = new SpinnerAdapter(getApplicationContext(), R.layout.spinner_row, spinnerOnIadeList);
                    spinner_oniadelist.setAdapter(adapter);

                } else {
                    if (response.raw().code() == 401)
                        CommonUtils.showTokenExpired(getApplicationContext(), "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                    else
                        CommonUtils.showAlertWithMesseage(getApplicationContext(), "İşlem Başarısız", "UYARI");
//                    alertDialog.dismiss();
//                    Toast.makeText(LoginActivity.this,"login error",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<SpinnerModel>> call, Throwable t) {
//                Toast.makeText(LoginActivity.this,"login ss error",Toast.LENGTH_LONG).show();
            }
        });
        spinner_oniadelist.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                SpinnerModel model = (SpinnerModel) adapter.getItemObj(position);
                onIadeTipiId = model.getValue();
                if (onIadeTipiId == 0) {
                    Toast.makeText(GelenGonderiDetayActivity.this, "Ön İade Nedeni Seçiniz", Toast.LENGTH_SHORT).show();
                }
//                Toast.makeText(getContext(), "ID: " + model.getValue() + "\nName: " + model.getText(),
//                        Toast.LENGTH_SHORT).show();
            }

        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    public void yonlendir() {
        Intent intent = new Intent(GelenGonderiDetayActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @OnClick(R.id.btnzimmetAl)
    public void ZimmetAl() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseUrl.Url)
                .addConverterFactory(GsonConverterFactory.create())
//                .client(getUnsafeOkHttpClient().build())
                .build();
        JetizzService service = retrofit.create(JetizzService.class);
        String token = loginResponse.getAccess_token();


        Call<ApiResponseModel> responseCall = service.ZimmetAl("Bearer " + token, String.valueOf(atfId), "6", "-1", "MOBIL", "false");


        alertDialog = CommonUtils.showLoadingDialogWithMessage(this, "Lütfen Bekleyiniz");
        responseCall.enqueue(new Callback<ApiResponseModel>() {
            @Override
            public void onResponse(Call<ApiResponseModel> call, retrofit2.Response<ApiResponseModel> response) {
                alertDialog.dismiss();
                if (response.isSuccessful()) {
                    String status = response.body().Status;
                    if (status.equals("success")) {
                        ShowDialog.setSnackBar(parentLayout, response.body().Message);
                        yonlendir();
                    } else {
                        CommonUtils.showAlertWithMesseage(GelenGonderiDetayActivity.this, response.body().Message, "UYARI");
                        vibrate();
                    }
                } else {
                    if (response.raw().code() == 401)
                        CommonUtils.showTokenExpired(GelenGonderiDetayActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                    else
                        CommonUtils.showAlertWithMesseage(GelenGonderiDetayActivity.this, "İşlem Başarısız", "UYARI");
                    alertDialog.dismiss();
                    vibrate();
                }
            }

            @Override
            public void onFailure(Call<ApiResponseModel> call, Throwable t) {
                alertDialog.dismiss();
                vibrate();
                CommonUtils.showAlertWithMesseage(GelenGonderiDetayActivity.this, "Bağlantıda hata oluştu", "UYARI");
            }
        });
    }

    @OnClick(R.id.btnOnIade)
    public void OnIadeYap() {
        if (onIadeTipiId != 0) {

            String token = loginResponse.getAccess_token();
            OnIadeRequest onIadeRequest = new OnIadeRequest();
            onIadeRequest.setId(atfId);
            onIadeRequest.setOnIadeNedeni(onIadeTipiId);

            JetizzService service = retrofit.create(JetizzService.class);
            Call<ApiResponseModel> responseCall = service.OnIadeGirisi("Bearer " + token, onIadeRequest);

            alertDialog = CommonUtils.showLoadingDialogWithMessage(GelenGonderiDetayActivity.this, "Lütfen Bekleyiniz");
            responseCall.enqueue(new Callback<ApiResponseModel>() {
                @Override
                public void onResponse(Call<ApiResponseModel> call, Response<ApiResponseModel> response) {
                    if (response.isSuccessful()) {
                        alertDialog.dismiss();
                        if (response.raw().code() == 401)
                            Toast.makeText(GelenGonderiDetayActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", Toast.LENGTH_LONG).show();
                        if (response.body() != null) {
                            if (response.body().Errors != null) {
                                for (ErrorMessage item : response.body().Errors) {
                                    Toast.makeText(GelenGonderiDetayActivity.this, item.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                if (response.body().Status.equals("success")) {
                                    alertDialog.dismiss();
                                    Toast.makeText(GelenGonderiDetayActivity.this, "İşlem Başarılı",
                                            Toast.LENGTH_SHORT).show();
                                    yonlendir();

                                }
                            }
                        }
                    } else {

                        if (response.raw().code() == 401)
                            CommonUtils.showTokenExpired(GelenGonderiDetayActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                        else
                            CommonUtils.showAlertWithMesseage(GelenGonderiDetayActivity.this, "İşlem Başarısız", "UYARI");
                        alertDialog.dismiss();
//                    Toast.makeText(LoginActivity.this,"login error",Toast.LENGTH_LONG).show();
//                    if (response.body().Errors != null) {
//                        for (ErrorMessage item : response.body().Errors) {
//                            Toast.makeText(getContext(), item.getMessage(),
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                    else{
//                        Toast.makeText(getActivity(),response.body().Message,Toast.LENGTH_LONG).show();
//                    }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponseModel> call, Throwable t) {
                    Toast.makeText(GelenGonderiDetayActivity.this, t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(GelenGonderiDetayActivity.this, "Ön İade Nedeni Seçiniz", Toast.LENGTH_SHORT).show();

        }
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

package com.arasholding.jetizzkuryeapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.apimodels.ApiResponseModel;
import com.arasholding.jetizzkuryeapp.apimodels.BaseUrl;
import com.arasholding.jetizzkuryeapp.apimodels.ErrorMessage;
import com.arasholding.jetizzkuryeapp.apimodels.LoginResponse;
import com.arasholding.jetizzkuryeapp.apimodels.ZimmetListModel;
import com.arasholding.jetizzkuryeapp.helpers.ShowDialog;
import com.arasholding.jetizzkuryeapp.pref.Prefences;
import com.arasholding.jetizzkuryeapp.service.JetizzService;
import com.arasholding.jetizzkuryeapp.utils.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.android.BeepManager;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.security.cert.CertificateException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;



import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BarkodileTeslimActivity extends Activity implements ZXingScannerView.ResultHandler {

    private EditText edtMnlGonderiNo;
    private ZXingScannerView mScannerView;
    private ActionBar actionBar;
    private BeepManager beepManager;

    private ImageView imgBtnFlash;
    private LoginResponse loginResponse;

    private List<String> barcodeList;
    private String okutmaTipi;
    AlertDialog alertDialog;
    List<ZimmetListModel> list;
    Call<List<ZimmetListModel>> responseCall;
    View parentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barkodile_teslim);

        loginResponse = Prefences.readFromPreferences(getApplicationContext());
        beepManager = new BeepManager(this);
        parentLayout = findViewById(android.R.id.content);

        mScannerView = (ZXingScannerView) findViewById(R.id.zXingScanner);
        edtMnlGonderiNo = (EditText) findViewById(R.id.edtMnlGonderiNo);

        List<BarcodeFormat> barcodeFormats = new ArrayList<>();
        barcodeFormats.add(BarcodeFormat.CODE_128);
        barcodeFormats.add(BarcodeFormat.QR_CODE);

        mScannerView.setAspectTolerance(0.5f);
        mScannerView.setFormats(barcodeFormats);
        mScannerView.setAutoFocus(true);
        mScannerView.setResultHandler(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }


    @Override
    public void handleResult(Result rawResult) {
        try {
            Log.v("TAG", rawResult.getText()); // Prints scan results
            Log.v("TAG", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
            final String barkod = rawResult.getText();

            beepManager.playBeepSoundAndVibrate();
            Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new JsonDeserializer<DateTime>() {
                @Override
                public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                        throws JsonParseException {
                    return new DateTime(json.getAsString());
                }
            }).create();
            mScannerView.stopCamera();           // Stop camera on pause
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BaseUrl.Url)
                    .addConverterFactory(GsonConverterFactory.create(gson))
//                .client(getUnsafeOkHttpClient().build())
                    .build();
            JetizzService service = retrofit.create(JetizzService.class);
            String token = loginResponse.getAccess_token();


            responseCall = service.GetGonderi("Bearer " + token, rawResult.getText().toString());

            Log.v("token", loginResponse.getAccess_token());

            alertDialog = CommonUtils.showLoadingDialogWithMessage(this, "Lütfen Bekleyiniz");
            responseCall.enqueue(new Callback<List<ZimmetListModel>>() {
                @Override
                public void onResponse(Call<List<ZimmetListModel>> call, retrofit2.Response<List<ZimmetListModel>> response) {
                    alertDialog.dismiss();
                    if (response.isSuccessful()) {
                        list = response.body();
                        if (response.body() != null) {
                            if (list != null && list.size() != 0) {

                                String tempAlici = list.get(0).getAlici();
                                String tempAtfNo = list.get(0).getAtfNo();
                                String tempGonderici = list.get(0).getMagaza();
                                String tempHizmet = list.get(0).getHizmet();
                                String tempZimmetAlan = list.get(0).getKurye();
                                String tempDesi = list.get(0).getDesi();
                                String tempSonIslem = list.get(0).getSonIslem();
                                int tempAtfId = list.get(0).getAtfId();
                                String tempAliciTelefon = list.get(0).getAliciTelefon();
                                String tempTahTip = list.get(0).getTahsilatTipi();
                                Double tempTahTur = list.get(0).getTahsilatTutari();

                                Intent i = new Intent(BarkodileTeslimActivity.this, DeliveryActivity.class);
                                i.putExtra("tempAlici", tempAlici);
                                i.putExtra("tempAtfNo", tempAtfNo);
                                i.putExtra("tempGonderici", tempGonderici);
                                i.putExtra("tempHizmet", tempHizmet);
                                i.putExtra("tempZimmetAlan", tempZimmetAlan);
                                i.putExtra("tempDesi", tempDesi);
                                i.putExtra("tempSonIslem", tempSonIslem);
                                i.putExtra("tempAtfId", tempAtfId);
                                i.putExtra("tempAliciTelefon", tempAliciTelefon);
                                i.putExtra("tempTahTip", tempTahTip);
                                i.putExtra("tempTahTur", tempTahTur);
                                startActivity(i);
                                finish();
                            } else {
                                CommonUtils.showAlertWithMesseage(BarkodileTeslimActivity.this, "Kriterlere uygun gönderi bulunamadı !", "UYARI");
                            }
                        }


                    } else {
                        alertDialog.dismiss();
                        if (response.raw().code() == 401)
                            CommonUtils.showTokenExpired(BarkodileTeslimActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                        else
                            CommonUtils.showAlertWithMesseage(BarkodileTeslimActivity.this, "İşlem Başarısız", "UYARI");
                        mScannerView.startCamera();

                    }
                }

                @Override
                public void onFailure(Call<List<ZimmetListModel>> call, Throwable t) {
                    alertDialog.dismiss();
                    ShowDialog.setSnackBar(parentLayout, "Bağlantıda hata oluştu");
                }
            });


            // If you would like to resume scanning, call this method below:
            mScannerView.startCamera();

            mScannerView.resumeCameraPreview(this);
        } catch (NullPointerException e) {
            mScannerView.startCamera();
            Toast.makeText(BarkodileTeslimActivity.this, "İşlem sırasında bir hata oluştu", Toast.LENGTH_SHORT).show();
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
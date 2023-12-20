package com.arasholding.jetizzkuryeapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.apimodels.ApiResponseModel;
import com.arasholding.jetizzkuryeapp.apimodels.BaseUrl;
import com.arasholding.jetizzkuryeapp.apimodels.ErrorMessage;
import com.arasholding.jetizzkuryeapp.apimodels.LoginResponse;
import com.arasholding.jetizzkuryeapp.helpers.ShowDialog;
import com.arasholding.jetizzkuryeapp.pref.Prefences;
import com.arasholding.jetizzkuryeapp.service.JetizzService;
import com.arasholding.jetizzkuryeapp.utils.CommonUtils;
import com.arasholding.jetizzkuryeapp.utils.OkutmaTipleri;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.android.BeepManager;

import java.io.IOException;
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

public class DesiGuncellemeActivity extends Activity implements ZXingScannerView.ResultHandler {
    private TextView txtOkutulanBarkodSayisi;
    private EditText edtMnlDesi;
    private EditText edtMnlGonderiNo;
    private ZXingScannerView mScannerView;
    private ActionBar actionBar;
    private BeepManager beepManager;

    private ImageView imgBtnFlash;
    private LoginResponse loginResponse;

    private List<String> barcodeList;
    private String okutmaTipi;
    AlertDialog alertDialog;
    Call<ApiResponseModel> responseCall;
    View parentLayout;

    CheckBox checkmuskolino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desi_guncelleme);

        Intent intent = getIntent();

        barcodeList = new ArrayList<>();
        loginResponse = Prefences.readFromPreferences(getApplicationContext());
        beepManager = new BeepManager(this);
        parentLayout = findViewById(android.R.id.content);
//        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view

        mScannerView = (ZXingScannerView) findViewById(R.id.zXingScanner);
        txtOkutulanBarkodSayisi = (TextView) findViewById(R.id.txtOkutulanBarkodSayisi);
        edtMnlGonderiNo = (EditText) findViewById(R.id.edtMnlGonderiNo);
        edtMnlDesi = (EditText) findViewById(R.id.edtMnlDesi);
//        checkmuskolino = (CheckBox) findViewById(R.id.checkmuskolino);

        okutmaTipi = intent.getStringExtra("okutmaTipi");

        imgBtnFlash = findViewById(R.id.imgBtnFlash);

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
            final String desi = edtMnlDesi.getText().toString();

            beepManager.playBeepSoundAndVibrate();

            if (!barcodeList.contains(rawResult.getText())) {
                mScannerView.stopCamera();           // Stop camera on pause
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BaseUrl.Url)
                        .addConverterFactory(GsonConverterFactory.create())
//                       .client(getUnsafeOkHttpClient().build())
                        .build();
                JetizzService service = retrofit.create(JetizzService.class);
                String token = loginResponse.getAccess_token();


                responseCall = service.DesiGuncelle("Bearer " + token, rawResult.getText(), desi, "MOBIL", "false");


                Log.v("token", loginResponse.getAccess_token());

                alertDialog = CommonUtils.showLoadingDialogWithMessage(this, "Lütfen Bekleyiniz");
                responseCall.enqueue(new Callback<ApiResponseModel>() {
                    @Override
                    public void onResponse(Call<ApiResponseModel> call, retrofit2.Response<ApiResponseModel> response) {
                        alertDialog.dismiss();
                        if (response.isSuccessful()) {
                            String status = response.body().Status;
                            if (response.body() != null) {
                                if (status.equals("success")) {
                                    barcodeList.add(barkod);
                                    txtOkutulanBarkodSayisi.setText(String.valueOf(barcodeList.size()));
                                    edtMnlGonderiNo.setText("");

                                } else {
                                    if (response.body().Errors != null) {
                                        for (ErrorMessage item : response.body().Errors) {
                                            CommonUtils.showAlertWithMesseage(DesiGuncellemeActivity.this, item.getMessage(), "UYARI");
                                        }
                                    }
                                    mScannerView.startCamera();
                                    vibrate();
                                }
                            }


                        } else {
                            alertDialog.dismiss();
                            if (response.raw().code() == 401)
                                CommonUtils.showTokenExpired(DesiGuncellemeActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                            else
                                CommonUtils.showAlertWithMesseage(DesiGuncellemeActivity.this, "İşlem Başarısız", "UYARI");
                            mScannerView.startCamera();
//                            ShowDialog.setSnackBar(parentLayout,"İşlem Başarısız");
                            vibrate();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponseModel> call, Throwable t) {
                        alertDialog.dismiss();

                        ShowDialog.setSnackBar(parentLayout, "Bağlantıda hata oluştu");
//                        ShowDialog.CreateDialog(getParent(),"Bağlantıda hata oluştu","UYARI");
                        vibrate();
                    }
                });
            }

            // If you would like to resume scanning, call this method below:
            mScannerView.startCamera();

            mScannerView.resumeCameraPreview(this);
        } catch (NullPointerException e) {
            mScannerView.startCamera();
            Toast.makeText(DesiGuncellemeActivity.this, "İşlem sırasında bir hata oluştu", Toast.LENGTH_SHORT).show();
        }
    }

    public void btnManuelOkut(View view) {
        String barkod = edtMnlGonderiNo.getText().toString();
        String desi = edtMnlDesi.getText().toString();

        if (!barcodeList.contains(barkod)) {
            mScannerView.stopCamera();           // Stop camera on pause
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BaseUrl.Url)
                    .addConverterFactory(GsonConverterFactory.create())
//                    .client(getUnsafeOkHttpClient().build())
                    .build();
            JetizzService service = retrofit.create(JetizzService.class);
            String token = loginResponse.getAccess_token();

            responseCall = service.DesiGuncelle("Bearer " + token, barkod, desi, "MOBIL", "false");

            Log.v("token", loginResponse.getAccess_token());

            alertDialog = CommonUtils.showLoadingDialogWithMessage(this, "Lütfen Bekleyiniz");
            responseCall.enqueue(new Callback<ApiResponseModel>() {
                @Override
                public void onResponse(Call<ApiResponseModel> call, retrofit2.Response<ApiResponseModel> response) {
                    alertDialog.dismiss();
                    if (response.isSuccessful()) {
                        String status = response.body().Status;
                        if (status.equals("success")) {
                            barcodeList.add(barkod);
                            txtOkutulanBarkodSayisi.setText(String.valueOf(barcodeList.size()));
                            edtMnlGonderiNo.setText("");

                        } else {

                            CommonUtils.showAlertWithMesseage(DesiGuncellemeActivity.this, response.body().Message, "UYARI");
//                                ShowDialog.setSnackBar(parentLayout,response.body().Message);
                            vibrate();
                        }


                    } else {
                        alertDialog.dismiss();
                        if (response.raw().code() == 401)
                            CommonUtils.showTokenExpired(DesiGuncellemeActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                        else
                            CommonUtils.showAlertWithMesseage(DesiGuncellemeActivity.this, "İşlem Başarısız", "UYARI");
//                            ShowDialog.setSnackBar(parentLayout,"İşlem Başarısız");
                        vibrate();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponseModel> call, Throwable t) {
                    alertDialog.dismiss();

                    ShowDialog.setSnackBar(parentLayout, "Bağlantıda hata oluştu");
//                        ShowDialog.CreateDialog(getParent(),"Bağlantıda hata oluştu","UYARI");
                    vibrate();
                }
            });
        }

        // If you would like to resume scanning, call this method below:
        mScannerView.startCamera();

        mScannerView.resumeCameraPreview(this);

    }

    public void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
//            for (int i = 0; i < 3; i++) {
//
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    playBeepSound();
//
//                }
//
//            }, 1000);
//        }
            playBeepSound();
        } else {
            //deprecated in API 26
            v.vibrate(1000);
            playBeepSound();
        }
    }

    public MediaPlayer playBeepSound() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.release();
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
//                Log.w(TAG, "Failed to beep " + what + ", " + extra);
                // possibly media player error, so release and recreate
                mp.stop();
                mp.release();
                return true;
            }
        });
        try {
            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.system_fault);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            } finally {
                file.close();
            }

            mediaPlayer.prepare();
            mediaPlayer.start();
            return mediaPlayer;
        } catch (IOException ioe) {
//            Log.w(TAG, ioe);
            mediaPlayer.release();
            return null;
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
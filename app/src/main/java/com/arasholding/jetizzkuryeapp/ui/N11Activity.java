package com.arasholding.jetizzkuryeapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.apimodels.ApiResponseModel;
import com.arasholding.jetizzkuryeapp.apimodels.BaseUrl;
import com.arasholding.jetizzkuryeapp.apimodels.LoginResponse;
import com.arasholding.jetizzkuryeapp.helpers.ShowDialog;
import com.arasholding.jetizzkuryeapp.pref.Prefences;
import com.arasholding.jetizzkuryeapp.service.JetizzService;
import com.arasholding.jetizzkuryeapp.utils.CommonUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.android.BeepManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//import static com.arasholding.jetizzkuryeapp.ui.CameraBarcodeReaderActivity.getUnsafeOkHttpClient;

public class N11Activity extends Activity implements ZXingScannerView.ResultHandler {
    private TextView txtOkutulanBarkodSayisi;
    private ZXingScannerView mScannerView;
    private BeepManager beepManager;
    private LoginResponse loginResponse;
    private EditText edtMnlGonderiNo;
    AlertDialog alertDialog;
    private String barkod;
    private List<String> barcodeList;
    View parentLayout;
    Call<ApiResponseModel> responseCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_n11);

        loginResponse = Prefences.readFromPreferences(getApplicationContext());

        barcodeList = new ArrayList<>();
        mScannerView = (ZXingScannerView) findViewById(R.id.zXingScanner);
        edtMnlGonderiNo = (EditText) findViewById(R.id.edtMnlGonderiNo);
        txtOkutulanBarkodSayisi = findViewById(R.id.txtOkutulanBarkodSayisi);

        List<BarcodeFormat> barcodeFormats = new ArrayList<>();
        barcodeFormats.add(BarcodeFormat.CODE_128);
        barcodeFormats.add(BarcodeFormat.QR_CODE);

        mScannerView.setAspectTolerance(0.5f);
        mScannerView.setFormats(barcodeFormats);
        mScannerView.setAutoFocus(true);
        mScannerView.setResultHandler(this);

        beepManager = new BeepManager(this);

    }

    public void btnManuelOkut(View view) {
        String barkod = edtMnlGonderiNo.getText().toString();

        edtMnlGonderiNo.setText("");

        N11Alimi(barkod);

        Toast.makeText(getApplicationContext(), barkod, Toast.LENGTH_LONG).show();


    }

    @Override
    public void handleResult(Result rawResult) {
        final String barkod = rawResult.getText();

        beepManager.playBeepSoundAndVibrate();

        N11Alimi(barkod);

        Toast.makeText(getApplicationContext(), barkod, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }
    public void N11Alimi (String barkod)
    {
        try {
            if (!barcodeList.contains(barkod)) {
                mScannerView.stopCamera();           // Stop camera on pause
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BaseUrl.Url)
                        .addConverterFactory(GsonConverterFactory.create())
//                        .client(getUnsafeOkHttpClient().build())
                        .build();
                JetizzService service = retrofit.create(JetizzService.class);
                String token = loginResponse.getAccess_token();

                responseCall = service.N11Alimi("Bearer " + token, barkod);

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

                                CommonUtils.showAlertWithMesseage(N11Activity.this, response.body().Message, "UYARI");
//                                ShowDialog.setSnackBar(parentLayout,response.body().Message);
                                vibrate();
                            }


                        } else {
                            alertDialog.dismiss();
                            if (response.raw().code() == 401)
                                CommonUtils.showTokenExpired(N11Activity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                            else
                                CommonUtils.showAlertWithMesseage(N11Activity.this, "İşlem Başarısız", "UYARI");
                            vibrate();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponseModel> call, Throwable t) {
                        alertDialog.dismiss();

                        ShowDialog.setSnackBar(parentLayout, "Bağlantıda hata oluştu");
                        vibrate();
                    }
                });

            }
            mScannerView.startCamera();

            mScannerView.resumeCameraPreview(this);
        }catch (NullPointerException e)
        {
            mScannerView.startCamera();
            Toast.makeText(N11Activity.this, "İşlem sırasında bir hata oluştu", Toast.LENGTH_SHORT).show();
        }

    }
    public void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
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
}
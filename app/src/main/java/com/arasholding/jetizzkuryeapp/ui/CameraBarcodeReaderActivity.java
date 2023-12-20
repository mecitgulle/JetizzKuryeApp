package com.arasholding.jetizzkuryeapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.adapters.SpinnerAdapter;
import com.arasholding.jetizzkuryeapp.apimodels.ApiResponseModel;
import com.arasholding.jetizzkuryeapp.apimodels.ApiResponseModelObj;
import com.arasholding.jetizzkuryeapp.apimodels.BaseUrl;
import com.arasholding.jetizzkuryeapp.apimodels.HareketRequestModel;
import com.arasholding.jetizzkuryeapp.apimodels.LoginResponse;
import com.arasholding.jetizzkuryeapp.apimodels.SpinnerModel;
import com.arasholding.jetizzkuryeapp.helpers.ShowDialog;
import com.arasholding.jetizzkuryeapp.pref.Prefences;
import com.arasholding.jetizzkuryeapp.pref.SharedPref;
import com.arasholding.jetizzkuryeapp.service.JetizzService;
import com.arasholding.jetizzkuryeapp.utils.CommonUtils;
import com.arasholding.jetizzkuryeapp.utils.OkutmaTipleri;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.android.BeepManager;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import dmax.dialog.SpotsDialog;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CameraBarcodeReaderActivity extends Activity implements ZXingScannerView.ResultHandler {

    private static final int REQUEST_ENABLE_BT = 2;
    DrawerLayout drawer;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;
    private String pairedDeviceName;

    private TextView txtOkutulanBarkodSayisi;
    private EditText edtMnlGonderiNo;
    private EditText edtDesi;
    private ZXingScannerView mScannerView;
    private ActionBar actionBar;
    private BeepManager beepManager;

    private ImageView imgBtnFlash;
    private LoginResponse loginResponse;
    List<SpinnerModel> spinnerCustomerList;
    List<SpinnerModel> spinnerBranchList;
    SpinnerAdapter adapter;

    private List<String> barcodeList;
    private String okutmaTipi;
    private int musteriId;
    private int subeId;
    private String MAC_Address;
    private String koordinat;
    AlertDialog alertDialog;
    Call<ApiResponseModel> responseCall;
    Call<ApiResponseModelObj<String[]>> responseCallSubeYukleme;
    View parentLayout;

    //    @BindView(R.id.checkmuskolino)
    CheckBox checkmuskolino;
    CheckBox checkFarkliAcente;
    CheckBox checkBarcodePrinter;
    SearchableSpinner spinner;
    SearchableSpinner subeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_barcode_reader);

        SharedPref sharedPref = new SharedPref(getApplicationContext());
        koordinat = sharedPref.getLatitude() + ";" + sharedPref.getLongitude();
        MAC_Address = Prefences.readFromPreferencesMAC(getApplicationContext());


        Intent intent = getIntent();

        barcodeList = new ArrayList<>();
        loginResponse = Prefences.readFromPreferences(getApplicationContext());

        beepManager = new BeepManager(this);
        parentLayout = findViewById(android.R.id.content);
//        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view

        mScannerView = (ZXingScannerView) findViewById(R.id.zXingScanner);
        txtOkutulanBarkodSayisi = (TextView) findViewById(R.id.txtOkutulanBarkodSayisi);
        edtMnlGonderiNo = (EditText) findViewById(R.id.edtMnlGonderiNo);
        edtDesi = (EditText) findViewById(R.id.edtDesi);
//        checkmuskolino = (CheckBox) findViewById(R.id.checkmuskolino);
        checkFarkliAcente = (CheckBox) findViewById(R.id.checkfarkliacente);
        checkBarcodePrinter = (CheckBox) findViewById(R.id.checkBarcodePrinter);
        spinner = findViewById(R.id.spinner_musterilist);
        spinner.setTitle("Müşteri Seçiniz");
        spinner.setPositiveButton("Tamam");

        subeSpinner = findViewById(R.id.spinner_subelist);
        subeSpinner.setTitle("Şube Seçiniz");
        subeSpinner.setPositiveButton("Tamam");

        okutmaTipi = intent.getStringExtra("okutmaTipi");

        imgBtnFlash = findViewById(R.id.imgBtnFlash);

        List<BarcodeFormat> barcodeFormats = new ArrayList<>();
        barcodeFormats.add(BarcodeFormat.CODE_128);
        barcodeFormats.add(BarcodeFormat.QR_CODE);

        mScannerView.setAspectTolerance(0.5f);
        mScannerView.setFormats(barcodeFormats);
        mScannerView.setAutoFocus(true);
        mScannerView.setResultHandler(this);

        if (okutmaTipi.equals(OkutmaTipleri.SUBEYUKLEME) || okutmaTipi.equals(OkutmaTipleri.TMINDIRME) || okutmaTipi.equals(OkutmaTipleri.TRENDYOLALIM)) {
            subeId = -1;

            if (okutmaTipi.equals(OkutmaTipleri.SUBEYUKLEME)) {

                pairedDeviceName = sharedPref.getPairedDeviceName();
                if (pairedDeviceName == null || pairedDeviceName.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Lütfen ayarlardan bluetooth cihazı seçiniz", Toast.LENGTH_LONG).show();
                }
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                checkBluetooth();

                new BluetoothConnectionAsyncTask().execute();

                checkBarcodePrinter.setVisibility(View.VISIBLE);
            }

            if (!okutmaTipi.equals(OkutmaTipleri.TRENDYOLALIM)) {
                edtDesi.setVisibility(View.VISIBLE);
            }
            spinner.setVisibility(View.VISIBLE);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BaseUrl.Url)
                    .addConverterFactory(GsonConverterFactory.create())
//                    .client(getUnsafeOkHttpClient().build())
                    .build();
            JetizzService service = retrofit.create(JetizzService.class);
            String token = loginResponse.getAccess_token();
            Call<List<SpinnerModel>> responseCall = service.GetMusteriList("Bearer " + token);
            alertDialog = CommonUtils.showLoadingDialogWithMessage(this, "Lütfen Bekleyinizz");
            responseCall.enqueue(new Callback<List<SpinnerModel>>() {
                @Override
                public void onResponse(Call<List<SpinnerModel>> call, Response<List<SpinnerModel>> response) {
                    if (response.isSuccessful()) {
                        alertDialog.dismiss();
                        spinnerCustomerList = response.body();

                        adapter = new SpinnerAdapter(getApplicationContext(), R.layout.spinner_row, spinnerCustomerList);
                        spinner.setAdapter(adapter);

                    } else {
                        if (response.raw().code() == 401)
                            CommonUtils.showTokenExpired(CameraBarcodeReaderActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                        else
                            CommonUtils.showAlertWithMesseage(CameraBarcodeReaderActivity.this, "İşlem Başarısız", "UYARI");
                        alertDialog.dismiss();
//                    Toast.makeText(LoginActivity.this,"login error",Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<List<SpinnerModel>> call, Throwable t) {
                    alertDialog.dismiss();
                    CommonUtils.showAlertWithMesseage(CameraBarcodeReaderActivity.this, t.getMessage(), "UYARI");
//                Toast.makeText(LoginActivity.this,"login ss error",Toast.LENGTH_LONG).show();
                }
            });
        } else if (okutmaTipi.equals(OkutmaTipleri.DAGITIMSUBESEVK)) {
            musteriId = -1;
            subeSpinner.setVisibility(View.VISIBLE);
            checkFarkliAcente.setVisibility(View.VISIBLE);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BaseUrl.Url)
                    .addConverterFactory(GsonConverterFactory.create())
//                    .client(getUnsafeOkHttpClient().build())
                    .build();
            JetizzService service = retrofit.create(JetizzService.class);
            String token = loginResponse.getAccess_token();
            Call<List<SpinnerModel>> responseCall = service.GetSubeler("Bearer " + token);
            alertDialog = CommonUtils.showLoadingDialogWithMessage(this, "Lütfen Bekleyinizz");
            responseCall.enqueue(new Callback<List<SpinnerModel>>() {
                @Override
                public void onResponse(Call<List<SpinnerModel>> call, Response<List<SpinnerModel>> response) {
                    if (response.isSuccessful()) {
                        alertDialog.dismiss();
                        spinnerBranchList = response.body();

                        adapter = new SpinnerAdapter(getApplicationContext(), R.layout.spinner_row, spinnerBranchList);
                        subeSpinner.setAdapter(adapter);

                    } else {
                        if (response.raw().code() == 401)
                            CommonUtils.showTokenExpired(CameraBarcodeReaderActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                        else
                            CommonUtils.showAlertWithMesseage(CameraBarcodeReaderActivity.this, "İşlem Başarısız", "UYARI");
                        alertDialog.dismiss();
//                    Toast.makeText(LoginActivity.this,"login error",Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<List<SpinnerModel>> call, Throwable t) {
                    alertDialog.dismiss();
                    CommonUtils.showAlertWithMesseage(CameraBarcodeReaderActivity.this, t.getMessage(), "UYARI");
//                Toast.makeText(LoginActivity.this,"login ss error",Toast.LENGTH_LONG).show();
                }
            });
        } else {
            musteriId = -1;
            subeId = -1;
        }


//        spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                SpinnerModel model = (SpinnerModel) adapter.getItemObj(position);
//                musteriId = model.getValue();
//            }
//        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerModel model = (SpinnerModel) adapter.getItemObj(position);
                musteriId = model.getValue();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        subeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerModel model = (SpinnerModel) adapter.getItemObj(position);
                subeId = model.getValue();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
        if (drawer != null)
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
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

            try {
                beepManager.playBeepSoundAndVibrate();
            } catch (Exception ex) {

            }

            if (musteriId == 0) {
                CommonUtils.showAlertWithMesseage(CameraBarcodeReaderActivity.this, "Lütfen müşteri seçiniz", "UYARI");
            } else if (subeId == 0) {
                CommonUtils.showAlertWithMesseage(CameraBarcodeReaderActivity.this, "Lütfen şube seçiniz", "UYARI");
            } else {
                if (!barcodeList.contains(rawResult.getText())) {
                    mScannerView.stopCamera();

                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .readTimeout(60, TimeUnit.SECONDS)
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .build();


                    // Stop camera on pause
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BaseUrl.Url)
                            .client(okHttpClient)
//                            .client(getUnsafeOkHttpClient().build())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    JetizzService service = retrofit.create(JetizzService.class);
                    String token = loginResponse.getAccess_token();

                    HareketRequestModel requestModel = new HareketRequestModel();
                    requestModel.setBarkod(rawResult.getText());
                    requestModel.setKaynak("MOBIL");

                    requestModel.setMusteriId(musteriId);
                    requestModel.setSube(String.valueOf(subeId));
                    if (!edtDesi.getText().toString().isEmpty())
                        requestModel.setDesi(Double.parseDouble(edtDesi.getText().toString()));
                    else
                        requestModel.setDesi(0);
                    if (!MAC_Address.isEmpty()) {
                        requestModel.setKaynakIp(MAC_Address);
                    } else {
                        requestModel.setKaynakIp("0");
                    }

                    requestModel.setKoordinat(koordinat);
                    requestModel.setIsKoliNo("false");
                    requestModel.setFarkliAcente(checkFarkliAcente.isChecked());
                    requestModel.setBarkodPrint(checkBarcodePrinter.isChecked());


                    if (okutmaTipi.equals(OkutmaTipleri.MERKEZDENGELEN)) {
                        responseCall = service.MerkezdenGelen("Bearer " + token, rawResult.getText(), "MOBIL", "false");
                    } else if (okutmaTipi.equals(OkutmaTipleri.SUBEYUKLEME)) {
                        responseCallSubeYukleme = service.SubeYuklemeMobil("Bearer " + token, requestModel);
                    } else if (okutmaTipi.equals(OkutmaTipleri.SUBEINDIRME)) {
                        responseCall = service.SubeIndirme("Bearer " + token, rawResult.getText(), "MOBIL", "false");
                    } else if (okutmaTipi.equals(OkutmaTipleri.HATYUKLEME)) {
                        responseCall = service.HatYukleme("Bearer " + token, rawResult.getText(), "MOBIL", "false");
                    } else if (okutmaTipi.equals(OkutmaTipleri.HATINDIRME)) {
                        responseCall = service.HatIndirme("Bearer " + token, rawResult.getText(), "MOBIL", "false");
                    } else if (okutmaTipi.equals(OkutmaTipleri.ZIMMETAL)) {
                        responseCall = service.ZimmetAl("Bearer " + token, rawResult.getText(), null, null, "MOBIL", "false");
                    } else if (okutmaTipi.equals(OkutmaTipleri.TMSEVK)) {
                        responseCall = service.TMSevk("Bearer " + token, rawResult.getText(), "MOBIL", "false");
                    } else if (okutmaTipi.equals(OkutmaTipleri.TMINDIRME)) {
                        responseCall = service.CikisTMIndirmeMobil("Bearer " + token, requestModel);
                    } else if (okutmaTipi.equals(OkutmaTipleri.DAGITIMSUBESEVK)) {
//                        responseCall = service.DagitimSubeSevk("Bearer " + token, rawResult.getText(), "MOBIL", "false");
                        responseCall = service.DagitimSubeSevkRequest("Bearer " + token, requestModel);
                    } else if (okutmaTipi.equals(OkutmaTipleri.TRENDYOLALIM)) {
//                        responseCall = service.DagitimSubeSevk("Bearer " + token, rawResult.getText(), "MOBIL", "false");
                        responseCall = service.GetTrendyolOrder("Bearer " + token, rawResult.getText(), musteriId);
                    }

                    Log.v("token", loginResponse.getAccess_token());

                    alertDialog = CommonUtils.showLoadingDialogWithMessage(this, "Lütfen Bekleyiniz");
                    if (okutmaTipi.equals(OkutmaTipleri.SUBEYUKLEME)) {
                        responseCallSubeYukleme.enqueue(new Callback<ApiResponseModelObj<String[]>>() {
                            @Override
                            public void onResponse(Call<ApiResponseModelObj<String[]>> call, retrofit2.Response<ApiResponseModelObj<String[]>> response) {
                                alertDialog.dismiss();
                                if (response.isSuccessful()) {
                                    String status = response.body().Status;
                                    if (status.equals("success")) {
                                        barcodeList.add(barkod);
                                        txtOkutulanBarkodSayisi.setText(String.valueOf(barcodeList.size()));
                                        edtMnlGonderiNo.setText("");

                                        if(response.body().Result != null){
                                            if (response.body().Result.length > 0) {

                                                writeData(response.body().Result);
                                            }

                                        }

                                    } else {

                                        CommonUtils.showAlertWithMesseage(CameraBarcodeReaderActivity.this, response.body().Message, "UYARI");
//                                ShowDialog.setSnackBar(parentLayout,response.body().Message);
                                        mScannerView.startCamera();
                                        vibrate();
                                    }


                                } else {
                                    alertDialog.dismiss();
                                    if (response.raw().code() == 401)
                                        CommonUtils.showTokenExpired(CameraBarcodeReaderActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                                    else
                                        CommonUtils.showAlertWithMesseage(CameraBarcodeReaderActivity.this, "İşlem Başarısız", "UYARI");
                                    mScannerView.startCamera();
//                            ShowDialog.setSnackBar(parentLayout,"İşlem Başarısız");
                                    vibrate();
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponseModelObj<String[]>> call, Throwable t) {
                                alertDialog.dismiss();

                                ShowDialog.setSnackBar(parentLayout, "Bağlantıda hata oluştu");
//                        ShowDialog.CreateDialog(getParent(),"Bağlantıda hata oluştu","UYARI");
                                vibrate();
                            }
                        });
                    } else {
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

                                        CommonUtils.showAlertWithMesseage(CameraBarcodeReaderActivity.this, response.body().Message, "UYARI");
//                                ShowDialog.setSnackBar(parentLayout,response.body().Message);
                                        mScannerView.startCamera();
                                        vibrate();
                                    }


                                } else {
                                    alertDialog.dismiss();
                                    if (response.raw().code() == 401)
                                        CommonUtils.showTokenExpired(CameraBarcodeReaderActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                                    else
                                        CommonUtils.showAlertWithMesseage(CameraBarcodeReaderActivity.this, "İşlem Başarısız", "UYARI");
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

                }
            }
            // If you would like to resume scanning, call this method below:
            mScannerView.startCamera();

            mScannerView.resumeCameraPreview(this);
        } catch (NullPointerException e) {
            mScannerView.startCamera();
            Toast.makeText(CameraBarcodeReaderActivity.this, "İşlem sırasında bir hata oluştu", Toast.LENGTH_SHORT).show();
        }
    }

    public void btnManuelOkut(View view) {
        String barkod = edtMnlGonderiNo.getText().toString();

        if (musteriId == 0) {
            CommonUtils.showAlertWithMesseage(CameraBarcodeReaderActivity.this, "Lütfen müşteri seçiniz", "UYARI");
        } else if (subeId == 0) {
            CommonUtils.showAlertWithMesseage(CameraBarcodeReaderActivity.this, "Lütfen şube seçiniz", "UYARI");
        } else {
            if (!barcodeList.contains(barkod)) {
                mScannerView.stopCamera();           // Stop camera on pause

                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .readTimeout(60, TimeUnit.SECONDS)
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .build();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BaseUrl.Url)
                        .addConverterFactory(GsonConverterFactory.create())
//                        .client(okHttpClient)
//                        .client(getUnsafeOkHttpClient().build())
                        .build();
                JetizzService service = retrofit.create(JetizzService.class);
                String token = loginResponse.getAccess_token();

                HareketRequestModel requestModel = new HareketRequestModel();
                requestModel.setBarkod(barkod);
                requestModel.setKaynak("MOBIL");
                requestModel.setMusteriId(musteriId);
                requestModel.setSube(String.valueOf(subeId));
                if (!edtDesi.getText().toString().isEmpty())
                    requestModel.setDesi(Double.parseDouble(edtDesi.getText().toString()));
                else
                    requestModel.setDesi(0);
                if (!MAC_Address.isEmpty()) {
                    requestModel.setKaynakIp(MAC_Address);
                } else {
                    requestModel.setKaynakIp("0");
                }
                requestModel.setKoordinat(koordinat);
                requestModel.setIsKoliNo("false");
                requestModel.setFarkliAcente(checkFarkliAcente.isChecked());
                requestModel.setBarkodPrint(checkBarcodePrinter.isChecked());

                if (okutmaTipi.equals(OkutmaTipleri.MERKEZDENGELEN)) {
                    responseCall = service.MerkezdenGelen("Bearer " + token, barkod, "MOBIL", "false");
                } else if (okutmaTipi.equals(OkutmaTipleri.SUBEYUKLEME)) {
                    responseCallSubeYukleme = service.SubeYuklemeMobil("Bearer " + token, requestModel);
                } else if (okutmaTipi.equals(OkutmaTipleri.SUBEINDIRME)) {
                    responseCall = service.SubeIndirme("Bearer " + token, barkod, "MOBIL", "false");
                } else if (okutmaTipi.equals(OkutmaTipleri.HATYUKLEME)) {
                    responseCall = service.HatYukleme("Bearer " + token, barkod, "MOBIL", "false");
                } else if (okutmaTipi.equals(OkutmaTipleri.HATINDIRME)) {
                    responseCall = service.HatIndirme("Bearer " + token, barkod, "MOBIL", "false");
                } else if (okutmaTipi.equals(OkutmaTipleri.ZIMMETAL)) {
                    responseCall = service.ZimmetAl("Bearer " + token, barkod, "", "", "MOBIL", "false");
                } else if (okutmaTipi.equals(OkutmaTipleri.TMSEVK)) {
                    responseCall = service.TMSevk("Bearer " + token, barkod, "MOBIL", "false");
                } else if (okutmaTipi.equals(OkutmaTipleri.TMINDIRME)) {
                    responseCall = service.CikisTMIndirmeMobil("Bearer " + token, requestModel);
                } else if (okutmaTipi.equals(OkutmaTipleri.DAGITIMSUBESEVK)) {
//                    responseCall = service.DagitimSubeSevk("Bearer " + token, barkod, "MOBIL", String.valueOf(checkmuskolino.isChecked()));
                    responseCall = service.DagitimSubeSevkRequest("Bearer " + token, requestModel);
                } else if (okutmaTipi.equals(OkutmaTipleri.TRENDYOLALIM)) {
//                        responseCall = service.DagitimSubeSevk("Bearer " + token, rawResult.getText(), "MOBIL", "false");
                    responseCall = service.GetTrendyolOrder("Bearer " + token, barkod, musteriId);
                }

                Log.v("token", loginResponse.getAccess_token());

                alertDialog = CommonUtils.showLoadingDialogWithMessage(this, "Lütfen Bekleyiniz");
                if (okutmaTipi.equals(OkutmaTipleri.SUBEYUKLEME)) {
                    responseCallSubeYukleme.enqueue(new Callback<ApiResponseModelObj<String[]>>() {
                        @Override
                        public void onResponse(Call<ApiResponseModelObj<String[]>> call, retrofit2.Response<ApiResponseModelObj<String[]>> response) {
                            alertDialog.dismiss();
                            if (response.isSuccessful()) {
                                String status = response.body().Status;
                                if (status.equals("success")) {
                                    barcodeList.add(barkod);
                                    txtOkutulanBarkodSayisi.setText(String.valueOf(barcodeList.size()));
                                    edtMnlGonderiNo.setText("");
                                    if (response.body().Result.length > 0) {

                                        writeData(response.body().Result);
                                    }

                                } else {

                                    CommonUtils.showAlertWithMesseage(CameraBarcodeReaderActivity.this, response.body().Message, "UYARI");
//                                ShowDialog.setSnackBar(parentLayout,response.body().Message);
                                    mScannerView.startCamera();
                                    vibrate();
                                }


                            } else {
                                alertDialog.dismiss();
                                if (response.raw().code() == 401)
                                    CommonUtils.showTokenExpired(CameraBarcodeReaderActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                                else
                                    CommonUtils.showAlertWithMesseage(CameraBarcodeReaderActivity.this, "İşlem Başarısız", "UYARI");
                                mScannerView.startCamera();
//                            ShowDialog.setSnackBar(parentLayout,"İşlem Başarısız");
                                vibrate();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponseModelObj<String[]>> call, Throwable t) {
                            alertDialog.dismiss();

                            ShowDialog.setSnackBar(parentLayout, "Bağlantıda hata oluştu");
//                        ShowDialog.CreateDialog(getParent(),"Bağlantıda hata oluştu","UYARI");
                            vibrate();
                        }
                    });
                } else {
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

                                    CommonUtils.showAlertWithMesseage(CameraBarcodeReaderActivity.this, response.body().Message, "UYARI");
//                                ShowDialog.setSnackBar(parentLayout,response.body().Message);
                                    vibrate();
                                }


                            } else {
                                alertDialog.dismiss();
                                if (response.raw().code() == 401)
                                    CommonUtils.showTokenExpired(CameraBarcodeReaderActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                                else
                                    CommonUtils.showAlertWithMesseage(CameraBarcodeReaderActivity.this, "İşlem Başarısız", "UYARI");
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
            }
        }
        // If you would like to resume scanning, call this method below:
        mScannerView.startCamera();

        mScannerView.resumeCameraPreview(this);

    }

//    @OnCheckedChanged(R.id.checkmuskolino)
//    void onCheckBoxSelected(CompoundButton button, boolean checked) {
//        if (checked) {
//            // perform logic
//        }
//
//    }

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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            closeBT();
        } catch (IOException e) {

        }
    }

    public void closeBT() throws IOException {
        stopWorker = true;
        if (mmInputStream != null && mmSocket != null) {
            mmInputStream.close();
            mmOutputStream.close();
            mmSocket.close();
            Toast.makeText(CameraBarcodeReaderActivity.this, "Bağlantı kapatıldı", Toast.LENGTH_SHORT).show();
        }

    }

    public BluetoothDevice findBT() {
        if (mBluetoothAdapter != null) {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals(pairedDeviceName)) {
                        mmDevice = device;
                        return device;
                    }
                }
            }
        } else {
            Toast.makeText(CameraBarcodeReaderActivity.this, "BLuetooth Cihaz Bulunamadı!", Toast.LENGTH_SHORT).show();
        }
//        myLabel.setText("Bluetooth Device Found");
        return null;
    }

    public void writeData(String[] data) {
        try {

            if (mmOutputStream != null) {
                for (String item : data) {
                    if (item != null && !item.isEmpty()) {
                        mmOutputStream.write(item.getBytes());
                        Toast.makeText(CameraBarcodeReaderActivity.this, "Data Sent", Toast.LENGTH_SHORT).show();
                    }
                }

            } else {
                Toast.makeText(CameraBarcodeReaderActivity.this, "Aktif bir bağlantı bulunmuyor!", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {

            Toast.makeText(CameraBarcodeReaderActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 13; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            Toast.makeText(CameraBarcodeReaderActivity.this, data, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    public void checkBluetooth() {

        if (mBluetoothAdapter == null) {
            Toast.makeText(CameraBarcodeReaderActivity.this, "No bluetooth adapter available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    public void openBT() throws IOException {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

//        beginListenForData();
    }


    public void UpdateLabel(String lbl) {
        Toast.makeText(CameraBarcodeReaderActivity.this, lbl, Toast.LENGTH_SHORT).show();
    }

//    public void ButtonText(String txt) {
//        openButton.setText(txt);
//    }

    public BluetoothSocket getBluetoothSocket() {
        return mmSocket;
    }

    public class BluetoothConnectionAsyncTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            alertDialog = new SpotsDialog.Builder()
                    .setContext(CameraBarcodeReaderActivity.this)
                    .setMessage("Bağlanıyor")
                    .setCancelable(false)
                    .build();

            alertDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String sonuc = "";

            try {
                if ((alertDialog != null) && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                if (findBT() != null) {
                    openBT();
                } else {
                    sonuc = "Bluetooth cihaz bulunamadı";
                }

            } catch (IOException e) {
                sonuc = e.getMessage();
            }

            return sonuc;
        }

        @Override
        protected void onPostExecute(String result) {
            if ((alertDialog != null) && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
            UpdateLabel(result);

            if (getBluetoothSocket() != null) {
                if (getBluetoothSocket().isConnected()) {
                    beginListenForData();
                    Toast.makeText(CameraBarcodeReaderActivity.this, "Cihaz bağlandı : " + mmDevice.getName(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CameraBarcodeReaderActivity.this, result, Toast.LENGTH_SHORT).show();
                }
            }
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

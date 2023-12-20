package com.arasholding.jetizzkuryeapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.apimodels.ApiResponseModel;
import com.arasholding.jetizzkuryeapp.apimodels.BaseUrl;
import com.arasholding.jetizzkuryeapp.apimodels.ErrorMessage;
import com.arasholding.jetizzkuryeapp.apimodels.LoginResponse;
import com.arasholding.jetizzkuryeapp.helpers.ShowDialog;
import com.arasholding.jetizzkuryeapp.pref.Prefences;
import com.arasholding.jetizzkuryeapp.pref.SharedPref;
import com.arasholding.jetizzkuryeapp.service.JetizzService;
import com.arasholding.jetizzkuryeapp.utils.CommonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import dmax.dialog.SpotsDialog;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class N11BluetoothActivity extends BaseActivity {
    private static final int REQUEST_ENABLE_BT = 2;
    DrawerLayout drawer;
    TextView myLabel;
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
    AlertDialog alertDialog;
    Button openButton;
    private LoginResponse loginResponse;
    private String pairedDeviceName;
    private TextView txtOkutulanBarkodSayisi;
    private String okutmaTipi;
    private List<String> barcodeList;
    Call<ApiResponseModel> responseCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_n11_bluetooth);

        Intent intent = getIntent();
        SharedPref sharedPref = new SharedPref(this);
        okutmaTipi = intent.getStringExtra("okutmaTipi");
        loginResponse = Prefences.readFromPreferences(getApplicationContext());
        openButton = findViewById(R.id.open);
        myLabel = findViewById(R.id.label);
        txtOkutulanBarkodSayisi = (TextView) findViewById(R.id.txtOkutulanBarkodSayisi);

        barcodeList = new ArrayList<>();

        openButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new N11BluetoothActivity.BluetoothConnectionAsyncTask().execute();
            }
        });

        pairedDeviceName = sharedPref.getPairedDeviceName();
        if (pairedDeviceName == null || pairedDeviceName.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Lütfen ayarlardan bluetooth cihazı seçiniz", Toast.LENGTH_LONG).show();
            UpdateLabel("Ayarlardan cihaz seçiniz");
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBluetooth();

        new N11BluetoothActivity.BluetoothConnectionAsyncTask().execute();

    }
    public void checkBluetooth() {

        if (mBluetoothAdapter == null) {
            myLabel.setText("No bluetooth adapter available");
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (drawer != null)
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            closeBT();
        } catch (IOException e) {

        }
    }
    public BluetoothSocket getBluetoothSocket() {
        return mmSocket;
    }

    public class BluetoothConnectionAsyncTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            alertDialog = new SpotsDialog.Builder()
                    .setContext(N11BluetoothActivity.this)
                    .setMessage("Bağlanıyor")
                    .setCancelable(false)
                    .build();

            alertDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String sonuc = "";

            try {
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
                    UpdateLabel("Cihaz bağlandı : " + mmDevice.getName());
                    ButtonText("Bağlantıyı Kapat");
                } else {
                    ButtonText("Bağlan");
                    UpdateLabel(result);
                }
            }
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
            UpdateLabel("BLuetooth Cihaz Bulunamadı!");
        }
//        myLabel.setText("Bluetooth Device Found");
        return null;
    }
    public void openBT() throws IOException {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

//        beginListenForData();
    }
    public void closeBT() throws IOException {
        stopWorker = true;
        if (mmInputStream != null && mmSocket != null) {
            mmInputStream.close();
            mmOutputStream.close();
            mmSocket.close();
            myLabel.setText("Bağlantı kapatıldı");
            ButtonText("Bağlan");
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
                                            Toast.makeText(N11BluetoothActivity.this, data, Toast.LENGTH_SHORT).show();
                                            resultData(data);
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
    public void resultData(String barcode) {
        try {
            if (!barcodeList.contains(barcode)) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BaseUrl.Url)
                        .addConverterFactory(GsonConverterFactory.create())
//                        .client(getUnsafeOkHttpClient().build())
                        .build();
                JetizzService service = retrofit.create(JetizzService.class);
                String token = loginResponse.getAccess_token();

                responseCall = service.N11Alimi("Bearer " + token, barcode);

                Log.v("token", loginResponse.getAccess_token());
                alertDialog = CommonUtils.showLoadingDialogWithMessage(this, "Lütfen Bekleyiniz");
                responseCall.enqueue(new Callback<ApiResponseModel>() {
                    @Override
                    public void onResponse(Call<ApiResponseModel> call, retrofit2.Response<ApiResponseModel> response) {
                        alertDialog.dismiss();
                        if (response.isSuccessful()) {
                            String status = response.body().Status;
                            if (status.equals("success")) {

                                barcodeList.add(barcode);
                                txtOkutulanBarkodSayisi.setText(String.valueOf(barcodeList.size()));

                            } else {
                                if (response.body().Errors != null) {
                                    for (ErrorMessage item : response.body().Errors) {
                                        CommonUtils.showAlertWithMesseage(N11BluetoothActivity.this, item.getMessage(), "UYARI");
                                    }
                                }
                                else{
                                    CommonUtils.showAlertWithMesseage(N11BluetoothActivity.this, response.body().Message, "UYARI");
                                }
                                vibrate();
                            }


                        } else {
                            alertDialog.dismiss();
                            if (response.raw().code() == 401)
                                CommonUtils.showTokenExpired(N11BluetoothActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                            else
                                CommonUtils.showAlertWithMesseage(N11BluetoothActivity.this, "İşlem Başarısız", "UYARI");
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
        } catch (NullPointerException e) {

            Toast.makeText(getApplicationContext(), "Birşeyler yanlış, gitti!", Toast.LENGTH_SHORT).show();
        }
    }
    public void UpdateLabel(String lbl) {
        myLabel.setText(lbl);
    }
    public void ButtonText(String txt) {
        openButton.setText(txt);
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
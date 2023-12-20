package com.arasholding.jetizzkuryeapp.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.apimodels.ApiResponseModel;
import com.arasholding.jetizzkuryeapp.apimodels.BaseUrl;
import com.arasholding.jetizzkuryeapp.apimodels.ErrorMessage;
import com.arasholding.jetizzkuryeapp.apimodels.LoginResponse;
import com.arasholding.jetizzkuryeapp.apimodels.MobilDenemeRequest;
import com.arasholding.jetizzkuryeapp.helpers.ShowDialog;
import com.arasholding.jetizzkuryeapp.pref.Prefences;
import com.arasholding.jetizzkuryeapp.pref.SharedPref;
import com.arasholding.jetizzkuryeapp.service.JetizzService;
import com.arasholding.jetizzkuryeapp.utils.CommonUtils;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.zxing.Result;
import com.google.zxing.client.android.BeepManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MecitDenemeBluetoothActivity extends Activity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int PERMISSION_REQUEST_CODE = 101;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    private BeepManager beepManager;
    InputStream mmInputStream;
    EditText edtDesiMik;
    private LoginResponse loginResponse;
    private String okutmaTipi;
    TextView myLabel;
    Button openButton;
    private TextView txtOkutulanBarkodSayisi;
    private List<String> barcodeList;
    private String pairedDeviceName;
    AlertDialog alertDialog;
    DrawerLayout drawer;
    volatile boolean stopWorker;
    int readBufferPosition;
    Thread workerThread;
    byte[] readBuffer;
    private String mecitDeviceToken;
    private String koordinat;
    private String enlem1,boylam1;
    private double enlem, boylam;
    LocationManager konumYoneticisi;
    LocationListener locationListener;
    private String barkod2;
    Uri uri2;
    Call<ApiResponseModel> responseCall;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mecit_deneme_bluetooth);

        Intent intent = getIntent();
        SharedPref sharedPref = new SharedPref(this);
        okutmaTipi = intent.getStringExtra("okutmaTipi");
        loginResponse = Prefences.readFromPreferences(getApplicationContext());
        openButton = findViewById(R.id.open);

        myLabel = findViewById(R.id.label);
        txtOkutulanBarkodSayisi = (TextView) findViewById(R.id.txtOkutulanBarkodSayisi);
//        checkmuskolino = (CheckBox) findViewById(R.id.checkmuskolino);
        enlem1 =  sharedPref.getLatitude();
        boylam1 = sharedPref.getLongitude();
//
//
        koordinat = enlem1 + ";" + boylam1;


//
        Toast.makeText(getApplicationContext(), koordinat, Toast.LENGTH_LONG).show();

        barcodeList = new ArrayList<>();

        openButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new MecitDenemeBluetoothActivity.BluetoothConnectionAsyncTask().execute();
            }
        });

        pairedDeviceName = sharedPref.getPairedDeviceName();
        if (pairedDeviceName == null || pairedDeviceName.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Lütfen ayarlardan bluetooth cihazı seçiniz", Toast.LENGTH_LONG).show();
            UpdateLabel("Ayarlardan cihaz seçiniz");
        }
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult -> {
            mecitDeviceToken = instanceIdResult.getToken();
            Log.e("mecitDeviceToken", mecitDeviceToken);

            getCurrentLocation();

            Toast.makeText(getApplicationContext(), mecitDeviceToken, Toast.LENGTH_LONG).show();
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBluetooth();

        new MecitDenemeBluetoothActivity.BluetoothConnectionAsyncTask().execute();
    }


    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                uri2 = photoURI;
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }

    }
    String currentPhotoPath;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri2);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 5, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();

                String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                Log.e("encoded", encoded);

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BaseUrl.Url)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                JetizzService service = retrofit.create(JetizzService.class);
                String token = loginResponse.getAccess_token();

                MobilDenemeRequest request = new MobilDenemeRequest();

                request.setBarkod(barkod2);
                request.setImage(encoded.substring(0,500));

                Call<ApiResponseModel> responseCall = service.MobilDenemeImageUpload("Bearer " + token, request);
                alertDialog = CommonUtils.showLoadingDialogWithMessage(MecitDenemeBluetoothActivity.this, "Lütfen Bekleyiniz");

                responseCall.enqueue(new Callback<ApiResponseModel>() {
                    @Override
                    public void onResponse(Call<ApiResponseModel> call, Response<ApiResponseModel> response) {
                        if (response.isSuccessful()) {
                            alertDialog.dismiss();
                            if (response.raw().code() == 401)
                                Toast.makeText(MecitDenemeBluetoothActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", Toast.LENGTH_LONG).show();
                            if (response.body() != null) {
                                if (response.body().Errors != null) {
                                    for (ErrorMessage item : response.body().Errors) {
                                        Toast.makeText(MecitDenemeBluetoothActivity.this, item.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    if (response.body().Status.equals("success")) {
                                        alertDialog.dismiss();
                                        Toast.makeText(MecitDenemeBluetoothActivity.this, "İşlem Başarılı",
                                                Toast.LENGTH_SHORT).show();

//                                        Intent intent = new Intent(this, MecitDenemeActivity.class);

                                    }
                                }
                            }
                        } else {

                            if (response.raw().code() == 401)
                                CommonUtils.showTokenExpired(MecitDenemeBluetoothActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                            else
                                CommonUtils.showAlertWithMesseage(MecitDenemeBluetoothActivity.this, "İşlem Başarısız", "UYARI");
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
                        Toast.makeText(MecitDenemeBluetoothActivity.this, t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    public void UpdateLabel(String lbl) {
        myLabel.setText(lbl);
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    public class BluetoothConnectionAsyncTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            alertDialog = new SpotsDialog.Builder()
                    .setContext(MecitDenemeBluetoothActivity.this)
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
                    UpdateLabel("Cihaz" +
                            " bağlandı : " + mmDevice.getName());
                    ButtonText("Bağlantıyı Kapat");
                } else {
                    ButtonText("Bağlan");
                    UpdateLabel(result);
                }
            }
        }
    }
    public class CameraConnectionAsyncTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            alertDialog = new SpotsDialog.Builder()
                    .setContext(MecitDenemeBluetoothActivity.this)
                    .setMessage("Kamera Açılıyor")
                    .setCancelable(false)
                    .build();

            alertDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String sonuc = "";

            try {
                dispatchTakePictureIntent();
            } catch (Exception e) {
                sonuc = e.getMessage();
            }

            return sonuc;
        }

        @Override
        protected void onPostExecute(String result) {
            if ((alertDialog != null) && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
        }
    }
    public BluetoothSocket getBluetoothSocket() {
        return mmSocket;
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
    public void openBT() throws IOException {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

//        beginListenForData();
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
                                            Toast.makeText(MecitDenemeBluetoothActivity.this, data, Toast.LENGTH_SHORT).show();
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
                barkod2 = barcode;
                MobilDenemeRequest request = new MobilDenemeRequest();
                request.setBarkod(barkod2);
                request.setKonum(koordinat);
                request.setDeviceToken(mecitDeviceToken);

                Call<ApiResponseModel> responseCall = service.MobilDeneme("Bearer " + token, request);



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

                                new MecitDenemeBluetoothActivity.CameraConnectionAsyncTask().execute();

                            } else {
                                if (response.body().Errors != null) {
                                    for (ErrorMessage item : response.body().Errors) {
                                        CommonUtils.showAlertWithMesseage(MecitDenemeBluetoothActivity.this, item.getMessage(), "UYARI");
                                    }
                                }
                                vibrate();
                            }


                        } else {
                            alertDialog.dismiss();
                            if (response.raw().code() == 401)
                                CommonUtils.showTokenExpired(MecitDenemeBluetoothActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                            else
                                CommonUtils.showAlertWithMesseage(MecitDenemeBluetoothActivity.this, "İşlem Başarısız", "UYARI");
//                            ShowDialog.setSnackBar(parentLayout,"İşlem Başarısız");
                            vibrate();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponseModel> call, Throwable t) {
                        alertDialog.dismiss();
                        Toast.makeText(getApplicationContext(),"Bağlantıda hata oluştu",Toast.LENGTH_LONG).show();
//                        ShowDialog.setSnackBar(parentLayout, "Bağlantıda hata oluştu");
//                        ShowDialog.CreateDialog(getParent(),"Bağlantıda hata oluştu","UYARI");
                        vibrate();
                    }
                });
            }
        } catch (NullPointerException e) {

            Toast.makeText(getApplicationContext(), "Birşeyler yanlış gitti!", Toast.LENGTH_SHORT).show();
        }
    }
    public void vibrate()
    {
        Vibrator v = (Vibrator )getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
            playBeepSound();
        } else {
            //deprecated in API 26
            v.vibrate(1000);
            playBeepSound();
        }
    }
    protected void getCurrentLocation() {
        konumYoneticisi = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean kontrol = konumYoneticisi.isProviderEnabled("gps");
        if (!kontrol) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MecitDenemeBluetoothActivity.this);
            builder.setTitle("Konum Kullanılsın mı?");
            builder.setMessage("Bu uygulama konum ayarınızı değiştirmek istiyor");
            builder.setCancelable(false);
            builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(MecitDenemeBluetoothActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        }
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                SharedPref sharedPref = new SharedPref(getApplicationContext());
                sharedPref.setLatitude(String.valueOf(location.getLatitude()));
                sharedPref.setLongitude(String.valueOf(location.getLongitude()));
                enlem = location.getLatitude();
                boylam = location.getLongitude();

                koordinat = String.valueOf(enlem) + ";" +String.valueOf(boylam);
                Log.e("enlem", String.valueOf(enlem));
                Log.e("boylam", String.valueOf(boylam));
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String provider) {
//                Toast.makeText(getApplicationContext(), "Konum Açık", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, PERMISSION_REQUEST_CODE);
            } else {
                getLocation();
            }

        } else {
            getLocation();
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
    public void ButtonText(String txt) {
        openButton.setText(txt);
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
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        konumYoneticisi.requestLocationUpdates("gps", 5000, 0, locationListener);
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
            AssetFileDescriptor file =getResources().openRawResourceFd(R.raw.system_fault);
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
package com.arasholding.jetizzkuryeapp.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.apimodels.ApiResponseModel;
import com.arasholding.jetizzkuryeapp.apimodels.BaseUrl;
import com.arasholding.jetizzkuryeapp.apimodels.ErrorMessage;
import com.arasholding.jetizzkuryeapp.apimodels.LoginResponse;
import com.arasholding.jetizzkuryeapp.apimodels.MobilDenemeRequest;
import com.arasholding.jetizzkuryeapp.pref.Prefences;
import com.arasholding.jetizzkuryeapp.pref.SharedPref;
import com.arasholding.jetizzkuryeapp.service.JetizzService;
import com.arasholding.jetizzkuryeapp.utils.CommonUtils;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.android.BeepManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MecitDenemeActivity extends Activity implements ZXingScannerView.ResultHandler, NavigationView.OnNavigationItemSelectedListener {
    private TextView txtOkutulanBarkodSayisi;
    private ZXingScannerView mScannerView;
    private BeepManager beepManager;
    private String mecitDeviceToken;
    private LoginResponse loginResponse;
    AlertDialog alertDialog;
    String atf = "";
    private String filePath;
    String encoded;
    Uri uri2;
    private int izinKontrol;
    LocationManager konumYoneticisi;
    LocationListener locationListener;
    private double enlem, boylam;
    private static final int PERMISSION_REQUEST_CODE = 101;
    private LocationManager locationManager;
    private String barkod2;
    private String enlem1,boylam1;
    private String koordinat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mecit_deneme);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        izinKontrol = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (izinKontrol != PackageManager.PERMISSION_GRANTED){
            ActivityCompat
                    .requestPermissions(MecitDenemeActivity.this
                    ,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                    ,100);
        }else {

        }


        loginResponse = Prefences.readFromPreferences(getApplicationContext());
        SharedPref sharedPref = new SharedPref(getApplicationContext());
        enlem1 =  sharedPref.getLatitude();
        boylam1 = sharedPref.getLongitude();
//
//
        koordinat = enlem1 + ";" + boylam1;


//
        Toast.makeText(getApplicationContext(), koordinat, Toast.LENGTH_LONG).show();





        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult -> {
            mecitDeviceToken = instanceIdResult.getToken();
            Log.e("mecitDeviceToken", mecitDeviceToken);

            getCurrentLocation();

            Toast.makeText(getApplicationContext(), mecitDeviceToken, Toast.LENGTH_LONG).show();
        });


        mScannerView = (ZXingScannerView) findViewById(R.id.zXingScanner);
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

    //    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults){
//        if(requestCode == 100){
//            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                Toast.makeText(getApplicationContext(),"İZİN VERİLDİ",Toast.LENGTH_LONG).show();
//            }else {
//                Toast.makeText(getApplicationContext(),"İZİN VERİLMEDİ",Toast.LENGTH_LONG).show();
//            }
//        }
//    }
    @Override
    public void handleResult(Result rawResult) {
        final String barkod = rawResult.getText();
        barkod2 = barkod;
        Toast.makeText(getApplicationContext(), barkod, Toast.LENGTH_LONG).show();

        beepManager.playBeepSoundAndVibrate();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseUrl.Url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JetizzService service = retrofit.create(JetizzService.class);
        String token = loginResponse.getAccess_token();

        MobilDenemeRequest request = new MobilDenemeRequest();
//        request.setKonum(koordinat);
        request.setBarkod(barkod);
        request.setKonum(koordinat);
        request.setDeviceToken(mecitDeviceToken);

        Call<ApiResponseModel> responseCall = service.MobilDeneme("Bearer " + token, request);

        alertDialog = CommonUtils.showLoadingDialogWithMessage(MecitDenemeActivity.this, "Lütfen Bekleyiniz");

        responseCall.enqueue(new Callback<ApiResponseModel>() {
            @Override
            public void onResponse(Call<ApiResponseModel> call, Response<ApiResponseModel> response) {
                if (response.isSuccessful()) {
                    alertDialog.dismiss();
                    if (response.raw().code() == 401)
                        Toast.makeText(MecitDenemeActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", Toast.LENGTH_LONG).show();
                    if (response.body() != null) {
                        if (response.body().Errors != null) {
                            for (ErrorMessage item : response.body().Errors) {
                                Toast.makeText(MecitDenemeActivity.this, item.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (response.body().Status.equals("success")) {
                                alertDialog.dismiss();
                                Toast.makeText(MecitDenemeActivity.this, "İşlem Başarılı",
                                        Toast.LENGTH_SHORT).show();

                                dispatchTakePictureIntent();


                            }
                        }
                    }
                } else {

                    if (response.raw().code() == 401)
                        CommonUtils.showTokenExpired(MecitDenemeActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                    else
                        CommonUtils.showAlertWithMesseage(MecitDenemeActivity.this, "İşlem Başarısız", "UYARI");
                    alertDialog.dismiss();

//                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponseModel> call, Throwable t) {
                Toast.makeText(MecitDenemeActivity.this, t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        mScannerView.setResultHandler(this);
        mScannerView.startCamera();

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
                alertDialog = CommonUtils.showLoadingDialogWithMessage(MecitDenemeActivity.this, "Lütfen Bekleyiniz");

                responseCall.enqueue(new Callback<ApiResponseModel>() {
                    @Override
                    public void onResponse(Call<ApiResponseModel> call, Response<ApiResponseModel> response) {
                        if (response.isSuccessful()) {
                            alertDialog.dismiss();
                            if (response.raw().code() == 401)
                                Toast.makeText(MecitDenemeActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", Toast.LENGTH_LONG).show();
                            if (response.body() != null) {
                                if (response.body().Errors != null) {
                                    for (ErrorMessage item : response.body().Errors) {
                                        Toast.makeText(MecitDenemeActivity.this, item.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    if (response.body().Status.equals("success")) {
                                        alertDialog.dismiss();
                                        Toast.makeText(MecitDenemeActivity.this, "İşlem Başarılı",
                                                Toast.LENGTH_SHORT).show();

                                        dispatchTakePictureIntent();

//                                        Intent intent = new Intent(this, MecitDenemeActivity.class);



                                    }
                                }
                            }
                        } else {

                            if (response.raw().code() == 401)
                                CommonUtils.showTokenExpired(MecitDenemeActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                            else
                                CommonUtils.showAlertWithMesseage(MecitDenemeActivity.this, "İşlem Başarısız", "UYARI");
                            alertDialog.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponseModel> call, Throwable t) {
                        Toast.makeText(MecitDenemeActivity.this, t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    String currentPhotoPath;

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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    protected void getCurrentLocation() {
        konumYoneticisi = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean kontrol = konumYoneticisi.isProviderEnabled("gps");
        if (!kontrol) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MecitDenemeActivity.this);
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
                    Intent intent = new Intent(MecitDenemeActivity.this, MainActivity.class);
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

//    @Override
//    public void onLocationChanged(@NonNull Location location) {
//        double enlem = location.getLatitude();
//        double boylam = location.getLongitude();
//
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//
//    }
//
//    @Override
//    public void onProviderEnabled(@NonNull String provider) {
//
//    }
//
//    @Override
//    public void onProviderDisabled(@NonNull String provider) {
//
//    }
}
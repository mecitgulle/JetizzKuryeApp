package com.arasholding.jetizzkuryeapp.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.ui.AppBarConfiguration;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.apimodels.BaseUrl;
import com.arasholding.jetizzkuryeapp.apimodels.LoginResponse;
import com.arasholding.jetizzkuryeapp.pref.Prefences;
import com.arasholding.jetizzkuryeapp.pref.SharedPref;
import com.arasholding.jetizzkuryeapp.service.JetizzService;
import com.arasholding.jetizzkuryeapp.utils.CommonUtils;
import com.arasholding.jetizzkuryeapp.utils.OkutmaTipleri;
import com.google.android.material.navigation.NavigationView;
import com.infideap.drawerbehavior.AdvanceDrawerLayout;
import com.jaredrummler.materialspinner.MaterialSpinner;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//import static com.arasholding.jetizzkuryeapp.ui.CameraBarcodeReaderActivity.getUnsafeOkHttpClient;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private AppBarConfiguration mAppBarConfiguration;
    private AdvanceDrawerLayout drawer;
    private Class<?> mClss;
    private static final int ZXING_CAMERA_PERMISSION = 1;
    private static final int PERMISSION_REQUEST_CODE = 101;
    LocationManager konumYoneticisi;
    LocationListener locationListener;
    private double enlem, boylam;
    String[] permissions = {"android.permission.BLUETOOTH_CONNECT"};

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        drawer.setViewScale(Gravity.START, 0.9f);
        drawer.setRadius(Gravity.START, 35);
        drawer.setViewElevation(Gravity.START, 20);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_subeyukleme, R.id.nav_merkezdengelen,
                R.id.nav_zimmetealma, R.id.nav_zimmet_listesi, R.id.nav_teslim_iade, R.id.nav_TM_Sevk, R.id.nav_TM_Indirme, R.id.nav_tools, R.id.nav_session)
                .setDrawerLayout(drawer)
                .build();

        navigationView.setNavigationItemSelectedListener(this);


        try {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            String address = wm.getConnectionInfo().getMacAddress();
//            String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
            Prefences.saveToPreferencesMAC(getApplicationContext(), address);
        } catch (Exception ex) {
            //
        }

        surumKontrolEt();
        getCurrentLocation();

//        startJob();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.logout) {
            showLogoutDialog();
        }
        if (id == R.id.time) {
            showGunlukTeslimatDialog();
        }


        return super.onOptionsItemSelected(item);
    }


    public void showLogoutDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Çıkmak istediğinize emin misiniz?");
        alertDialog.setIcon(R.drawable.ic_exit_to_app_blackk_24dp);


        alertDialog.setPositiveButton("Evet", (dialog, which) -> {
            dialog.dismiss();
            LoginResponse response = new LoginResponse();
            response.setAccess_token(null);
            response.setExpires_in(0);
            response.setToken_type(null);
            Prefences.saveToPreferences(getApplicationContext(), response);
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        alertDialog.setNegativeButton("Hayır", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = alertDialog.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(getResources().getColor(android.R.color.transparent));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.gray));
            }
        });
        dialog.show();
//        alertDialog.show();
    }

    public void showGunlukTeslimatDialog() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseUrl.Url)
                .addConverterFactory(GsonConverterFactory.create())
//                .client(getUnsafeOkHttpClient().build())
                .build();
        JetizzService service = retrofit.create(JetizzService.class);
        LoginResponse loginResponse = Prefences.readFromPreferences(getApplicationContext());
        String token = loginResponse.getAccess_token();


        Call<Integer> responseCall = service.GunlukTeslimatSayisi("Bearer " + token);

        Log.v("token", loginResponse.getAccess_token());
        AlertDialog alertDialog;
        alertDialog = CommonUtils.showLoadingDialogWithMessage(this, "Lütfen Bekleyiniz");
        responseCall.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, retrofit2.Response<Integer> response) {
                alertDialog.dismiss();
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        int sonuc = response.body();
                        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                        View decide_layout = inflater.inflate(R.layout.gunlukteslimat_dialog, null);

                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogTheme);
                        alertDialog.setTitle(Html.fromHtml("<font color='#d34836'>Bugünkü Teslimat Sayınız!</font>"));
                        alertDialog.setView(decide_layout);
//        alertDialog.setIcon(R.drawable.ic_perm_device_information_black_24dp);

                        TextView txtTeslimatSayisi = decide_layout.findViewById(R.id.txtTeslimatSayisi);
                        txtTeslimatSayisi.setText(String.valueOf(sonuc));

                        alertDialog.setView(decide_layout);

                        alertDialog.show();
                    }


                } else {
                    alertDialog.dismiss();
                    if (response.raw().code() == 401)
                        CommonUtils.showTokenExpired(MainActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                    else
                        CommonUtils.showAlertWithMesseage(MainActivity.this, "İşlem Başarısız", "UYARI");

                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                alertDialog.dismiss();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_subeyukleme) {
            launchActivity(CameraBarcodeReaderActivity.class, OkutmaTipleri.SUBEYUKLEME);
            return true;
        } else if (id == R.id.nav_merkezdengelen) {
            showDecideDialog(OkutmaTipleri.MERKEZDENGELEN);
            return true;
        } else if (id == R.id.nav_zimmetealma) {
            showDecideDialog(OkutmaTipleri.ZIMMETAL);
            return true;
        } else if (id == R.id.nav_TM_Sevk) {
            showDecideDialog(OkutmaTipleri.TMSEVK);
            return true;
        } else if (id == R.id.nav_TM_Indirme) {
            showDecideDialog(OkutmaTipleri.TMINDIRME);
            return true;
        }
//        else if (id == R.id.nav_dagitim_sube_sevk) {
//            showDecideDialog(OkutmaTipleri.DAGITIMSUBESEVK);
//            return true;
//        }
        else if (id == R.id.nav_zimmet_listesi) {
            Intent intent = new Intent(this, ZimmetListActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_teslim_iade) {
            Intent intent = new Intent(this, DeliveryActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_tools) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_session) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }

    public void showDecideDialog(final String islemTipi) {

        LayoutInflater inflater = this.getLayoutInflater();
        View decide_layout = inflater.inflate(R.layout.decide_dialog, null);
//        chkRemember = decide_layout.findViewById(R.id.chkRemember);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Barkod Çekim Yöntemi Seçiniz!");
        alertDialog.setView(decide_layout);
//        alertDialog.setIcon(R.drawable.ic_perm_device_information_black_24dp);

        final MaterialSpinner spinner = decide_layout.findViewById(R.id.statusSpanner);

        spinner.setItems("Kamera", "Bluetooth", "Lazer");

//

//        chkRemember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                AppController.saveToPreferences(getApplicationContext(), PrefKeys.PREF_KEY_REMEMBER_CHECKED, chkRemember.isChecked());
//
//            }
//        });

        alertDialog.setView(decide_layout);

        alertDialog.setPositiveButton("SEÇ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Tamam butonuna basılınca yapılacaklar
                if (spinner.getSelectedIndex() == 0) {

                    if (islemTipi.equals(OkutmaTipleri.MERKEZDENGELEN)) {
                        launchActivity(CameraBarcodeReaderActivity.class, OkutmaTipleri.MERKEZDENGELEN);
                    } else if (islemTipi.equals(OkutmaTipleri.SUBEYUKLEME)) {
                        launchActivity(CameraBarcodeReaderActivity.class, OkutmaTipleri.SUBEYUKLEME);
                    } else if (islemTipi.equals(OkutmaTipleri.ZIMMETAL)) {
                        launchActivity(CameraBarcodeReaderActivity.class, OkutmaTipleri.ZIMMETAL);
                    } else if (islemTipi.equals(OkutmaTipleri.SUBEINDIRME)) {
                        launchActivity(CameraBarcodeReaderActivity.class, OkutmaTipleri.SUBEINDIRME);
                    } else if (islemTipi.equals(OkutmaTipleri.HATYUKLEME)) {
                        launchActivity(CameraBarcodeReaderActivity.class, OkutmaTipleri.HATYUKLEME);
                    } else if (islemTipi.equals(OkutmaTipleri.HATINDIRME)) {
                        launchActivity(CameraBarcodeReaderActivity.class, OkutmaTipleri.HATINDIRME);
                    } else if (islemTipi.equals(OkutmaTipleri.TMSEVK)) {
                        launchActivity(CameraBarcodeReaderActivity.class, OkutmaTipleri.TMSEVK);
                    } else if (islemTipi.equals(OkutmaTipleri.TMINDIRME)) {
                        launchActivity(CameraBarcodeReaderActivity.class, OkutmaTipleri.TMINDIRME);
                    } else if (islemTipi.equals(OkutmaTipleri.DAGITIMSUBESEVK)) {
                        launchActivity(CameraBarcodeReaderActivity.class, OkutmaTipleri.DAGITIMSUBESEVK);
                    } else if (islemTipi.equals(OkutmaTipleri.DESIGUNCELLEME)) {
                        launchActivity(DesiGuncellemeActivity.class, OkutmaTipleri.DESIGUNCELLEME);
                    } else if (islemTipi.equals(OkutmaTipleri.TRENDYOLALIM)) {
                        launchActivity(CameraBarcodeReaderActivity.class, OkutmaTipleri.TRENDYOLALIM);
                    } else if (islemTipi.equals(OkutmaTipleri.MECITDENEME)) {
                        launchActivity(MecitDenemeActivity.class, OkutmaTipleri.MECITDENEME);
                    } else if (islemTipi.equals(OkutmaTipleri.N11BARKOD)) {
                        launchActivity(N11Activity.class, OkutmaTipleri.N11BARKOD);
                    } else if (islemTipi.equals(OkutmaTipleri.MUSTERIIADE)) {
                        launchActivity(MusteriIadeActivity.class, OkutmaTipleri.MUSTERIIADE);
                    }


                } else if (spinner.getSelectedIndex() == 2) {
                    if (islemTipi.equals(OkutmaTipleri.MERKEZDENGELEN)) {
//                        startActivity(new Intent(getBaseContext(), BluetoothScannerActivity.class));
                        launchActivity(LazerBarcodeReaderActivity.class, OkutmaTipleri.MERKEZDENGELEN);
                    } else if (islemTipi.equals(OkutmaTipleri.SUBEYUKLEME)) {
                        launchActivity(LazerBarcodeReaderActivity.class, OkutmaTipleri.SUBEYUKLEME);
                    } else if (islemTipi.equals(OkutmaTipleri.ZIMMETAL)) {
                        launchActivity(LazerBarcodeReaderActivity.class, OkutmaTipleri.ZIMMETAL);
                    } else if (islemTipi.equals(OkutmaTipleri.SUBEINDIRME)) {
                        launchActivity(LazerBarcodeReaderActivity.class, OkutmaTipleri.SUBEINDIRME);
                    } else if (islemTipi.equals(OkutmaTipleri.HATYUKLEME)) {
                        launchActivity(LazerBarcodeReaderActivity.class, OkutmaTipleri.HATYUKLEME);
                    } else if (islemTipi.equals(OkutmaTipleri.HATINDIRME)) {
                        launchActivity(LazerBarcodeReaderActivity.class, OkutmaTipleri.HATINDIRME);
                    } else if (islemTipi.equals(OkutmaTipleri.TMSEVK)) {
                        launchActivity(LazerBarcodeReaderActivity.class, OkutmaTipleri.TMSEVK);
                    } else if (islemTipi.equals(OkutmaTipleri.TMINDIRME)) {
                        launchActivity(LazerBarcodeReaderActivity.class, OkutmaTipleri.TMINDIRME);
                    } else if (islemTipi.equals(OkutmaTipleri.DAGITIMSUBESEVK)) {
                        launchActivity(LazerBarcodeReaderActivity.class, OkutmaTipleri.DAGITIMSUBESEVK);
                    } else if (islemTipi.equals(OkutmaTipleri.DESIGUNCELLEME)) {
                        launchActivity(DesiGuncellemeBluetoothActivity.class, OkutmaTipleri.DESIGUNCELLEME);
                    } else if (islemTipi.equals(OkutmaTipleri.TRENDYOLALIM)) {
                        launchActivity(LazerBarcodeReaderActivity.class, OkutmaTipleri.TRENDYOLALIM);
                    }
                } else {
                    if (islemTipi.equals(OkutmaTipleri.MERKEZDENGELEN)) {
//                        startActivity(new Intent(getBaseContext(), BluetoothScannerActivity.class));
                        launchActivityBluetooth(BluetoothBarcodeReaderActivity.class, OkutmaTipleri.MERKEZDENGELEN);
                    } else if (islemTipi.equals(OkutmaTipleri.SUBEYUKLEME)) {
                        launchActivityBluetooth(BluetoothBarcodeReaderActivity.class, OkutmaTipleri.SUBEYUKLEME);
                    } else if (islemTipi.equals(OkutmaTipleri.ZIMMETAL)) {
                        launchActivityBluetooth(BluetoothBarcodeReaderActivity.class, OkutmaTipleri.ZIMMETAL);
                    } else if (islemTipi.equals(OkutmaTipleri.SUBEINDIRME)) {
                        launchActivityBluetooth(BluetoothBarcodeReaderActivity.class, OkutmaTipleri.SUBEINDIRME);
                    } else if (islemTipi.equals(OkutmaTipleri.HATYUKLEME)) {
                        launchActivityBluetooth(BluetoothBarcodeReaderActivity.class, OkutmaTipleri.HATYUKLEME);
                    } else if (islemTipi.equals(OkutmaTipleri.HATINDIRME)) {
                        launchActivityBluetooth(BluetoothBarcodeReaderActivity.class, OkutmaTipleri.HATINDIRME);
                    } else if (islemTipi.equals(OkutmaTipleri.TMSEVK)) {
                        launchActivityBluetooth(BluetoothBarcodeReaderActivity.class, OkutmaTipleri.TMSEVK);
                    } else if (islemTipi.equals(OkutmaTipleri.TMINDIRME)) {
                        launchActivityBluetooth(BluetoothBarcodeReaderActivity.class, OkutmaTipleri.TMINDIRME);
                    } else if (islemTipi.equals(OkutmaTipleri.DAGITIMSUBESEVK)) {
                        launchActivityBluetooth(BluetoothBarcodeReaderActivity.class, OkutmaTipleri.DAGITIMSUBESEVK);
                    } else if (islemTipi.equals(OkutmaTipleri.DESIGUNCELLEME)) {
                        launchActivityBluetooth(DesiGuncellemeBluetoothActivity.class, OkutmaTipleri.DESIGUNCELLEME);
                    } else if (islemTipi.equals(OkutmaTipleri.TRENDYOLALIM)) {
                        launchActivityBluetooth(BluetoothBarcodeReaderActivity.class, OkutmaTipleri.TRENDYOLALIM);
                    } else if (islemTipi.equals(OkutmaTipleri.MECITDENEME)) {
                        launchActivityBluetooth(MecitDenemeBluetoothActivity.class, OkutmaTipleri.MECITDENEME);
                    } else if (islemTipi.equals(OkutmaTipleri.MUSTERIIADE)) {
                        launchActivityBluetooth(MusteriIadeBluetoothActivity.class, OkutmaTipleri.MUSTERIIADE);
                    }

//                    else if (islemTipi.equals(OkutmaTipleri.N11BARKOD)) {
//                        launchActivity(N11BluetoothActivity.class, OkutmaTipleri.N11BARKOD);
//                    }

                }

            }
        });
//        alertDialog.setNegativeButton("HAYIR", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//
//                //İptal butonuna basılınca yapılacaklar.Sadece kapanması isteniyorsa boş bırakılacak
//
//            }
//
//        });
        AlertDialog dialog = alertDialog.create();
//        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface arg0) {
//                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(getResources().getColor(R.color.dark_gray));
//            }
//        });
        dialog.show();
    }

    public void launchActivity(Class<?> clss, String okutmaTipi) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            mClss = clss;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
        } else {
            Intent intent = new Intent(this, clss);
            intent.putExtra("okutmaTipi", okutmaTipi);
            startActivity(intent);
        }
    }
    public void launchActivityBluetooth(Class<?> clss, String okutmaTipi)
        {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, 2);
        }
        else {
            Intent intent = new Intent(this, clss);
            intent.putExtra("okutmaTipi", okutmaTipi);
            startActivity(intent);
        }
    }

    public void btnMerkezdenGelen(View v) {
        showDecideDialog(OkutmaTipleri.MERKEZDENGELEN);
    }

    public void btnSubeYukleme(View v) {
        showDecideDialog(OkutmaTipleri.SUBEYUKLEME);
    }

    public void btnSubeIndirme(View v) {
        showDecideDialog(OkutmaTipleri.SUBEINDIRME);
    }

    public void btnHatYukleme(View v) {
        showDecideDialog(OkutmaTipleri.HATYUKLEME);
    }

    public void btnHatIndirme(View v) {
        showDecideDialog(OkutmaTipleri.HATINDIRME);
    }

    public void btnZimmetAl(View v) {
        showDecideDialog(OkutmaTipleri.ZIMMETAL);
    }

    public void btnCikisTMIndirme(View v) {
        showDecideDialog(OkutmaTipleri.TMINDIRME);
    }

    public void btnDagitimSubeSevk(View v) {
        showDecideDialog(OkutmaTipleri.DAGITIMSUBESEVK);
    }

    public void btnTrendyol(View v) {
        showDecideDialog(OkutmaTipleri.TRENDYOLALIM);
    }

    public void btnIslemler(View v) {
        Intent intent = new Intent(this, DeliveryActivity.class);
        startActivity(intent);
    }

    public void btnzimmetListesi(View v) {
        Intent intent = new Intent(this, ZimmetListActivity.class);
        startActivity(intent);
    }

    public void btnMecitdenemelist(View v) {
        Intent intent = new Intent(this, MobilDenemeListActivity.class);
        startActivity(intent);
    }

    public void btnBarkodOkut(View v) {
        Intent intent = new Intent(this, BarkodileTeslimActivity.class);
        startActivity(intent);
    }


    public void btnGelenGonderiler(View v) {
        Intent intent = new Intent(this, GelenGonderiActivity.class);
        startActivity(intent);
    }

    public void btnNotbirakildi(View v) {
        Intent intent = new Intent(this, GelenGonderiActivity.class);
        intent.putExtra("notBirakildi", "notBirakildi");
        startActivity(intent);
    }

    public void btnDesiGuncelle(View v) {
        showDecideDialog(OkutmaTipleri.DESIGUNCELLEME);
//        Intent intent = new Intent(this, DesiGuncellemeActivity.class);
//        startActivity(intent);
    }

    public void btnMecitdeneme(View v) {
        showDecideDialog(OkutmaTipleri.MECITDENEME);
//        Intent intent = new Intent(this, DesiGuncellemeActivity.class);
//        startActivity(intent);
    }

    public void btnN11barkod(View v) {
//        showDecideDialog(OkutmaTipleri.N11BARKOD);
        Intent intent = new Intent(this, N11Activity.class);
        startActivity(intent);
    }

    public void btnIadebarkod(View v) {
        showDecideDialog(OkutmaTipleri.MUSTERIIADE);
    }


    protected void getCurrentLocation() {
        konumYoneticisi = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean kontrol = konumYoneticisi.isProviderEnabled("gps");
        if (!kontrol) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
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
//                Log.e("enlem", sharedPref.getLatitude());
//                Log.e("boylam", sharedPref.getLongitude());
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
        konumYoneticisi.requestLocationUpdates("gps", 5000, 0, locationListener);
    }

    private void surumKontrolEt() {
        try {
            String appVersionName = getApplicationContext().getPackageManager()
                    .getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BaseUrl.Url)
                    .addConverterFactory(GsonConverterFactory.create())
//                    .client(getUnsafeOkHttpClient().build())
                    .build();
            JetizzService service = retrofit.create(JetizzService.class);
            LoginResponse loginResponse = Prefences.readFromPreferences(getApplicationContext());
            String token = loginResponse.getAccess_token();


            Call<String> responseCall = service.GetUygulamaSurumu("Bearer " + token);

            Log.v("token", loginResponse.getAccess_token());
//            AlertDialog alertDialog;
//            alertDialog = CommonUtils.showLoadingDialogWithMessage(this, "Lütfen Bekleyiniz");
            responseCall.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, retrofit2.Response<String> response) {
//                    alertDialog.dismiss();
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            String sonuc = response.body();
                            if (appVersionName.equals(sonuc)) {

                            } else {
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                alertDialog.setTitle("Uygulama Güncelleme");
                                alertDialog.setMessage("Uygulamanın yeni versiyonu mevcut. \nLütfen güncelleyiniz.\nMevcut Version: " + appVersionName + "\nYeni Version: " + sonuc);
//                                alertDialog.setIcon(R.drawable.ic_rotation);
                                alertDialog.setCancelable(false);

                                alertDialog.setPositiveButton("Güncelle", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                        dialog.cancel();

                                        try {
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
                                            startActivity(intent);

                                        } catch (android.content.ActivityNotFoundException anfe) {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                        }

                                        finish();
                                    }
                                });

                                alertDialog.setNegativeButton("ÇIKIŞ", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
//                    uygulamayiSonlandir();
                                    }
                                });

                                try {
                                    alertDialog.show();
                                } catch (Exception e) {
                                    // WindowManager$BadTokenException will be caught and the app would not display
                                    // the 'Force Close' message
                                }
                            }


                        } else {
//                            alertDialog.dismiss();
                            if (response.raw().code() == 401)
                                CommonUtils.showTokenExpired(MainActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                            else
                                CommonUtils.showAlertWithMesseage(MainActivity.this, "İşlem Başarısız", "UYARI");

                        }
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
//                    alertDialog.dismiss();
                }
            });

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


    }
}

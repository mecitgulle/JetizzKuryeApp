package com.arasholding.jetizzkuryeapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.adapters.TabAdapter;
import com.arasholding.jetizzkuryeapp.apimodels.BaseUrl;
import com.arasholding.jetizzkuryeapp.apimodels.LoginResponse;
import com.arasholding.jetizzkuryeapp.apimodels.ZimmetListModel;
import com.arasholding.jetizzkuryeapp.helpers.ShowDialog;
import com.arasholding.jetizzkuryeapp.pref.Prefences;
import com.arasholding.jetizzkuryeapp.pref.SharedPref;
import com.arasholding.jetizzkuryeapp.service.JetizzService;
import com.arasholding.jetizzkuryeapp.ui.fragment.DeliveryFragment;
import com.arasholding.jetizzkuryeapp.ui.fragment.ReturnFragment;
import com.arasholding.jetizzkuryeapp.utils.CommonUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//import static com.arasholding.jetizzkuryeapp.ui.CameraBarcodeReaderActivity.getUnsafeOkHttpClient;

public class DeliveryActivity extends AppCompatActivity {
    private TabAdapter tabAdapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Unbinder unbinder;
    private EditText edtGonderiTakipNo;
    private Button btnGonderiSorgula;
    private LoginResponse loginResponse;
    List<ZimmetListModel> list;
    Call<List<ZimmetListModel>> responseCall;
    AlertDialog alertDialog;
    View parentLayout;
    int atfId;
    int musteriId;
    String koordinat;
    LocationManager konumYoneticisi;
    LocationListener locationListener;
    private double enlem, boylam;
    private static final int PERMISSION_REQUEST_CODE = 101;
    public static final int REQUEST_CALL_PHONE = 1;
    
    private String hizmet;

    @BindView(R.id.txtViewGonderici)
    TextView gonderici;
    @BindView(R.id.txtViewAlici)
    TextView alici;

    @BindView(R.id.txtViewGonderiNo)
    TextView gonderiNo;

    @BindView(R.id.txtViewSonHareket)
    TextView sonHareket;

    @BindView(R.id.txtViewDesi)
    TextView desi;

    @BindView(R.id.txtViewZimmeteAlan)
    TextView zimmeteAlan;

    @BindView(R.id.txtViewTahsilatTipi)
    TextView tahsilatTipi;

    @BindView(R.id.txtViewTahsilatTutari)
    TextView tahsilatTutari;

    @BindView(R.id.btnAliciTel)
    Button aliciTel;

    String telefon;
    double tutar=0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);
        unbinder = ButterKnife.bind(this);
        Intent i = getIntent();
        loginResponse = Prefences.readFromPreferences(getApplicationContext());

        edtGonderiTakipNo = (EditText) findViewById(R.id.edtGonderiTakipNo);
        btnGonderiSorgula = findViewById(R.id.btnGonderiSorgula);
        alici.setText(i.getStringExtra("tempAlici"));
        gonderici.setText(i.getStringExtra("tempGonderici"));
        atfId = i.getIntExtra("tempAtfId", 0);
        musteriId = i.getIntExtra("tempMusteriId",0);
        gonderiNo.setText(i.getStringExtra("tempAtfNo"));
        sonHareket.setText(i.getStringExtra("tempSonIslem"));
        hizmet = i.getStringExtra("tempHizmet");
        desi.setText(i.getStringExtra("tempDesi"));
        zimmeteAlan.setText(i.getStringExtra("tempZimmetAlan"));
        aliciTel.setText(i.getStringExtra("tempAliciTelefon"));
        telefon = i.getStringExtra("tempAliciTelefon");
        if (i.getStringExtra("tempTahTip")!= null){
            LinearLayout lytTip = findViewById(R.id.lytTahTip);
            lytTip.setVisibility(View.VISIBLE);
            tahsilatTipi.setText(i.getStringExtra("tempTahTip"));
        }
        tutar = i.getDoubleExtra("tempTahTur",0);
        if ( tutar > 0.0   ){
            LinearLayout lytTut = findViewById(R.id.lytTahTur);
            lytTut.setVisibility(View.VISIBLE);
            tahsilatTutari.setText(String.valueOf(i.getDoubleExtra("tempTahTur",0)));
        }

//        getCurrentLocation();
        setUp();
     btnGonderiSorgula.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetGonderi();
            }
        });

    }
    public   void  GetGonderi(){

        String barkod = edtGonderiTakipNo.getText().toString();

        Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new JsonDeserializer<DateTime>() {
            @Override
            public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                return new DateTime(json.getAsString());
            }
        }).create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseUrl.Url)
                .addConverterFactory(GsonConverterFactory.create(gson))
//                    .client(getUnsafeOkHttpClient().build())
                .build();
        JetizzService service = retrofit.create(JetizzService.class);
        String token = loginResponse.getAccess_token();


        responseCall = service.GetGonderi("Bearer " + token, barkod);

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

                            edtGonderiTakipNo = (EditText) findViewById(R.id.edtGonderiTakipNo);
                            btnGonderiSorgula = findViewById(R.id.btnGonderiSorgula);
                            alici.setText(tempAlici);
                            gonderici.setText(tempGonderici);
                            atfId = tempAtfId;
                            gonderiNo.setText(tempAtfNo);
                            sonHareket.setText(tempSonIslem);
                            hizmet = tempHizmet;
                            desi.setText(tempDesi);
                            zimmeteAlan.setText(tempZimmetAlan);
                            aliciTel.setText(tempAliciTelefon);
                            telefon = tempAliciTelefon;
                            if (tempTahTip!= null){
                                LinearLayout lytTip = findViewById(R.id.lytTahTip);
                                lytTip.setVisibility(View.VISIBLE);
                                tahsilatTipi.setText(tempTahTip);
                            }
                            tutar = tempTahTur;
                            if ( tutar > 0.0   ){
                                LinearLayout lytTut = findViewById(R.id.lytTahTur);
                                lytTut.setVisibility(View.VISIBLE);
                                tahsilatTutari.setText(String.valueOf(tempTahTur));
                            }
                            setUp();
//                            Intent i = new Intent(DeliveryActivity.this, DeliveryActivity.class);
//                            i.putExtra("tempAlici", tempAlici);
//                            i.putExtra("tempAtfNo", tempAtfNo);
//                            i.putExtra("tempGonderici", tempGonderici);
//                            i.putExtra("tempHizmet", tempHizmet);
//                            i.putExtra("tempZimmetAlan", tempZimmetAlan);
//                            i.putExtra("tempDesi", tempDesi);
//                            i.putExtra("tempSonIslem", tempSonIslem);
//                            i.putExtra("tempAtfId", tempAtfId);
//                            i.putExtra("tempAliciTelefon", tempAliciTelefon);
//                            i.putExtra("tempTahTip", tempTahTip);
//                            i.putExtra("tempTahTur", tempTahTur);
//                            startActivity(i);
//                            finish();
                        } else {
                            CommonUtils.showAlertWithMesseage(DeliveryActivity.this, "Kriterlere uygun gönderi bulunamadı !", "UYARI");
                        }
                    }


                } else {
                    alertDialog.dismiss();
                    if (response.raw().code() == 401)
                        CommonUtils.showTokenExpired(DeliveryActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                    else
                        CommonUtils.showAlertWithMesseage(DeliveryActivity.this, "İşlem Başarısız", "UYARI");

                }
            }

            @Override
            public void onFailure(Call<List<ZimmetListModel>> call, Throwable t) {
                alertDialog.dismiss();
                ShowDialog.setSnackBar(parentLayout, "Bağlantıda hata oluştu");
            }
        });

    }



    private void getLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        konumYoneticisi.requestLocationUpdates("gps", 5000, 0, locationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    protected void setUp() {
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabAdapter = new TabAdapter(getSupportFragmentManager());
        tabAdapter.addFragment(new DeliveryFragment(atfId,alici.getText().toString(),hizmet,tutar,musteriId), "Teslim");
        tabAdapter.addFragment(new ReturnFragment(atfId), "İade");
        viewPager.setAdapter(tabAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }
    public void btnAliciTelOnClick(View view) {
        try {
            String telNo = telefon;
            if (!telNo.equals("")) {
                String ilkKarakter = telNo.substring(0, 1);
                String ikinciKarakter = telNo.substring(1, 1);

                if (ilkKarakter.equals("0")) {
                    callPhone(telefon);
                } else if (ilkKarakter.equals("+")) {
                    String ilkIkiKarakter = telNo.substring(0, 2);
                    String geriyeKalan = telNo.substring(2, telNo.length());
                    geriyeKalan.trim().replaceAll(" ", "");
                    callPhone(geriyeKalan);
                } else if (ilkKarakter.equals("9")) {
                    String ilkKarakteri = telNo.substring(0, 1);
                    String geriyeKalan = telNo.substring(1, telNo.length());
                    geriyeKalan.trim().replaceAll(" ", "");
                    callPhone(geriyeKalan);
                } else {

                    callPhone("0" + telefon);
                }

            }
        } catch (StringIndexOutOfBoundsException ex) {
            Toast.makeText(getApplicationContext(), "Geçersiz telefon numarası", Toast.LENGTH_LONG).show();
        }
    }

    public void callPhone(String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DeliveryActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
        } else {
            startActivity(callIntent);
        }
    }
    protected void getCurrentLocation() {
        konumYoneticisi = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean kontrol = konumYoneticisi.isProviderEnabled("gps");
        if (!kontrol) {
            AlertDialog.Builder builder = new AlertDialog.Builder(DeliveryActivity.this);
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
                    Intent intent = new Intent(DeliveryActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        }
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                SharedPref sharedPref = new SharedPref(getBaseContext());
                sharedPref.setLatitude(String.format("Latitude : %s", location.getLatitude()));
                sharedPref.setLongitude(String.format("Longitude : %s", location.getLongitude()));
                enlem = location.getLatitude();
                boylam = location.getLongitude();
                koordinat = enlem + ";" + boylam;
                Log.e("enlem", sharedPref.getLatitude());
                Log.e("boylam", sharedPref.getLongitude());
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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, PERMISSION_REQUEST_CODE);
            } else {
                getLocation();
            }

        } else {
            getLocation();
        }
    }
}

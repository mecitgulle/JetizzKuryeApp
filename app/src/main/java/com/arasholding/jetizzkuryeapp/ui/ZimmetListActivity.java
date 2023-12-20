package com.arasholding.jetizzkuryeapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Toast;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.adapters.SpinnerAdapter;
import com.arasholding.jetizzkuryeapp.adapters.ZimmetAdapter;
import com.arasholding.jetizzkuryeapp.apimodels.ApiResponseModel;
import com.arasholding.jetizzkuryeapp.apimodels.BaseUrl;
import com.arasholding.jetizzkuryeapp.apimodels.DeliveryRequest;
import com.arasholding.jetizzkuryeapp.apimodels.LoginResponse;
import com.arasholding.jetizzkuryeapp.apimodels.SpinnerModel;
import com.arasholding.jetizzkuryeapp.apimodels.ZimmetListModel;
import com.arasholding.jetizzkuryeapp.helpers.ShowDialog;
import com.arasholding.jetizzkuryeapp.pref.Prefences;
import com.arasholding.jetizzkuryeapp.pref.SharedPref;
import com.arasholding.jetizzkuryeapp.service.JetizzService;
import com.arasholding.jetizzkuryeapp.utils.CommonUtils;
import com.arasholding.jetizzkuryeapp.utils.OkutmaTipleri;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.security.cert.CertificateException;
//
//import javax.net.ssl.HostnameVerifier;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.SSLSession;
//import javax.net.ssl.SSLSocketFactory;
//import javax.net.ssl.TrustManager;
//import javax.net.ssl.X509TrustManager;

public class ZimmetListActivity extends BaseActivity {
    RecyclerView recyclerView;
    AlertDialog alertDialog;
    AlertDialog alertDialogResponse;
    List<ZimmetListModel> list;
    private LoginResponse loginResponse;
    View parentLayout;
    EditText txtValues;
    ZimmetAdapter adapter;
    SpinnerAdapter spinnerAdapter;
    List<ZimmetListModel> filterList;
    MaterialSpinner teslimKodlariSpinner;
    List<SpinnerModel> spinnerDeliveryList;
    int teslimTipiId;
    String koordinat;
//
//    @BindView(R.id.btnHubTopluTeslim)
//    com.manojbhadane.QButton btnHubTopluTeslim;

    Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new JsonDeserializer<DateTime>() {
        @Override
        public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return new DateTime(json.getAsString());
        }
    }).create();

    final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build();

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BaseUrl.Url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
//      .client(getUnsafeOkHttpClient().build())
            .build();
    JetizzService service = retrofit.create(JetizzService.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zimmet_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loginResponse = Prefences.readFromPreferences(getApplicationContext());
        parentLayout = findViewById(android.R.id.content);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerGonderiList);
        txtValues = (EditText) findViewById(R.id.txtValues);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);


        String token = loginResponse.getAccess_token();

        SharedPref sharedPref = new SharedPref(getApplicationContext());
        koordinat = sharedPref.getLatitude()+";"+sharedPref.getLongitude();

//        Call<List<SpinnerModel>> responseTeslimTipleri = service.GetTeslimTipleri("Bearer " + token);
//
//        alertDialog = CommonUtils.showLoadingDialogWithMessage(this, "Lütfen Bekleyinizz");
//        responseTeslimTipleri.enqueue(new Callback<List<SpinnerModel>>() {
//            @Override
//            public void onResponse(Call<List<SpinnerModel>> call, Response<List<SpinnerModel>> response) {
//                if (response.isSuccessful()) {
//                    alertDialog.dismiss();
//                    spinnerDeliveryList = response.body();
//
//                    spinnerAdapter = new SpinnerAdapter(ZimmetListActivity.this, R.layout.spinner_row, spinnerDeliveryList);
//                    teslimKodlariSpinner.setAdapter(spinnerAdapter);
//
//                } else {
//                    if (response.raw().code() == 401)
//                        CommonUtils.showTokenExpired(ZimmetListActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
//                    else
//                        CommonUtils.showAlertWithMesseage(ZimmetListActivity.this, "İşlem Başarısız", "UYARI");
//                    alertDialog.dismiss();
////                    Toast.makeText(LoginActivity.this,"login error",Toast.LENGTH_LONG).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<SpinnerModel>> call, Throwable t) {
//                alertDialog.dismiss();
//                CommonUtils.showAlertWithMesseage(getApplicationContext(), t.getMessage(), "UYARI");
////                Toast.makeText(LoginActivity.this,"login ss error",Toast.LENGTH_LONG).show();
//            }
//        });


        Call<List<ZimmetListModel>> responseCall = service.GetZimmetListesi("Bearer " + token, "");

        alertDialog = CommonUtils.showLoadingDialogWithMessage(this, "Lütfen Bekleyiniz");
        responseCall.enqueue(new Callback<List<ZimmetListModel>>() {
            @Override
            public void onResponse(Call<List<ZimmetListModel>> call, retrofit2.Response<List<ZimmetListModel>> response) {
                alertDialog.dismiss();
                if (response.isSuccessful()) {
                    list = response.body();
                    if (list != null) {
                        filterList = list;
                        setZimmetListAdapter(list);
                    } else {
                        ShowDialog.setSnackBar(parentLayout, "Görüntülenecek veri bulunamadı");
//                        Toast.makeText(ZimmetListActivity.this, "Görüntülenecek veri bulunamadı", Toast.LENGTH_LONG).show();
                    }


                } else {
                    if (response.raw().code() == 401)
                        CommonUtils.showTokenExpired(ZimmetListActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                    else
                        CommonUtils.showAlertWithMesseage(ZimmetListActivity.this, "İşlem Başarısız", "UYARI");
                    alertDialog.dismiss();

                }
            }

            @Override
            public void onFailure(Call<List<ZimmetListModel>> call, Throwable t) {
                alertDialog.dismiss();
                ShowDialog.setSnackBar(parentLayout, "Bağlantıda hata oluştu");
            }
        });

        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        txtValues.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                adapter.getFilter().filter(s);
                String filterString = s.toString().toLowerCase();
                final ArrayList<ZimmetListModel> nlist = new ArrayList<ZimmetListModel>(count);

                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getAlici().toLowerCase().contains(filterString) || list.get(i).getAtfNo().toLowerCase().contains(filterString)) {
                        nlist.add(list.get(i));
                    } else {
                        setZimmetListAdapter(list);
                    }
                }
                setZimmetListAdapter(nlist);
                if (filterString.isEmpty())
                    setZimmetListAdapter(list);
            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.notifyDataSetChanged();
            }
        });
    }


    private void setZimmetListAdapter(List<ZimmetListModel> zimmetList) {
        adapter = new ZimmetAdapter(zimmetList, this);

        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

//    @OnClick(R.id.btnHubTopluTeslim)
    public void TopluTeslimatKapat(View v) {

        if (list.size() == 0 ){
            CommonUtils.showAlertWithMesseage(ZimmetListActivity.this, "Zimmet Listesi Bulunamadı", "UYARI");
            return;
        }
        loginResponse = Prefences.readFromPreferences(getApplicationContext());
        String token = loginResponse.getAccess_token();

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_toplu_teslimat, null);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Teslimat Kapat");
        alertDialog.setView(dialogView);

        teslimKodlariSpinner =  dialogView.findViewById(R.id.spinner_deliverable);
        EditText edtTeslimAlanAdi = dialogView.findViewById(R.id.edtTeslimAlanAdi);
        EditText edtTeslimAlanSoyadi = dialogView.findViewById(R.id.edtTeslimAlanSoyadi);
        EditText edtKimlikNo = dialogView.findViewById(R.id.edtKimlikNo);
        EditText edtAciklama = dialogView.findViewById(R.id.edtAciklama);

        Call<List<SpinnerModel>> responseTeslimTipleri = service.GetTeslimTipleri("Bearer " + token);

        responseTeslimTipleri.enqueue(new Callback<List<SpinnerModel>>() {
            @Override
            public void onResponse(Call<List<SpinnerModel>> call, Response<List<SpinnerModel>> response) {
                if (response.isSuccessful()) {
                    spinnerDeliveryList = response.body();

                    spinnerAdapter = new SpinnerAdapter(ZimmetListActivity.this, R.layout.spinner_row, spinnerDeliveryList);
                    teslimKodlariSpinner.setAdapter(spinnerAdapter);

                } else {
                    if (response.raw().code() == 401)
                        CommonUtils.showTokenExpired(ZimmetListActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                    else
                        CommonUtils.showAlertWithMesseage(ZimmetListActivity.this, "İşlem Başarısız", "UYARI");
//                    Toast.makeText(LoginActivity.this,"login error",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<SpinnerModel>> call, Throwable t) {
                CommonUtils.showAlertWithMesseage(getApplicationContext(), t.getMessage(), "UYARI");
//                Toast.makeText(LoginActivity.this,"login ss error",Toast.LENGTH_LONG).show();
            }
        });

        teslimKodlariSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                SpinnerModel model = (SpinnerModel) spinnerAdapter.getItemObj(position);
                teslimTipiId = model.getValue();
            }

        });

        alertDialog.setPositiveButton("Kaydet", (dialog, which) -> {
            dialog.dismiss();

            if (edtTeslimAlanAdi.getText().toString().isEmpty()) {
                CommonUtils.showAlertWithMesseage(ZimmetListActivity.this, "Teslim alan adi boş olamaz", "UYARI");
                return;
            }
            if (edtTeslimAlanSoyadi.getText().toString().isEmpty()) {
                CommonUtils.showAlertWithMesseage(ZimmetListActivity.this, "Teslim Alan Soyadi boş olamaz", "UYARI");
                return;
            }

            DeliveryRequest request = new DeliveryRequest();

            request.setTeslimAlanAd(edtTeslimAlanAdi.getText().toString());
            request.setTeslimAlanSoyad(edtTeslimAlanSoyadi.getText().toString());
            request.setTeslimAlanKimlikNo(edtKimlikNo.getText().toString());
            request.setKanit(edtAciklama.getText().toString());
            request.setTeslimKoordinat(koordinat);
            request.setKaynak("MOBIL");
            request.setTeslimTipi(teslimTipiId);

            Call<ApiResponseModel> responseCall = service.HubTopluTeslimGirisi("Bearer " + token, request);

            alertDialogResponse = CommonUtils.showLoadingDialogWithMessage(ZimmetListActivity.this, "Lütfen Bekleyiniz");
            responseCall.enqueue(new Callback<ApiResponseModel>() {
                @Override
                public void onResponse(Call<ApiResponseModel> call, Response<ApiResponseModel> response) {
                    if (response.isSuccessful()) {
                        alertDialogResponse.dismiss();
                        String status = response.body().Status;
                        if (status.equals("success")) {
                            ShowDialog.CreateDialog(ZimmetListActivity.this, "İşlem Başarılı", "SONUÇ").show();
                            yonlendir();

                        }
                        else{
                            ShowDialog.CreateDialog(ZimmetListActivity.this,  response.body().Message, "HATA").show();
                        }

                    } else {
                        alertDialogResponse.dismiss();
                        if (response.raw().code() == 401)
                            CommonUtils.showTokenExpired(ZimmetListActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                        else
                            CommonUtils.showAlertWithMesseage(ZimmetListActivity.this, "İşlem Başarısız", "UYARI");
                    }
                }

                @Override
                public void onFailure(Call<ApiResponseModel> call, Throwable t) {
                    alertDialogResponse.dismiss();
                    CommonUtils.showAlertWithMesseage(ZimmetListActivity.this, t.getMessage(), "UYARI");
//                for (ErrorMessage item:t) {
//                    Toast.makeText(getContext(),item.getMessage(),
//                            Toast.LENGTH_SHORT).show();
//                }
//                Toast.makeText(LoginActivity.this,"login ss error",Toast.LENGTH_LONG).show();
                }
            });
        });

        alertDialog.setNegativeButton("Kapat", (dialog, which) -> {
            dialog.dismiss();
        });

        alertDialog.show();
    }
    public void yonlendir() {

        Intent intent = new Intent(ZimmetListActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

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

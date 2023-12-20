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
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.adapters.GelenGonderiAdapter;
import com.arasholding.jetizzkuryeapp.apimodels.BaseUrl;
import com.arasholding.jetizzkuryeapp.apimodels.LoginResponse;
import com.arasholding.jetizzkuryeapp.apimodels.ZimmetListModel;
import com.arasholding.jetizzkuryeapp.pref.Prefences;
import com.arasholding.jetizzkuryeapp.service.JetizzService;
import com.arasholding.jetizzkuryeapp.utils.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
//import java.security.cert.CertificateException;
//
//import javax.net.ssl.HostnameVerifier;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.SSLSession;
//import javax.net.ssl.SSLSocketFactory;
//import javax.net.ssl.TrustManager;
//import javax.net.ssl.X509TrustManager;
//import static com.arasholding.jetizzkuryeapp.ui.LoginActivity.getUnsafeOkHttpClient;

public class GelenGonderiActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    AlertDialog alertDialog;
    List<ZimmetListModel> list;
    private LoginResponse loginResponse;
    EditText txtValues;
    String notBirakildi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gelen_gonderi);

        Intent intent = getIntent();
        notBirakildi = intent.getStringExtra("notBirakildi");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        loginResponse = Prefences.readFromPreferences(getApplicationContext());

        recyclerView = (RecyclerView) findViewById(R.id.recyclerGelenGonderiList);
        txtValues = (EditText) findViewById(R.id.txtValues);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new JsonDeserializer<DateTime>() {
                @Override
                public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                        throws JsonParseException {
                    return new DateTime(json.getAsString());
                }
            }).create();
            if (notBirakildi == null) {

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BaseUrl.Url)
                        .addConverterFactory(GsonConverterFactory.create(gson))
//                    .client(getUnsafeOkHttpClient().build())
                        .build();
                JetizzService service = retrofit.create(JetizzService.class);
                String token = loginResponse.getAccess_token();

                Call<List<ZimmetListModel>> responseCall = service.GetGelenGonderiListesi("Bearer " + token, "");

                alertDialog = CommonUtils.showLoadingDialogWithMessage(this, "Lütfen Bekleyiniz");
                responseCall.enqueue(new Callback<List<ZimmetListModel>>() {
                    @Override
                    public void onResponse(Call<List<ZimmetListModel>> call, retrofit2.Response<List<ZimmetListModel>> response) {
                        alertDialog.dismiss();
                        if (response.isSuccessful()) {
                            list = response.body();
                            if (list != null)
                                setZimmetListAdapter(list);
                            else {
                                Toast.makeText(GelenGonderiActivity.this, "Görüntülenecek veri bulunamadı", Toast.LENGTH_LONG).show();
                            }


                        } else {
                            alertDialog.dismiss();
                            if (response.raw().code() == 401)
                                CommonUtils.showTokenExpired(GelenGonderiActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                            else
                                CommonUtils.showAlertWithMesseage(GelenGonderiActivity.this, "İşlem Başarısız", "UYARI");
                        }
                    }


                    @Override
                    public void onFailure(Call<List<ZimmetListModel>> call, Throwable t) {
                        alertDialog.dismiss();
                        Toast.makeText(GelenGonderiActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
            else {

                toolbar.setTitle("Not Bırakılan Gönderiler");

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BaseUrl.Url)
                        .addConverterFactory(GsonConverterFactory.create(gson))
//                    .client(getUnsafeOkHttpClient().build())
                        .build();
                JetizzService service = retrofit.create(JetizzService.class);

                String token = loginResponse.getAccess_token();

                Call<List<ZimmetListModel>> responseCall = service.GetNotBirakilanListesi("Bearer " + token, "");

                alertDialog = CommonUtils.showLoadingDialogWithMessage(this, "Lütfen Bekleyiniz");
                responseCall.enqueue(new Callback<List<ZimmetListModel>>() {
                    @Override
                    public void onResponse(Call<List<ZimmetListModel>> call, retrofit2.Response<List<ZimmetListModel>> response) {
                        alertDialog.dismiss();
                        if (response.isSuccessful()) {
                            list = response.body();
                            if (list != null)
                                setZimmetListAdapter(list);
                            else {
                                Toast.makeText(GelenGonderiActivity.this, "Görüntülenecek veri bulunamadı", Toast.LENGTH_LONG).show();
                            }


                        } else {
                            alertDialog.dismiss();
                            if (response.raw().code() == 401)
                                CommonUtils.showTokenExpired(GelenGonderiActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                            else
                                CommonUtils.showAlertWithMesseage(GelenGonderiActivity.this, "İşlem Başarısız", "UYARI");
                        }
                    }


                    @Override
                    public void onFailure(Call<List<ZimmetListModel>> call, Throwable t) {
                        alertDialog.dismiss();
                        Toast.makeText(GelenGonderiActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (NullPointerException e) {
            Toast.makeText(GelenGonderiActivity.this, "Görüntülenecek veri bulunamadı", Toast.LENGTH_LONG).show();
        }


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
            }
        });
    }

    private void setZimmetListAdapter(List<ZimmetListModel> gelenGonderiList) {
        GelenGonderiAdapter adapter = new GelenGonderiAdapter(gelenGonderiList, this,notBirakildi);

        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
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

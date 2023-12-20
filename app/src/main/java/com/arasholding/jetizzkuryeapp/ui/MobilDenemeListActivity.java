package com.arasholding.jetizzkuryeapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.adapters.GelenGonderiAdapter;
import com.arasholding.jetizzkuryeapp.adapters.MobilDenemeAdapter;
import com.arasholding.jetizzkuryeapp.apimodels.BaseUrl;
import com.arasholding.jetizzkuryeapp.apimodels.LoginResponse;
import com.arasholding.jetizzkuryeapp.apimodels.MobilDenemeRequest;
import com.arasholding.jetizzkuryeapp.apimodels.ZimmetListModel;
import com.arasholding.jetizzkuryeapp.pref.Prefences;
import com.arasholding.jetizzkuryeapp.service.JetizzService;
import com.arasholding.jetizzkuryeapp.utils.CommonUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MobilDenemeListActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    AlertDialog alertDialog;
    List<MobilDenemeRequest> list;
    private LoginResponse loginResponse;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobil_deneme_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        loginResponse = Prefences.readFromPreferences(getApplicationContext());

        recyclerView = (RecyclerView) findViewById(R.id.recyclerMobilDenemeList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BaseUrl.Url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            JetizzService service = retrofit.create(JetizzService.class);
            String token = loginResponse.getAccess_token();

            Call<List<MobilDenemeRequest>> responseCall = service.MobilDenemeGetList("Bearer " + token);

            alertDialog = CommonUtils.showLoadingDialogWithMessage(this,"Lütfen Bekleyiniz");
            responseCall.enqueue(new Callback<List<MobilDenemeRequest>>() {
                @Override
                public void onResponse(Call<List<MobilDenemeRequest>> call, retrofit2.Response<List<MobilDenemeRequest>> response) {
                    alertDialog.dismiss();
                    if (response.isSuccessful()) {
                        list = response.body();
                        if (list != null)
                            setMobilDenemeListAdapter(list);
                        else{
                            Toast.makeText(MobilDenemeListActivity.this, "Görüntülenecek veri bulunamadı", Toast.LENGTH_LONG).show();
                        }


                    } else {
                        alertDialog.dismiss();
                        if (response.raw().code() == 401)
                            CommonUtils.showTokenExpired(MobilDenemeListActivity.this,"Oturum süresi doldu. Tekrar giriş yapınız","UYARI");
                        else
                            CommonUtils.showAlertWithMesseage(MobilDenemeListActivity.this,"İşlem Başarısız","UYARI");
                    }
                }

                @Override
                public void onFailure(Call<List<MobilDenemeRequest>> call, Throwable t) {
                    alertDialog.dismiss();
                    Toast.makeText(MobilDenemeListActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (Exception ex)
        {
            Toast.makeText(MobilDenemeListActivity.this, ex.toString(), Toast.LENGTH_LONG).show();
        }
    }
    private void setMobilDenemeListAdapter(List<MobilDenemeRequest> mobilDenemeList) {
        MobilDenemeAdapter adapter = new MobilDenemeAdapter(mobilDenemeList, this);

        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

}
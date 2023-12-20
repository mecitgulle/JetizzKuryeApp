package com.arasholding.jetizzkuryeapp.ui.fragment;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.adapters.SpinnerAdapter;
import com.arasholding.jetizzkuryeapp.apimodels.ApiResponseModel;
import com.arasholding.jetizzkuryeapp.apimodels.BaseUrl;
import com.arasholding.jetizzkuryeapp.apimodels.ErrorMessage;
import com.arasholding.jetizzkuryeapp.apimodels.GetTalimatRequestModel;
import com.arasholding.jetizzkuryeapp.apimodels.LoginResponse;
import com.arasholding.jetizzkuryeapp.apimodels.ReturnRequest;
import com.arasholding.jetizzkuryeapp.apimodels.SpinnerModel;
import com.arasholding.jetizzkuryeapp.helpers.ShowDialog;
import com.arasholding.jetizzkuryeapp.pref.Prefences;
import com.arasholding.jetizzkuryeapp.service.JetizzService;
import com.arasholding.jetizzkuryeapp.ui.MainActivity;
import com.arasholding.jetizzkuryeapp.ui.N11Activity;
import com.arasholding.jetizzkuryeapp.utils.CommonUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.security.cert.CertificateException;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//import static com.arasholding.jetizzkuryeapp.ui.LoginActivity.getUnsafeOkHttpClient;

public class ReturnFragment extends Fragment {

    private MaterialSpinner iadeKodlariSpinner;
    private List<SpinnerModel> spinnerReturnList;
    private int iadeTipiId;
    private SpinnerAdapter adapter;
    private LoginResponse loginResponse;
    private AlertDialog alertDialog;
    private AlertDialog alertDialog2;
    private Unbinder unbinder;
    int atfId;
    @BindView(R.id.edtIadeKanit)
    TextInputEditText edtIadeKanit;

    private String token;

    public ReturnFragment(int atfId) {
        this.atfId = atfId;
    }

    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BaseUrl.Url)
            .addConverterFactory(GsonConverterFactory.create())
//            .client(getUnsafeOkHttpClient().build())
            .build();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_return, container, false);
        unbinder = ButterKnife.bind(this, view);
        iadeKodlariSpinner = view.findViewById(R.id.spinner_return);

        loginResponse = Prefences.readFromPreferences(view.getContext());


        JetizzService service = retrofit.create(JetizzService.class);
        token = loginResponse.getAccess_token();

        Call<List<SpinnerModel>> responseCall = service.GetIadeTipleri("Bearer " + token);

        alertDialog = CommonUtils.showLoadingDialogWithMessage(getActivity(), "Lütfen Bekleyiniz");
        responseCall.enqueue(new Callback<List<SpinnerModel>>() {
            @Override
            public void onResponse(Call<List<SpinnerModel>> call, Response<List<SpinnerModel>> response) {
                if (response.isSuccessful()) {
                    alertDialog.dismiss();
                    spinnerReturnList = response.body();

                    adapter = new SpinnerAdapter(getActivity(), R.layout.spinner_row, spinnerReturnList);
                    iadeKodlariSpinner.setAdapter(adapter);

                } else {
                    if (response.raw().code() == 401)
                        CommonUtils.showTokenExpired(getActivity(), "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                    else
                        CommonUtils.showAlertWithMesseage(getActivity(), "İşlem Başarısız", "UYARI");
                    alertDialog.dismiss();
//                    Toast.makeText(LoginActivity.this,"login error",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<SpinnerModel>> call, Throwable t) {
                alertDialog.dismiss();
//                Toast.makeText(LoginActivity.this,"login ss error",Toast.LENGTH_LONG).show();
            }
        });
        iadeKodlariSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                SpinnerModel model = (SpinnerModel) adapter.getItemObj(position);
                iadeTipiId = model.getValue();
//                Toast.makeText(getContext(), "ID: " + model.getValue() + "\nName: " + model.getText(),
//                        Toast.LENGTH_SHORT).show();
            }

        });
        GetTalimat();
        return view;
    }

    public  void GetTalimat(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseUrl.Url)
                .addConverterFactory(GsonConverterFactory.create())
//                .client(getUnsafeOkHttpClient().build())
                .build();
        JetizzService service = retrofit.create(JetizzService.class);
        String token = loginResponse.getAccess_token();
        GetTalimatRequestModel requestModel = new GetTalimatRequestModel();
        requestModel.setAtfId(atfId);
        Call<ApiResponseModel> responseCall = service.TalimatGetir("Bearer " + token, requestModel);

        Log.v("token", loginResponse.getAccess_token());

        alertDialog2 = CommonUtils.showLoadingDialogWithMessage(getActivity(), "Lütfen Bekleyiniz");
        responseCall.enqueue(new Callback<ApiResponseModel>() {
            @Override
            public void onResponse(Call<ApiResponseModel> call, retrofit2.Response<ApiResponseModel> response) {
                alertDialog2.dismiss();
                if (response.isSuccessful()) {
                    alertDialog2.dismiss();
                    if(response.body() != null)
                    {
                        alertDialog2.dismiss();
                        String status = response.body().Status;
                        if(status.equals("success"))
                        {
                            CommonUtils.showAlertWithMesseage(getActivity(), response.body().Message, "UYARI");
                        }
                    }

                } else {
                    alertDialog2.dismiss();
                    if (response.raw().code() == 401)
                        CommonUtils.showTokenExpired(getActivity(), "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                    else
                        CommonUtils.showAlertWithMesseage(getActivity(), "İşlem Başarısız", "UYARI");
                }
            }

            @Override
            public void onFailure(Call<ApiResponseModel> call, Throwable t) {
                alertDialog2.dismiss();
                CommonUtils.showAlertWithMesseage(getActivity(), "Bağlantıda hata oluştu", "UYARI");
            }
        });
    }
    @OnClick(R.id.btnIadeGir)
    public void IadeGirisi() {
        JetizzService service = retrofit.create(JetizzService.class);
        token = loginResponse.getAccess_token();
        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setId(atfId);
        returnRequest.setKanit(edtIadeKanit.getText().toString());
        returnRequest.setIadeNedeni(iadeTipiId);

        Call<ApiResponseModel> responseCall = service.IadeGirisi("Bearer " + token, returnRequest);

        alertDialog = CommonUtils.showLoadingDialogWithMessage(getActivity(), "Lütfen Bekleyiniz");
        responseCall.enqueue(new Callback<ApiResponseModel>() {
            @Override
            public void onResponse(Call<ApiResponseModel> call, Response<ApiResponseModel> response) {
                if (response.isSuccessful()) {
                    alertDialog.dismiss();
                    if (response.raw().code() == 401)
                        Toast.makeText(getActivity(), "Oturum süresi doldu. Tekrar giriş yapınız", Toast.LENGTH_LONG).show();
                    if (response.body() != null) {
                        alertDialog.dismiss();
                        String status = response.body().Status;
                        if (status.equals("success")) {
                            ShowDialog.CreateDialog(getActivity(), "İşlem Başarılı", "HATA").show();
                            yonlendir();

                        } else {
                            ShowDialog.CreateDialog(getActivity(), response.body().Message, "HATA").show();
                        }
//                        if (response.body().Errors != null) {
//                            for (ErrorMessage item : response.body().Errors) {
//                                Toast.makeText(getContext(), item.getMessage(),
//                                        Toast.LENGTH_SHORT).show();
//                            }
//                        } else {
//                            if (response.body().Status.equals("success")) {
//                                Toast.makeText(getContext(), "İşlem Başarılı",
//                                        Toast.LENGTH_SHORT).show();
//                                yonlendir();
//
//                            }
//                        }
                    }
                } else {

                    if (response.raw().code() == 401)
                        CommonUtils.showTokenExpired(getActivity(), "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                    else
                        CommonUtils.showAlertWithMesseage(getActivity(), "İşlem Başarısız", "UYARI");
                    alertDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<ApiResponseModel> call, Throwable t) {
                Toast.makeText(getContext(), t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void yonlendir() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
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

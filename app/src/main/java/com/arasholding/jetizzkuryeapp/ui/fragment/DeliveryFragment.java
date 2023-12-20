package com.arasholding.jetizzkuryeapp.ui.fragment;


import android.Manifest;
import android.app.AlertDialog;
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

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.adapters.SpinnerAdapter;
import com.arasholding.jetizzkuryeapp.apimodels.ApiResponseModel;
import com.arasholding.jetizzkuryeapp.apimodels.BaseUrl;
import com.arasholding.jetizzkuryeapp.apimodels.DeliveryRequest;
import com.arasholding.jetizzkuryeapp.apimodels.ErrorMessage;
import com.arasholding.jetizzkuryeapp.apimodels.LoginResponse;
import com.arasholding.jetizzkuryeapp.apimodels.MobilDenemeRequest;
import com.arasholding.jetizzkuryeapp.apimodels.SpinnerModel;
import com.arasholding.jetizzkuryeapp.helpers.ShowDialog;
import com.arasholding.jetizzkuryeapp.pref.Prefences;
import com.arasholding.jetizzkuryeapp.pref.SharedPref;
import com.arasholding.jetizzkuryeapp.service.JetizzService;
import com.arasholding.jetizzkuryeapp.ui.LoginActivity;
import com.arasholding.jetizzkuryeapp.ui.MainActivity;
import com.arasholding.jetizzkuryeapp.ui.MecitDenemeActivity;
import com.arasholding.jetizzkuryeapp.ui.ZimmetListActivity;
import com.arasholding.jetizzkuryeapp.utils.CommonUtils;
import com.arasholding.jetizzkuryeapp.utils.DataFormatter;
import com.arasholding.jetizzkuryeapp.utils.OkutmaTipleri;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

import static android.app.Activity.RESULT_OK;

public class DeliveryFragment extends Fragment {
    private TextView textViewTeslimTarihi;
    MaterialSpinner teslimKodlariSpinner;
    List<SpinnerModel> spinnerDeliveryList;
    int teslimTipiId;
    LocationManager konumYoneticisi;
    LocationListener locationListener;
    private double enlem, boylam;
    private static final int PERMISSION_REQUEST_CODE = 101;
    SpinnerAdapter adapter;
    private LoginResponse loginResponse;
    AlertDialog alertDialog;
    private Unbinder unbinder;

    @BindView(R.id.edtTeslimAlan)
    TextInputEditText edtTeslimAlan;

    @BindView(R.id.edtTeslimAlanSoyadi)
    TextInputEditText edtTeslimAlanSoyadi;

    @BindView(R.id.edtKanit)
    TextInputEditText edtKanit;

    @BindView(R.id.edtKimlikNo)
    TextInputEditText edtKimlikNo;

    @BindView(R.id.edtTeslimKodu)
    TextInputEditText edtTeslimKodu;

    @BindView(R.id.btnKameraAc)
    Button btnKameraAc;

    String encoded;
    Uri uri2;
    int musteriId;
    int atfId;
    String token;
    String koordinat;
    String alici;
    String hizmet;
    double tutar;

    public DeliveryFragment(int atfId, String alici, String hizmet, double tutar, int musteriId) {
        this.atfId = atfId;
        this.alici = alici;
        this.hizmet = hizmet;
        this.tutar = tutar;
        this.musteriId = musteriId;
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
        View view = inflater.inflate(R.layout.fragment_delivery, container, false);
        unbinder = ButterKnife.bind(this, view);
//        textViewTeslimTarihi = view.findViewById(R.id.textViewTeslimTarihi);
        teslimKodlariSpinner = view.findViewById(R.id.spinner_delivery);

        SharedPref sharedPref = new SharedPref(view.getContext());
        koordinat = sharedPref.getLatitude() + ";" + sharedPref.getLongitude();

        if (hizmet != null) {
            if (!hizmet.equals("Restoran") && !hizmet.equals("Market")) {
                edtKanit.setVisibility(View.VISIBLE);
                edtKimlikNo.setVisibility(View.VISIBLE);
            }
        } else {
            edtKanit.setVisibility(View.VISIBLE);
            edtKimlikNo.setVisibility(View.VISIBLE);
        }
        if (musteriId == 214) {
            btnKameraAc.setVisibility(View.VISIBLE);
        }
//        alici = view.findViewById(R.id.txtViewAlici);
        view.findViewById(R.id.btnKameraAc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnFotografCek();
            }
        });
        view.findViewById(R.id.btnteslimatkapat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnTeslimatKapat();
            }
        });

//        textViewTeslimTarihi.setText(DataFormatter.GetDateFormat(new DateTime().plusDays(-1).toDateTime(), DataFormatter.DateFormats.ddMMyyyy));
        loginResponse = Prefences.readFromPreferences(view.getContext());
        getCurrentLocation(view);

        JetizzService service = retrofit.create(JetizzService.class);
        token = loginResponse.getAccess_token();
//            LoginRequest loginRequest = new LoginRequest("sinem", "123456", "password");
        Call<List<SpinnerModel>> responseCall = service.GetTeslimTipleri("Bearer " + token);

        Gson gson = new Gson();
//        String json = gson.toJson(loginRequest);
        alertDialog = CommonUtils.showLoadingDialogWithMessage(getActivity(), "Lütfen Bekleyinizz");
        responseCall.enqueue(new Callback<List<SpinnerModel>>() {
            @Override
            public void onResponse(Call<List<SpinnerModel>> call, Response<List<SpinnerModel>> response) {
                if (response.isSuccessful()) {
                    alertDialog.dismiss();
                    spinnerDeliveryList = response.body();

                    adapter = new SpinnerAdapter(getActivity(), R.layout.spinner_row, spinnerDeliveryList);
                    teslimKodlariSpinner.setAdapter(adapter);

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
                CommonUtils.showAlertWithMesseage(getActivity(), t.getMessage(), "UYARI");
//                Toast.makeText(LoginActivity.this,"login ss error",Toast.LENGTH_LONG).show();
            }
        });
        teslimKodlariSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                SpinnerModel model = (SpinnerModel) adapter.getItemObj(position);
                edtTeslimAlan.setText("");
                edtTeslimAlanSoyadi.setText("");
                teslimTipiId = model.getValue();
//                if (teslimTipiId == 40) {
//                    String deger = "";
//                    String[] kelime = null;
////                    String cumle ="Alkan Akıncı";
//                    kelime = alici.split(" ");
//                    if (kelime.length != 0) {
//                        edtTeslimAlan.setText(kelime[0]);
//                        if (kelime.length >= 2) {
//                            for (int i = 1; i < kelime.length; i++) {
//
//                                if (kelime[i] != null) {
////                                    deger.trim();
//                                    deger = deger.trim() + " " + kelime[i];
//                                }
//
//                            }
//                            edtTeslimAlanSoyadi.setText(deger.trim());
//                        }
//                    }
//
//                }
//                Toast.makeText(getContext(), "ID: " + model.getValue() + "\nName: " + model.getText(),
//                        Toast.LENGTH_SHORT).show();
            }

        });


        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void yonlendir() {

        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();

    }

    private void getLocation() {
        konumYoneticisi.requestLocationUpdates("gps", 5000, 0, locationListener);
    }

    public void btnFotografCek() {
        dispatchTakePictureIntent();
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(), "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                uri2 = photoURI;
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri2);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();

                encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                Log.e("encoded", encoded);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + atfId + "_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void btnTeslimatKapat() {

        if (tutar > 0.0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(Html.fromHtml("<font color='#d34836'>UYARI</font>"));
//            builder.setIcon(R.drawable.ic_alert);
            builder.setMessage("Tahsilat tutarını aldınız mı ?");
            builder.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);

                }
            });

            builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (!edtTeslimKodu.getText().toString().isEmpty()) {
                        String kod = String.valueOf(atfId * 456);
                        String b = kod.substring(kod.length() - 4, kod.length());
                        if (b.equals(edtTeslimKodu.getText().toString())) {
                            if (edtTeslimAlan.getText().toString().isEmpty()) {
                                CommonUtils.showAlertWithMesseage(getActivity(), "Teslim alan adi boş olamaz", "UYARI");
                                return;
                            }
                            if (edtTeslimAlanSoyadi.getText().toString().isEmpty()) {
                                CommonUtils.showAlertWithMesseage(getActivity(), "Teslim Alan Soyadi boş olamaz", "UYARI");
                                return;
                            }
                            if (edtKanit.getText().toString().isEmpty()) {
                                CommonUtils.showAlertWithMesseage(getActivity(), "Kanıt boş olamaz", "UYARI");
                                return;
                            }
                            if (musteriId == 214) {
                                if (encoded.equals(null)) {
                                    CommonUtils.showAlertWithMesseage(getActivity(), "Fotoğraf Çekiniz!", "UYARI");
                                    return;
                                }
                            }
                            JetizzService service = retrofit.create(JetizzService.class);
                            token = loginResponse.getAccess_token();
                            DeliveryRequest deliveryRequest = new DeliveryRequest();
                            deliveryRequest.setId(atfId);
                            deliveryRequest.setTeslimTarihi(DataFormatter.GetDateFormat(new DateTime().plusDays(-1).toDateTime(), DataFormatter.DateFormats.ddMMyyyy));
                            deliveryRequest.setTeslimAlanAd(edtTeslimAlan.getText().toString());
                            deliveryRequest.setTeslimAlanSoyad(edtTeslimAlanSoyadi.getText().toString());
                            deliveryRequest.setTeslimAlanKimlikNo(edtKimlikNo.getText().toString());
                            deliveryRequest.setTeslimKoordinat(koordinat);
                            deliveryRequest.setKaynak("MOBIL");
                            deliveryRequest.setKanit(edtKanit.getText().toString());
                            deliveryRequest.setTeslimTipi(teslimTipiId);
                            if (musteriId == 214) {
                                deliveryRequest.setResim(encoded.replace("\n", "").trim());
                            }


                            Call<ApiResponseModel> responseCall = service.TeslimGirisi("Bearer " + token, deliveryRequest);

                            alertDialog = CommonUtils.showLoadingDialogWithMessage(getActivity(), "Lütfen Bekleyiniz");
                            responseCall.enqueue(new Callback<ApiResponseModel>() {
                                @Override
                                public void onResponse(Call<ApiResponseModel> call, Response<ApiResponseModel> response) {
                                    if (response.isSuccessful()) {
                                        alertDialog.dismiss();
                                        String status = response.body().Status;
                                        if (status.equals("success")) {
                                            ShowDialog.CreateDialog(getActivity(), "İşlem Başarılı", "HATA").show();
                                            yonlendir();

                                        } else {
                                            ShowDialog.CreateDialog(getActivity(), response.body().Message, "HATA").show();
                                        }


//                                        if (response.body() != null) {
//                                            if (response.body().Errors != null) {
//                                                for (ErrorMessage item : response.body().Errors) {
//                                                    ShowDialog.CreateDialog(getActivity(), item.getMessage(), "HATA").show();
//                                                }
//                                            } else {
//                                                if (response.body().Status.equals("success")) {
//                                                    ShowDialog.CreateDialog(getActivity(), "İşlem Başarılı", "HATA").show();
//                                                    yonlendir();
//                                                }
//                                            }
//                                        }
                                    } else {
                                        alertDialog.dismiss();
                                        if (response.raw().code() == 401)
                                            CommonUtils.showTokenExpired(getActivity(), "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                                        else
                                            CommonUtils.showAlertWithMesseage(getActivity(), "İşlem Başarısız", "UYARI");
                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResponseModel> call, Throwable t) {
                                    alertDialog.dismiss();
                                    CommonUtils.showAlertWithMesseage(getActivity(), t.getMessage(), "UYARI");
//                for (ErrorMessage item:t) {
//                    Toast.makeText(getContext(),item.getMessage(),
//                            Toast.LENGTH_SHORT).show();
//                }
//                Toast.makeText(LoginActivity.this,"login ss error",Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            CommonUtils.showAlertWithMesseage(getActivity(), "Girilen Teslimat Kodu Yanlış", "UYARI");
                        }
                    } else {
                        if (edtTeslimAlan.getText().toString().isEmpty()) {
                            CommonUtils.showAlertWithMesseage(getActivity(), "Teslim alan adi boş olamaz", "UYARI");
                            return;
                        }
                        if (edtTeslimAlanSoyadi.getText().toString().isEmpty()) {
                            CommonUtils.showAlertWithMesseage(getActivity(), "Teslim Alan Soyadi boş olamaz", "UYARI");
                            return;
                        }
                        if (edtKanit.getText().toString().isEmpty()) {
                            CommonUtils.showAlertWithMesseage(getActivity(), "Kanıt boş olamaz", "UYARI");
                            return;
                        }
                        if (musteriId == 214) {
                            if (encoded.equals(null)) {
                                CommonUtils.showAlertWithMesseage(getActivity(), "Fotoğraf Çekiniz!", "UYARI");
                                return;
                            }
                        }
                        JetizzService service = retrofit.create(JetizzService.class);
                        token = loginResponse.getAccess_token();
                        DeliveryRequest deliveryRequest = new DeliveryRequest();
                        deliveryRequest.setId(atfId);
                        deliveryRequest.setTeslimTarihi(DataFormatter.GetDateFormat(new DateTime().plusDays(-1).toDateTime(), DataFormatter.DateFormats.ddMMyyyy));
                        deliveryRequest.setTeslimAlanAd(edtTeslimAlan.getText().toString());
                        deliveryRequest.setTeslimAlanSoyad(edtTeslimAlanSoyadi.getText().toString());
                        deliveryRequest.setTeslimAlanKimlikNo(edtKimlikNo.getText().toString());
                        deliveryRequest.setTeslimKoordinat(koordinat);
                        deliveryRequest.setKaynak("MOBIL");
                        deliveryRequest.setKanit(edtKanit.getText().toString());
                        deliveryRequest.setTeslimTipi(teslimTipiId);
                        if (musteriId == 214) {
                            deliveryRequest.setResim(encoded.replace("\n", "").trim());
                        }

                        Call<ApiResponseModel> responseCall = service.TeslimGirisi("Bearer " + token, deliveryRequest);

                        alertDialog = CommonUtils.showLoadingDialogWithMessage(getActivity(), "Lütfen Bekleyiniz");
                        responseCall.enqueue(new Callback<ApiResponseModel>() {
                            @Override
                            public void onResponse(Call<ApiResponseModel> call, Response<ApiResponseModel> response) {
                                if (response.isSuccessful()) {
                                    alertDialog.dismiss();
                                    if (response.body() != null) {
                                        if (response.body().Errors != null) {
                                            for (ErrorMessage item : response.body().Errors) {
                                                ShowDialog.CreateDialog(getActivity(), item.getMessage(), "HATA").show();
                                            }
                                        } else {
                                            if (response.body().Status.equals("success")) {
                                                ShowDialog.CreateDialog(getActivity(), "İşlem Başarılı", "HATA").show();
                                                yonlendir();
                                            }
                                        }
                                    }
                                } else {
                                    alertDialog.dismiss();
                                    if (response.raw().code() == 401)
                                        CommonUtils.showTokenExpired(getActivity(), "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                                    else
                                        CommonUtils.showAlertWithMesseage(getActivity(), "İşlem Başarısız", "UYARI");
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponseModel> call, Throwable t) {
                                alertDialog.dismiss();
                                CommonUtils.showAlertWithMesseage(getActivity(), t.getMessage(), "UYARI");
//                for (ErrorMessage item:t) {
//                    Toast.makeText(getContext(),item.getMessage(),
//                            Toast.LENGTH_SHORT).show();
//                }
//                Toast.makeText(LoginActivity.this,"login ss error",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
            builder.show();
        } else {
            if (!edtTeslimKodu.getText().toString().isEmpty()) {
                String kod = String.valueOf(atfId * 456);
                String b = kod.substring(kod.length() - 4, kod.length());
                if (b.equals(edtTeslimKodu.getText().toString())) {
                    if (edtTeslimAlan.getText().toString().isEmpty()) {
                        CommonUtils.showAlertWithMesseage(getActivity(), "Teslim alan adi boş olamaz", "UYARI");
                        return;
                    }
                    if (edtTeslimAlanSoyadi.getText().toString().isEmpty()) {
                        CommonUtils.showAlertWithMesseage(getActivity(), "Teslim Alan Soyadi boş olamaz", "UYARI");
                        return;
                    }
                    if (edtKanit.getText().toString().isEmpty()) {
                        CommonUtils.showAlertWithMesseage(getActivity(), "Kanıt boş olamaz", "UYARI");
                        return;
                    }
                    if (musteriId == 214) {
                        if (encoded.equals(null)) {
                            CommonUtils.showAlertWithMesseage(getActivity(), "Fotoğraf Çekiniz!", "UYARI");
                            return;
                        }
                    }
                    JetizzService service = retrofit.create(JetizzService.class);
                    token = loginResponse.getAccess_token();
                    DeliveryRequest deliveryRequest = new DeliveryRequest();
                    deliveryRequest.setId(atfId);
                    deliveryRequest.setTeslimTarihi(DataFormatter.GetDateFormat(new DateTime().plusDays(-1).toDateTime(), DataFormatter.DateFormats.ddMMyyyy));
                    deliveryRequest.setTeslimAlanAd(edtTeslimAlan.getText().toString());
                    deliveryRequest.setTeslimAlanSoyad(edtTeslimAlanSoyadi.getText().toString());
                    deliveryRequest.setTeslimAlanKimlikNo(edtKimlikNo.getText().toString());
                    deliveryRequest.setTeslimKoordinat(koordinat);
                    deliveryRequest.setKaynak("MOBIL");
                    deliveryRequest.setKanit(edtKanit.getText().toString());
                    deliveryRequest.setTeslimTipi(teslimTipiId);
                    if (musteriId == 214) {
                        deliveryRequest.setResim(encoded.replace("\n", "").trim());
                    }

                    Call<ApiResponseModel> responseCall = service.TeslimGirisi("Bearer " + token, deliveryRequest);

                    alertDialog = CommonUtils.showLoadingDialogWithMessage(getActivity(), "Lütfen Bekleyiniz");
                    responseCall.enqueue(new Callback<ApiResponseModel>() {
                        @Override
                        public void onResponse(Call<ApiResponseModel> call, Response<ApiResponseModel> response) {
                            if (response.isSuccessful()) {
                                alertDialog.dismiss();
                                if (response.body() != null) {
                                    if (response.body().Errors != null) {
                                        for (ErrorMessage item : response.body().Errors) {
                                            ShowDialog.CreateDialog(getActivity(), item.getMessage(), "HATA").show();
                                        }
                                    } else {
                                        if (response.body().Status.equals("success")) {
                                            ShowDialog.CreateDialog(getActivity(), "İşlem Başarılı", "HATA").show();
                                            yonlendir();
                                        }
                                    }
                                }
                            } else {
                                alertDialog.dismiss();
                                if (response.raw().code() == 401)
                                    CommonUtils.showTokenExpired(getActivity(), "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                                else
                                    CommonUtils.showAlertWithMesseage(getActivity(), "İşlem Başarısız", "UYARI");
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponseModel> call, Throwable t) {
                            alertDialog.dismiss();
                            CommonUtils.showAlertWithMesseage(getActivity(), t.getMessage(), "UYARI");
//                for (ErrorMessage item:t) {
//                    Toast.makeText(getContext(),item.getMessage(),
//                            Toast.LENGTH_SHORT).show();
//                }
//                Toast.makeText(LoginActivity.this,"login ss error",Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    CommonUtils.showAlertWithMesseage(getActivity(), "Girilen Teslimat Kodu Yanlış", "UYARI");
                }
            } else {
                if (edtTeslimAlan.getText().toString().isEmpty()) {
                    CommonUtils.showAlertWithMesseage(getActivity(), "Teslim alan adi boş olamaz", "UYARI");
                    return;
                }
                if (edtTeslimAlanSoyadi.getText().toString().isEmpty()) {
                    CommonUtils.showAlertWithMesseage(getActivity(), "Teslim Alan Soyadi boş olamaz", "UYARI");
                    return;
                }
                if (edtKanit.getText().toString().isEmpty()) {
                    CommonUtils.showAlertWithMesseage(getActivity(), "Kanıt boş olamaz", "UYARI");
                    return;
                }
                if (musteriId == 214) {
                    if (encoded == null) {
                        CommonUtils.showAlertWithMesseage(getActivity(), "Fotoğraf Çekiniz!", "UYARI");
                        return;
                    }
                }
                JetizzService service = retrofit.create(JetizzService.class);
                token = loginResponse.getAccess_token();
                DeliveryRequest deliveryRequest = new DeliveryRequest();
                deliveryRequest.setId(atfId);
                deliveryRequest.setTeslimTarihi(DataFormatter.GetDateFormat(new DateTime().plusDays(-1).toDateTime(), DataFormatter.DateFormats.ddMMyyyy));
                deliveryRequest.setTeslimAlanAd(edtTeslimAlan.getText().toString());
                deliveryRequest.setTeslimAlanSoyad(edtTeslimAlanSoyadi.getText().toString());
                deliveryRequest.setTeslimAlanKimlikNo(edtKimlikNo.getText().toString());
                deliveryRequest.setTeslimKoordinat(koordinat);
                deliveryRequest.setKaynak("MOBIL");
                deliveryRequest.setKanit(edtKanit.getText().toString());
                deliveryRequest.setTeslimTipi(teslimTipiId);
                if (musteriId == 214) {
                    deliveryRequest.setResim(encoded.replace("\n", "").trim());
                }

                Call<ApiResponseModel> responseCall = service.TeslimGirisi("Bearer " + token, deliveryRequest);

                alertDialog = CommonUtils.showLoadingDialogWithMessage(getActivity(), "Lütfen Bekleyiniz");
                responseCall.enqueue(new Callback<ApiResponseModel>() {
                    @Override
                    public void onResponse(Call<ApiResponseModel> call, Response<ApiResponseModel> response) {
                        if (response.isSuccessful()) {
                            alertDialog.dismiss();
                            if (response.body() != null) {
                                if (response.body().Errors != null) {
                                    for (ErrorMessage item : response.body().Errors) {
                                        ShowDialog.CreateDialog(getActivity(), item.getMessage(), "HATA").show();
                                    }
                                } else {
                                    if (response.body().Status.equals("success")) {
                                        ShowDialog.CreateDialog(getActivity(), "İşlem Başarılı", "UYARI").show();
                                        yonlendir();
                                    }
                                    if (response.body().Status.equals("error")) {
                                        ShowDialog.CreateDialog(getActivity(), response.body().Message, "UYARI").show();
                                        yonlendir();
                                    }
                                }
                            }
                        } else {
                            alertDialog.dismiss();
                            if (response.raw().code() == 401)
                                CommonUtils.showTokenExpired(getActivity(), "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                            else
                                CommonUtils.showAlertWithMesseage(getActivity(), "İşlem Başarısız", "UYARI");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponseModel> call, Throwable t) {
                        alertDialog.dismiss();
                        CommonUtils.showAlertWithMesseage(getActivity(), t.getMessage(), "UYARI");
//                for (ErrorMessage item:t) {
//                    Toast.makeText(getContext(),item.getMessage(),
//                            Toast.LENGTH_SHORT).show();
//                }
//                Toast.makeText(LoginActivity.this,"login ss error",Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    protected void getCurrentLocation(View view) {
        konumYoneticisi = (LocationManager) view.getContext().getSystemService(Context.LOCATION_SERVICE);
        boolean kontrol = konumYoneticisi.isProviderEnabled("gps");
        if (!kontrol) {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
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
                    Intent intent = new Intent(view.getContext(), MainActivity.class);
                    startActivity(intent);
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        }
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
//                SharedPref sharedPref = new SharedPref(getActivity());
//                sharedPref.setLatitude(String.format("Latitude : %s", location.getLatitude()));
//                sharedPref.setLongitude(String.format("Longitude : %s", location.getLongitude()));
                enlem = location.getLatitude();
                boylam = location.getLongitude();
                koordinat = enlem + ";" + boylam;
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
            if (ActivityCompat.checkSelfPermission(view.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(view.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, PERMISSION_REQUEST_CODE);
            } else {
                getLocation();
            }

        } else {
            getLocation();
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
//    @OnClick(R.id.btnteslimatkapat)
//    public void TeslimatKapat() {
//        JetizzService service = retrofit.create(JetizzService.class);
//        token = loginResponse.getAccess_token();
//        DeliveryRequest deliveryRequest = new DeliveryRequest();
//        deliveryRequest.setId("34");
//        deliveryRequest.setTeslimAlanAd(edtTeslimAlan.getText().toString());
//        deliveryRequest.setTeslimAlanSoyad(edtTeslimAlan.getText().toString());
//        deliveryRequest.setTeslimAlanKimlikNo(edtKimlikNo.getText().toString());
//        deliveryRequest.setTeslimTipi(teslimKodlariSpinner.getSelectedIndex());
////            LoginRequest loginRequest = new LoginRequest("sinem", "123456", "password");
//        Call<ApiResponseModel> responseCall = service.TeslimGirisi("Bearer " + token,deliveryRequest);
//
//        Gson gson = new Gson();
////        String json = gson.toJson(loginRequest);
//        alertDialog = CommonUtils.showLoadingDialogWithMessage(getActivity(),"Lütfen Bekleyiniz");
//        responseCall.enqueue(new Callback<ApiResponseModel>() {
//            @Override
//            public void onResponse(Call<ApiResponseModel> call, Response<ApiResponseModel> response) {
//                if (response.isSuccessful()) {
//                    alertDialog.dismiss();
//
//                }
//                else
//                {
//                    alertDialog.dismiss();
////                    Toast.makeText(LoginActivity.this,"login error",Toast.LENGTH_LONG).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ApiResponseModel> call, Throwable t) {
////                Toast.makeText(LoginActivity.this,"login ss error",Toast.LENGTH_LONG).show();
//            }
//        });
//    }
}

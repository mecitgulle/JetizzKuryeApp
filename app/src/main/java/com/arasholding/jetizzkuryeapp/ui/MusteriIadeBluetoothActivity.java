package com.arasholding.jetizzkuryeapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ActionBar;
import android.app.Activity;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.adapters.SpinnerAdapter;
import com.arasholding.jetizzkuryeapp.apimodels.ApiResponseModel;
import com.arasholding.jetizzkuryeapp.apimodels.BaseUrl;
import com.arasholding.jetizzkuryeapp.apimodels.ErrorMessage;
import com.arasholding.jetizzkuryeapp.apimodels.LoginResponse;
import com.arasholding.jetizzkuryeapp.apimodels.MobilDenemeRequest;
import com.arasholding.jetizzkuryeapp.apimodels.MusteriIadeRequest;
import com.arasholding.jetizzkuryeapp.apimodels.SpinnerModel;
import com.arasholding.jetizzkuryeapp.helpers.ShowDialog;
import com.arasholding.jetizzkuryeapp.pref.Prefences;
import com.arasholding.jetizzkuryeapp.pref.SharedPref;
import com.arasholding.jetizzkuryeapp.service.JetizzService;
import com.arasholding.jetizzkuryeapp.utils.CommonUtils;
import com.google.zxing.client.android.BeepManager;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//import static com.arasholding.jetizzkuryeapp.ui.CameraBarcodeReaderActivity.getUnsafeOkHttpClient;

public class MusteriIadeBluetoothActivity extends BaseActivity {
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
    AlertDialog alertDialogList;
    Button openButton;
    private String pairedDeviceName;
    private String okutmaTipi;
    private TextView txtOkutulanBarkodSayisi;
    private List<String> barcodeList;
    private ActionBar actionBar;
    private LoginResponse loginResponse;
    private EditText edtTeslimAlan;
    SpinnerAdapter adapter;
    SearchableSpinner spinner;
    List<SpinnerModel> spinnerCustomerList;
    private int musteriId;
    private String MusteriId;
    private String barkod;
    private String teslimalan;
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BaseUrl.Url)
            .addConverterFactory(GsonConverterFactory.create())
//            .client(getUnsafeOkHttpClient().build())
            .build();
    JetizzService service = retrofit.create(JetizzService.class);
    Call<ApiResponseModel> responseCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musteri_iade_bluetooth);

        loginResponse = Prefences.readFromPreferences(getApplicationContext());
        String token = loginResponse.getAccess_token();

        Intent intent = getIntent();
        SharedPref sharedPref = new SharedPref(this);
        openButton = findViewById(R.id.open);
        edtTeslimAlan = findViewById(R.id.edtTeslimAlan);

        spinner = findViewById(R.id.spinner_musterilistesi);
        spinner.setTitle("Müşteri Seçiniz");
        spinner.setPositiveButton("Tamam");

        myLabel = findViewById(R.id.label);
        txtOkutulanBarkodSayisi = findViewById(R.id.txtOkutulanBarkodSayisi);

        barcodeList = new ArrayList<>();

        openButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new MusteriIadeBluetoothActivity.BluetoothConnectionAsyncTask().execute();
            }
        });

        pairedDeviceName = sharedPref.getPairedDeviceName();
        if (pairedDeviceName == null || pairedDeviceName.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Lütfen ayarlardan bluetooth cihazı seçiniz", Toast.LENGTH_LONG).show();
            UpdateLabel("Ayarlardan cihaz seçiniz");
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBluetooth();

        new MusteriIadeBluetoothActivity.BluetoothConnectionAsyncTask().execute();

        Call<List<SpinnerModel>> responseCall = service.GetMusteriList("Bearer " + token);
        alertDialogList = CommonUtils.showLoadingDialogWithMessage(this, "Lütfen Bekleyinizz");
        responseCall.enqueue(new Callback<List<SpinnerModel>>() {
            @Override
            public void onResponse(Call<List<SpinnerModel>> call, Response<List<SpinnerModel>> response) {
                if (response.isSuccessful()) {
                    alertDialogList.dismiss();
                    spinnerCustomerList = response.body();

                    adapter = new SpinnerAdapter(getApplicationContext(), R.layout.spinner_row, spinnerCustomerList);
                    spinner.setAdapter(adapter);

                } else {
                    if (response.raw().code() == 401)
                        CommonUtils.showTokenExpired(MusteriIadeBluetoothActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                    else
                        CommonUtils.showAlertWithMesseage(MusteriIadeBluetoothActivity.this, "İşlem Başarısız", "UYARI");
                    alertDialogList.dismiss();
//                    Toast.makeText(LoginActivity.this,"login error",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<SpinnerModel>> call, Throwable t) {
                alertDialogList.dismiss();
                CommonUtils.showAlertWithMesseage(MusteriIadeBluetoothActivity.this, t.getMessage(), "UYARI");
//                Toast.makeText(LoginActivity.this,"login ss error",Toast.LENGTH_LONG).show();
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerModel model = (SpinnerModel) adapter.getItemObj(position);
                musteriId = model.getValue();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
                                            Toast.makeText(MusteriIadeBluetoothActivity.this, data, Toast.LENGTH_SHORT).show();
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
    public void UpdateLabel(String lbl) {
        myLabel.setText(lbl);
    }

    public void ButtonText(String txt) {
        openButton.setText(txt);
    }

    public BluetoothSocket getBluetoothSocket() {
        return mmSocket;
    }

    public class BluetoothConnectionAsyncTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            alertDialog = new SpotsDialog.Builder()
                    .setContext(MusteriIadeBluetoothActivity.this)
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

                String teslimalan = edtTeslimAlan.getText().toString();

                MusteriIadeRequest model = new MusteriIadeRequest();
                model.setBarkod(barcode);
                model.setTeslimalan(teslimalan);
                model.setMusteriId(musteriId);
                try {
                    if (!barcodeList.contains(model.getBarkod())) {
                        if (edtTeslimAlan.getText().toString().isEmpty()) {
                            CommonUtils.showAlertWithMesseage(MusteriIadeBluetoothActivity.this, "Teslim alan adi boş olamaz", "UYARI");
                            return;
                        }

                        if (musteriId == 0) {
                            CommonUtils.showAlertWithMesseage(MusteriIadeBluetoothActivity.this, "Lütfen Müşteri Seçiniz", "UYARI");
                            return;
                        }// Stop camera on pause// Stop camera on pause
                        Call<ApiResponseModel> responseCall = service.MusteriIadeGirisi("Bearer " + token, model);

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
                                        Toast.makeText(MusteriIadeBluetoothActivity.this, "İşlem Başarılı", Toast.LENGTH_SHORT).show();


                                    } else {

                                        CommonUtils.showAlertWithMesseage(MusteriIadeBluetoothActivity.this, response.body().Message, "UYARI");
//                                ShowDialog.setSnackBar(parentLayout,response.body().Message);
                                        vibrate();
                                    }


                                } else {
                                    alertDialog.dismiss();
                                    if (response.raw().code() == 401)
                                        CommonUtils.showTokenExpired(MusteriIadeBluetoothActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                                    else
                                        CommonUtils.showAlertWithMesseage(MusteriIadeBluetoothActivity.this, "İşlem Başarısız", "UYARI");
                                    vibrate();
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponseModel> call, Throwable t) {
                                alertDialog.dismiss();

                                ShowDialog.setSnackBar(parentLayout, "Bağlantıda hata oluştu");
                                vibrate();
                            }
                        });
                    }


                }catch (NullPointerException e)
                {
                    Toast.makeText(MusteriIadeBluetoothActivity.this, "İşlem sırasında bir hata oluştu", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NullPointerException e) {

            Toast.makeText(getApplicationContext(), "Birşeyler yanlış gitti!", Toast.LENGTH_SHORT).show();
        }
    }
}
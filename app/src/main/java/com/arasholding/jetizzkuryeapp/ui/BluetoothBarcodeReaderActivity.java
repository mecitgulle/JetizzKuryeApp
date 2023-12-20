package com.arasholding.jetizzkuryeapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.adapters.SpinnerAdapter;
import com.arasholding.jetizzkuryeapp.apimodels.ApiResponseModel;
import com.arasholding.jetizzkuryeapp.apimodels.ApiResponseModelObj;
import com.arasholding.jetizzkuryeapp.apimodels.BaseUrl;
import com.arasholding.jetizzkuryeapp.apimodels.HareketRequestModel;
import com.arasholding.jetizzkuryeapp.apimodels.LoginResponse;
import com.arasholding.jetizzkuryeapp.apimodels.SpinnerModel;
import com.arasholding.jetizzkuryeapp.helpers.ShowDialog;
import com.arasholding.jetizzkuryeapp.pref.PrefKeys;
import com.arasholding.jetizzkuryeapp.pref.Prefences;
import com.arasholding.jetizzkuryeapp.pref.SharedPref;
import com.arasholding.jetizzkuryeapp.service.JetizzService;
import com.arasholding.jetizzkuryeapp.utils.CommonUtils;
import com.arasholding.jetizzkuryeapp.utils.OkutmaTipleri;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//import static com.arasholding.jetizzkuryeapp.ui.LoginActivity.getUnsafeOkHttpClient;

public class BluetoothBarcodeReaderActivity extends BaseActivity {
    private Unbinder unbinder;
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
    Button openButton;
    private String pairedDeviceName;
    private String okutmaTipi;
    private TextView txtOkutulanBarkodSayisi;
    private List<String> barcodeList;
    private ActionBar actionBar;
    private LoginResponse loginResponse;
    Call<ApiResponseModel> responseCall;
    CheckBox checkmuskolino;
    EditText edtDesiTM;
    @BindView(R.id.edtBarcode)
    EditText edtBarcode;
    private int musteriId;
    private int subeId;
    List<SpinnerModel> spinnerCustomerList;
    List<SpinnerModel> spinnerBranchList;
    SpinnerAdapter adapter;
    SearchableSpinner spinner;
    CheckBox checkFarkliAcente;
    SearchableSpinner subeSpinner;
    private String MAC_Address;
    private String koordinat;
    CheckBox checkBarcodePrinter;

    Call<ApiResponseModelObj<String[]>> responseCallSubeYukleme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_barcode_reader);
        Intent intent = getIntent();

        unbinder = ButterKnife.bind(this);

        okutmaTipi = intent.getStringExtra("okutmaTipi");
        loginResponse = Prefences.readFromPreferences(getApplicationContext());
        openButton = findViewById(R.id.open);

        myLabel = findViewById(R.id.label);
        txtOkutulanBarkodSayisi = (TextView) findViewById(R.id.txtCount);
//        checkmuskolino = (CheckBox) findViewById(R.id.checkmuskolino);
        checkFarkliAcente = (CheckBox) findViewById(R.id.checkfarkliacente);
        edtDesiTM = (EditText) findViewById(R.id.edtDesiTM);
        edtBarcode = (EditText) findViewById(R.id.edtBarcode);
        checkBarcodePrinter = (CheckBox) findViewById(R.id.checkBarcodePrinter);

        SharedPref sharedPref = new SharedPref(getApplicationContext());
        koordinat = sharedPref.getLatitude() + ";" + sharedPref.getLongitude();
        MAC_Address = Prefences.readFromPreferencesMAC(getApplicationContext());

        spinner = findViewById(R.id.spinner_musterilist);
        spinner.setTitle("Müşteri Seçiniz");
        spinner.setPositiveButton("Tamam");

        subeSpinner = findViewById(R.id.spinner_subelist);
        subeSpinner.setTitle("Şube Seçiniz");
        subeSpinner.setPositiveButton("Tamam");

        barcodeList = new ArrayList<>();

        openButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new BluetoothConnectionAsyncTask().execute();
            }
        });
        edtBarcode.setFocusableInTouchMode(true);
        edtBarcode.requestFocus();

        pairedDeviceName = sharedPref.getPairedDeviceName();
        if (pairedDeviceName == null || pairedDeviceName.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Lütfen ayarlardan bluetooth cihazı seçiniz", Toast.LENGTH_LONG).show();
            UpdateLabel("Ayarlardan cihaz seçiniz");
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBluetooth();

        new BluetoothBarcodeReaderActivity.BluetoothConnectionAsyncTask().execute();

        if (okutmaTipi.equals(OkutmaTipleri.SUBEYUKLEME) || okutmaTipi.equals(OkutmaTipleri.TMINDIRME)) {
            subeId=-1;
            edtDesiTM.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.VISIBLE);
            if (okutmaTipi.equals(OkutmaTipleri.SUBEYUKLEME)){
                edtBarcode.setVisibility(View.VISIBLE);
                edtBarcode.requestFocus();
                checkBarcodePrinter.setVisibility(View.VISIBLE);
            }
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BaseUrl.Url)
                    .addConverterFactory(GsonConverterFactory.create())
//                .client(getUnsafeOkHttpClient().build())
                    .build();
            JetizzService service = retrofit.create(JetizzService.class);
            String token = loginResponse.getAccess_token();
            Call<List<SpinnerModel>> responseCall = service.GetMusteriList("Bearer " + token);
            alertDialog = CommonUtils.showLoadingDialogWithMessage(this, "Lütfen Bekleyinizz");
            responseCall.enqueue(new Callback<List<SpinnerModel>>() {
                @Override
                public void onResponse(Call<List<SpinnerModel>> call, Response<List<SpinnerModel>> response) {
                    if (response.isSuccessful()) {
                        alertDialog.dismiss();
                        spinnerCustomerList = response.body();

                        adapter = new SpinnerAdapter(getApplicationContext(), R.layout.spinner_row, spinnerCustomerList);
                        spinner.setAdapter(adapter);

                    } else {
                        if (response.raw().code() == 401)
                            CommonUtils.showTokenExpired(BluetoothBarcodeReaderActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                        else
                            CommonUtils.showAlertWithMesseage(BluetoothBarcodeReaderActivity.this, "İşlem Başarısız", "UYARI");
                        alertDialog.dismiss();
//                    Toast.makeText(LoginActivity.this,"login error",Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<List<SpinnerModel>> call, Throwable t) {
                    alertDialog.dismiss();
                    CommonUtils.showAlertWithMesseage(BluetoothBarcodeReaderActivity.this, t.getMessage(), "UYARI");
//                Toast.makeText(LoginActivity.this,"login ss error",Toast.LENGTH_LONG).show();
                }
            });
        } else if (okutmaTipi.equals(OkutmaTipleri.DAGITIMSUBESEVK)) {
            musteriId = -1;
            subeSpinner.setVisibility(View.VISIBLE);
            checkFarkliAcente.setVisibility(View.VISIBLE);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BaseUrl.Url)
                    .addConverterFactory(GsonConverterFactory.create())
//                   .client(getUnsafeOkHttpClient().build())
                    .build();
            JetizzService service = retrofit.create(JetizzService.class);
            String token = loginResponse.getAccess_token();
            Call<List<SpinnerModel>> responseCall = service.GetSubeler("Bearer " + token);
            alertDialog = CommonUtils.showLoadingDialogWithMessage(this, "Lütfen Bekleyinizz");
            responseCall.enqueue(new Callback<List<SpinnerModel>>() {
                @Override
                public void onResponse(Call<List<SpinnerModel>> call, Response<List<SpinnerModel>> response) {
                    if (response.isSuccessful()) {
                        alertDialog.dismiss();
                        spinnerBranchList = response.body();

                        adapter = new SpinnerAdapter(getApplicationContext(), R.layout.spinner_row, spinnerBranchList);
                        subeSpinner.setAdapter(adapter);

                    } else {
                        if (response.raw().code() == 401)
                            CommonUtils.showTokenExpired(BluetoothBarcodeReaderActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                        else
                            CommonUtils.showAlertWithMesseage(BluetoothBarcodeReaderActivity.this, "İşlem Başarısız", "UYARI");
                        alertDialog.dismiss();
//                    Toast.makeText(LoginActivity.this,"login error",Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<List<SpinnerModel>> call, Throwable t) {
                    alertDialog.dismiss();
                    CommonUtils.showAlertWithMesseage(BluetoothBarcodeReaderActivity.this, t.getMessage(), "UYARI");
//                Toast.makeText(LoginActivity.this,"login ss error",Toast.LENGTH_LONG).show();
                }
            });
        } else {
            musteriId = -1;
            subeId=-1;
        }
//        spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                SpinnerModel model = (SpinnerModel) adapter.getItemObj(position);
//                musteriId = model.getValue();
//            }
//        });
//        edtBarcode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                int i = 5; // added this to set a break point
//                return false;
//            }
//        });

//        edtBarcode.setOnKeyListener(new View.OnKeyListener() {
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                // If the event is a key-down event on the "enter" button
//                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
//                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
//                    // Perform action on key press
//                    resultData(edtBarcode.getText().toString());
//                }
//                return false;
//            }
//        });
        edtBarcode.setOnEditorActionListener(new TextView.OnEditorActionListener()

        {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event)
            {
                String textBarcode = v.getText().toString();
                edtBarcode.setSelection(textBarcode.length());

                if (textBarcode.isEmpty())
                    return false;

                edtBarcode.clearFocus();
                if (event != null) {
                    // Barcode finger scanner

                    if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                        resultData(textBarcode);
                    }
                } else {
                    // Keyboard

//            hideKeyboard();
                    resultData(textBarcode);
                }

                return false;
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
        subeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerModel model = (SpinnerModel) adapter.getItemObj(position);
                subeId = model.getValue();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    @OnEditorAction(R.id.edtBarcode)
    public boolean onEditorAction(TextView view, int keyCode, KeyEvent keyEvent) {

        String textBarcode = view.getText().toString();
        edtBarcode.setSelection(textBarcode.length());

        if (textBarcode.isEmpty())
            return false;

        edtBarcode.clearFocus();
        if (keyEvent != null) {
            // Barcode finger scanner

            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                resultData(textBarcode);
            }
        } else {
            // Keyboard

//            hideKeyboard();
            resultData(textBarcode);
        }

        return false;
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
                                            Toast.makeText(BluetoothBarcodeReaderActivity.this, data, Toast.LENGTH_SHORT).show();
                                            if (okutmaTipi.equals(OkutmaTipleri.SUBEYUKLEME) && checkBarcodePrinter.isChecked())
                                            {
                                                edtBarcode.setText(data);
                                                edtBarcode.setSelection(edtBarcode.getText().length());
//                                                resultData(edtBarcode.getText().toString());

                                            }
                                            else{
                                                resultData(data);
                                            }


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

            if (musteriId == 0) {
                CommonUtils.showAlertWithMesseage(BluetoothBarcodeReaderActivity.this, "Lütfen müşteri seçiniz", "UYARI");
            }
            else if (subeId == 0)
            {
                CommonUtils.showAlertWithMesseage(BluetoothBarcodeReaderActivity.this, "Lütfen şube seçiniz", "UYARI");
            }
            else {

                if (!barcodeList.contains(barcode)) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BaseUrl.Url)
                            .addConverterFactory(GsonConverterFactory.create())
//                           .client(getUnsafeOkHttpClient().build())
                            .build();
                    JetizzService service = retrofit.create(JetizzService.class);
                    String token = loginResponse.getAccess_token();

                    HareketRequestModel requestModel = new HareketRequestModel();
                    requestModel.setBarkod(barcode);
                    requestModel.setKaynak("MOBIL");
                    requestModel.setMusteriId(musteriId);
                    requestModel.setSube(String.valueOf(subeId));
                    if (!edtDesiTM.getText().toString().isEmpty())
                        requestModel.setDesi(Double.parseDouble(edtDesiTM.getText().toString()));
                    else
                        requestModel.setDesi(0);
                    if (!MAC_Address.isEmpty()) {
                        requestModel.setKaynakIp(MAC_Address);
                    } else {
                        requestModel.setKaynakIp("0");
                    }

                    requestModel.setKoordinat(koordinat);
                    requestModel.setIsKoliNo("false");
                    requestModel.setFarkliAcente(checkFarkliAcente.isChecked());
                    requestModel.setBarkodPrint(checkBarcodePrinter.isChecked());

                    if (okutmaTipi.equals(OkutmaTipleri.MERKEZDENGELEN)) {
                        responseCall = service.MerkezdenGelen("Bearer " + token, barcode, "MOBIL", "false");
                    } else if (okutmaTipi.equals(OkutmaTipleri.SUBEYUKLEME)) {
                        responseCallSubeYukleme = service.SubeYuklemeMobil("Bearer " + token, requestModel);
                    } else if (okutmaTipi.equals(OkutmaTipleri.SUBEINDIRME)) {
                        responseCall = service.SubeIndirme("Bearer " + token, barcode, "MOBIL","false");
                    } else if (okutmaTipi.equals(OkutmaTipleri.HATYUKLEME)) {
                        responseCall = service.HatYukleme("Bearer " + token, barcode, "MOBIL", "false");
                    } else if (okutmaTipi.equals(OkutmaTipleri.HATINDIRME)) {
                        responseCall = service.HatIndirme("Bearer " + token, barcode, "MOBIL", "false");
                    } else if (okutmaTipi.equals(OkutmaTipleri.ZIMMETAL)) {
                        responseCall = service.ZimmetAl("Bearer " + token, barcode, "", "", "MOBIL", "false");
                    } else if (okutmaTipi.equals(OkutmaTipleri.TMSEVK)) {
                        responseCall = service.TMSevk("Bearer " + token, barcode, "MOBIL", "false");
                    } else if (okutmaTipi.equals(OkutmaTipleri.TMINDIRME)) {
                        responseCall = service.CikisTMIndirmeMobil("Bearer " + token, requestModel);
                    } else if (okutmaTipi.equals(OkutmaTipleri.DAGITIMSUBESEVK)) {
//                    responseCall = service.DagitimSubeSevk("Bearer " + token, barcode,"MOBIL",String.valueOf(checkmuskolino.isChecked()));
                        responseCall = service.DagitimSubeSevkRequest("Bearer " + token, requestModel);
                    }

                    Log.v("token", loginResponse.getAccess_token());

                    alertDialog = CommonUtils.showLoadingDialogWithMessage(this, "Lütfen Bekleyiniz");

                    if (okutmaTipi.equals(OkutmaTipleri.SUBEYUKLEME)) {
                        responseCallSubeYukleme.enqueue(new Callback<ApiResponseModelObj<String[]>>() {
                            @Override
                            public void onResponse(Call<ApiResponseModelObj<String[]>> call, retrofit2.Response<ApiResponseModelObj<String[]>> response) {
                                alertDialog.dismiss();
                                if (response.isSuccessful()) {
                                    String status = response.body().Status;
                                    if (status.equals("success")) {
                                        barcodeList.add(barcode);
                                        txtOkutulanBarkodSayisi.setText(String.valueOf(barcodeList.size()));
                                        edtBarcode.setText("");
                                        if(response.body().Result != null){
                                            if (response.body().Result.length > 0) {
                                                edtBarcode.setText("");
                                                edtBarcode.requestFocus();
                                                writeData(response.body().Result);
                                            }

                                        }

                                    } else {
                                        if (checkBarcodePrinter.isChecked()){
                                            edtBarcode.setText("");
                                            edtBarcode.requestFocus();
                                        }
                                        CommonUtils.showAlertWithMesseage(BluetoothBarcodeReaderActivity.this, response.body().Message, "UYARI");
                                        vibrate();
                                    }


                                } else {
                                    alertDialog.dismiss();
                                    if (checkBarcodePrinter.isChecked()){
                                        edtBarcode.setText("");
                                        edtBarcode.requestFocus();
                                    }
                                    if (response.raw().code() == 401)
                                        CommonUtils.showTokenExpired(BluetoothBarcodeReaderActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                                    else
                                        CommonUtils.showAlertWithMesseage(BluetoothBarcodeReaderActivity.this, "İşlem Başarısız", "UYARI");
                                    vibrate();
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponseModelObj<String[]>> call, Throwable t) {
                                alertDialog.dismiss();
                                if (checkBarcodePrinter.isChecked()){
                                    edtBarcode.setText("");
                                    edtBarcode.requestFocus();
                                }
                                ShowDialog.setSnackBar(parentLayout, "Bağlantıda hata oluştu");
//                        ShowDialog.CreateDialog(getParent(),"Bağlantıda hata oluştu","UYARI");
                                vibrate();
                            }
                        });
                    }
                    else{
                        responseCall.enqueue(new Callback<ApiResponseModel>() {
                            @Override
                            public void onResponse(Call<ApiResponseModel> call, retrofit2.Response<ApiResponseModel> response) {
                                alertDialog.dismiss();
                                if (response.isSuccessful()) {
                                    String status = response.body().Status;
                                    if (status.equals("success")) {
                                        barcodeList.add(barcode);
                                        txtOkutulanBarkodSayisi.setText(String.valueOf(barcodeList.size()));

                                    } else {

                                        CommonUtils.showAlertWithMesseage(BluetoothBarcodeReaderActivity.this, response.body().Message, "UYARI");
//                                ShowDialog.setSnackBar(parentLayout,response.body().Message);
                                        vibrate();
                                    }


                                } else {
                                    alertDialog.dismiss();
                                    if (response.raw().code() == 401)
                                        CommonUtils.showTokenExpired(BluetoothBarcodeReaderActivity.this, "Oturum süresi doldu. Tekrar giriş yapınız", "UYARI");
                                    else
                                        CommonUtils.showAlertWithMesseage(BluetoothBarcodeReaderActivity.this, "İşlem Başarısız", "UYARI");
//                            ShowDialog.setSnackBar(parentLayout,"İşlem Başarısız");
                                    vibrate();
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponseModel> call, Throwable t) {
                                alertDialog.dismiss();

                                ShowDialog.setSnackBar(parentLayout, "Bağlantıda hata oluştu");
//                        ShowDialog.CreateDialog(getParent(),"Bağlantıda hata oluştu","UYARI");
                                vibrate();
                            }
                        });
                    }

                }
            }
        } catch (NullPointerException e) {

            Toast.makeText(getApplicationContext(), "Birşeyler yanlış gitti!", Toast.LENGTH_SHORT).show();
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
    public void writeData(String[] data) {
        try {

            if (mmOutputStream != null) {
                for (String item : data) {
                    if (item != null && !item.isEmpty()) {
                        byte[] by = item.getBytes();
                        mmOutputStream.write(by);
                        Toast.makeText(BluetoothBarcodeReaderActivity.this, "Data Sent", Toast.LENGTH_SHORT).show();
                    }
                }

            } else {
                Toast.makeText(BluetoothBarcodeReaderActivity.this, "Aktif bir bağlantı bulunmuyor!", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {

            Toast.makeText(BluetoothBarcodeReaderActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    public class BluetoothConnectionAsyncTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            alertDialog = new SpotsDialog.Builder()
                    .setContext(BluetoothBarcodeReaderActivity.this)
                    .setMessage("Bağlanıyor")
                    .setCancelable(false)
                    .build();

            alertDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String sonuc = "";

            try {
                if ((alertDialog != null) && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
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
                    ButtonText("Bağlantı Kapat");
                } else {
                    ButtonText("Bağlan");
                    UpdateLabel(result);
                }
            }
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
}

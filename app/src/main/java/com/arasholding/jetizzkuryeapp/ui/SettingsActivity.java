package com.arasholding.jetizzkuryeapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.pref.SharedPref;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {
    MaterialSpinner bluetoothSpanner;
    BluetoothAdapter mBluetoothAdapter;
    String selectedBluetoothDeviceName;
    SharedPref sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setDisplayShowHomeEnabled(true);
//        }

        sharedPref = new SharedPref(this);
        bluetoothSpanner = findViewById(R.id.spinner_list);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        getBluetoothDeviceNames();
        selectedBluetoothDeviceName = sharedPref.getPairedDeviceName();
        setSelectedBluetoothDevice(selectedBluetoothDeviceName);
    }
    public void setSelectedBluetoothDevice(String selectedBluetoothDeviceName) {

        int index = bluetoothSpanner.getItems().indexOf(selectedBluetoothDeviceName);

        if (index >= 0)
            bluetoothSpanner.setSelectedIndex(index);
    }

    private List<String> getBluetoothDeviceNames() {
        List<String> resultlist = new ArrayList<>();

        if (mBluetoothAdapter != null) {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    resultlist.add(device.getName());
                    if (resultlist != null && resultlist.size() > 0) {
                        bluetoothSpanner.setItems(resultlist);
                        bluetoothSpanner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

                            @Override
                            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                                sharedPref.setPairedDeviceName(item);
                            }
                        });
                    } else {
                        bluetoothSpanner.setItems("Bluetooth cihazı seçiniz");
                    }

                }
            }
            else {
                bluetoothSpanner.setItems("Bluetooth cihazı seçiniz");
            }
        }

        return resultlist;
    }
}

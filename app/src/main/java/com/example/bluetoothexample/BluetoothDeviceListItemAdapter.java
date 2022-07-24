package com.example.bluetoothexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class BluetoothDeviceListItemAdapter extends ArrayAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<String> data;

    TextView deviceName;
    TextView connectState;

    public BluetoothDeviceListItemAdapter(Context context, ArrayList<String> bluetoothDevices) {
        super(context, 0, bluetoothDevices);
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.data = bluetoothDevices;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        v = layoutInflater.inflate(R.layout.bluetooth_device_list_item_view, null);
        deviceName = v.findViewById(R.id.tvDeviceName);
        connectState = v.findViewById(R.id.tvConnectState);

        deviceName.setText(data.get(position));

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return v;
    }
}

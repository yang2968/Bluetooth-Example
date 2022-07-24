package com.example.bluetoothexample.Bluetooth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluetoothexample.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BluetoothDevicesSimpleAdapter extends SimpleAdapter {
    LayoutInflater layoutInflater;
    Context context;
    ArrayList<HashMap<String, String>> deviceData;

    TextView deviceName, connectState;

    public BluetoothDevicesSimpleAdapter(Context context, ArrayList<HashMap<String, String>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.context = context;
        this.deviceData = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        View view = layoutInflater.inflate(R.layout.bluetooth_device_list_item_view, null);
        deviceName = view.findViewById(R.id.tvDeviceName);
        connectState = view.findViewById(R.id.tvConnectState);

        deviceName.setText(deviceData.get(position).get("name"));

        if(deviceData.get(position).get("connectState").equals("연결됨")) {
            connectState.setText("연결됨");
            connectState.setTextColor(context.getResources().getColor(R.color.connected));
        } else if(deviceData.get(position).get("connectState").equals("연결 중...")) {
            connectState.setText("연결 중...");
            connectState.setTextColor(context.getResources().getColor(R.color.disConnected));
        } else {
            connectState.setText("연결 안됨");
            connectState.setTextColor(context.getResources().getColor(R.color.disConnected));
        }

        return view;
    }

    @Override
    public int getCount() {
        return deviceData.size();
    }
}

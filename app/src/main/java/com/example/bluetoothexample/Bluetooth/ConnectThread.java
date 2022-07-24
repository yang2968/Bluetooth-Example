package com.example.bluetoothexample.Bluetooth;

import static com.example.bluetoothexample.MainActivity.binding;
import static com.example.bluetoothexample.MainActivity.bluetoothAdapter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bluetoothexample.BluetoothSearchActivity;
import com.example.bluetoothexample.MainActivity;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

public class ConnectThread  extends Thread {
    private String TAG = "ConnectThread";
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
//    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // UUID 생성
    private UUID uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // UUID 생성
    private ConnectedThread connectedThread;
    private int position = 0; // 데이터
    private int division = 0; // 등록된 장치 리스트뷰인지, 검색된 장치 리스트뷰인지 구분 변수

    @SuppressLint("MissingPermission")
    public ConnectThread(BluetoothDevice device, int pos, int div) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        mmDevice = device;
        position = pos;
        division = div;
        BluetoothSocket tmp = null;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = createBluetoothSocket(mmDevice);

            //tmp = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    @SuppressLint("MissingPermission")
    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        bluetoothAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
                ((MainActivity) MainActivity.context_main).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText((MainActivity.context_main), "에러가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                        binding.tvConnectedBluetoothDevice.setVisibility(View.GONE);
                    }
                });
                (BluetoothSearchActivity.context_BS).stopService(new Intent( (BluetoothSearchActivity.context_BS).getApplicationContext(), BluetoothConnectService.class)); // 백그라운드 장치 연결 서비스 중지
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }
        // 블루투스 장치와 연결 성공
        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        ((BluetoothSearchActivity) BluetoothSearchActivity.context_BS).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (division) {
                    case 0: // 등록된 장치
                        ((BluetoothSearchActivity) BluetoothSearchActivity.context_BS).pairedBluetoothDevice.get(position).put("connectState", "연결됨"); // hashmap 값 변경
                        ((BluetoothSearchActivity) BluetoothSearchActivity.context_BS).pairedBTArrayAdapter.notifyDataSetChanged();
                        break;
                    case 1: // 검색된 장치
                        ((BluetoothSearchActivity) BluetoothSearchActivity.context_BS).searchedBluetoothDevice.remove(position); // hashmap 값 삭제
                        ((BluetoothSearchActivity) BluetoothSearchActivity.context_BS).searchedBTArrayAdapter.notifyDataSetChanged();
                        ((BluetoothSearchActivity) BluetoothSearchActivity.context_BS).checkConnectState();
                        break;
                }
            }
        });
        manageConnectedSocket(mmSocket);
    }

    // 블루투스 소켓 생성
    @SuppressLint("MissingPermission")
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, uuid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return device.createRfcommSocketToServiceRecord(uuid);
    }
    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            if (connectedThread != null) {
                connectedThread.cancel();
            }
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }
}

package com.example.bluetoothexample.Bluetooth;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.bluetoothexample.BluetoothSearchActivity;
import com.example.bluetoothexample.MainActivity;
import com.example.bluetoothexample.R;

public class BluetoothConnectService extends Service {
    public String CHANNEL_ID = "BluetoothConnectService Channel";
    NotificationChannel notificationChannel;
    public NotificationManagerCompat notificationManager;
    public int notificationId = 0;
    ConnectThread connectThread;
    int position = 0;
    int division = 0;

    private BluetoothDevice connectDevice; // 연결할 블루투스 장치

    public BluetoothConnectService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("BackgroundService", "블루투스 장치 연결 서비스 시작");
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);

        notificationManager = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!notificationChannel.isConversation()) { // channel이 생성되지 않았다면
                    notificationManager.createNotificationChannel(notificationChannel); // channel 생성
                }
            }
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_bluetooth_connected_24)
                .setContentTitle("알림")
                .setContentText("블루투스 서비스")
                .build();
//              .setContentIntent(pendingIntent)

        startForeground(1, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        position = intent.getIntExtra("position", -1);
        division = intent.getIntExtra("division", -1);
        Log.i("BackgroundService", String.valueOf(intent.getIntExtra("position", -1)));
        connectDevice = ((BluetoothSearchActivity) BluetoothSearchActivity.context_BS).connectDevice;
        connectThread = new ConnectThread(connectDevice, position, division);
        connectThread.start();
        return  START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("BackgroundService", "블루투스 장치 연결 서비스 중지");
        if (connectThread != null) {
            connectThread.cancel();
        }
        notificationManager.cancel(notificationId); // 알림 없에기
    }
}
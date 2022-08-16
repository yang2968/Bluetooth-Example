package com.example.bluetoothexample;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.bluetoothexample.Bluetooth.BluetoothConnectService;
import com.example.bluetoothexample.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static ActivityMainBinding binding;

    private int permission_state = 0; // 위치 권한 허용 상태(0 : 권한 거부 상태)

    // 블루투스 정보
    public static BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터

    final static int BT_REQUEST_ENABLE = 1;

    public static Context context_main; // Context 변수 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context_main = this;
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(Color.BLACK); // 상태바 색상 설정
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide(); // 액션바 없애기
        checkPermissions(); // 필요한 모든 권한 요청

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (permission_state == 1) {
            if (bluetoothAdapter.isEnabled()) {
                binding.tvBluetoothStatus.setTextColor(Color.GREEN);
                binding.tvBluetoothStatus.setText("Bluetooth ON");
                binding.btnSearch.setEnabled(true);
            } else if (!bluetoothAdapter.isEnabled()) {
                binding.tvBluetoothStatus.setTextColor(Color.RED);
                binding.tvBluetoothStatus.setText("Bluetooth OFF");
                binding.btnSearch.setEnabled(false);
            }
        } else {
            binding.tvBluetoothStatus.setTextColor(Color.RED);
            binding.tvBluetoothStatus.setText("Request Bluetooth Permission");
            binding.btnSearch.setEnabled(false);
        }


        binding.btnBluetoothOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothOn();
            }
        });

        binding.btnBluetoothOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothOff();
            }
        });

        binding.btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, BluetoothSearchActivity.class));
            }
        });
    }

    public void bluetoothOn() {
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show();
        } else {
            if (bluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "Already Bluetooth ON", Toast.LENGTH_LONG).show();
                binding.tvBluetoothStatus.setTextColor(Color.GREEN);
                binding.tvBluetoothStatus.setText("Bluetooth ON");
                binding.btnSearch.setEnabled(true);
            } else {
                //Toast.makeText(getApplicationContext(), "블루투스가 활성화 되어 있지 않습니다.", Toast.LENGTH_LONG).show();
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // 블루투스 연결 권한 리스트
                    String[] permission_list = {
                            Manifest.permission.BLUETOOTH_CONNECT
                    };
                    // 각 권한의 허용 여부를 확인
                    for (String permission : permission_list) {
                        int check = checkCallingOrSelfPermission(permission); // 권한 혀용 여부
                        // 권한 거부 상태
                        if (check == PackageManager.PERMISSION_DENIED) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(permission_list, 1); // 권한 허용 여부 Dialog 출력
                            }
                        }
                    }
                } else {
                    startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    void bluetoothOff() {
        if (bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(MainActivity.this, BluetoothConnectService.class); // 실행 중인 서비스가 있다면 중지
            stopService(intent);
            bluetoothAdapter.disable();
            //Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되었습니다.", Toast.LENGTH_SHORT).show();
            binding.tvBluetoothStatus.setTextColor(Color.RED);
            binding.tvBluetoothStatus.setText("Bluetooth OFF");
            binding.btnSearch.setEnabled(false);
        } else {
            Toast.makeText(getApplicationContext(), "Already Bluetooth OFF", Toast.LENGTH_SHORT).show();
        }
    }

    public void checkPermissions() {
        // 위치 권한 리스트
        String[] permission_list = {
                Manifest.permission.ACCESS_FINE_LOCATION, // 위치 제공
                Manifest.permission.ACCESS_COARSE_LOCATION, // 더 정확한 위치 제공, 블루투스를 통해 근처 기기에 연결하는 등의 작업에 필요
                Manifest.permission.BLUETOOTH_SCAN, // Android 12부터 필요한 블루투스 권한들 없으면 블루투스 관련 기능 시 에러 발생
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
        };
        // 각 권한의 허용 여부를 확인
        for (String permission : permission_list) {
            int check = checkCallingOrSelfPermission(permission); // 권한 혀용 여부
            // 권한 거부 상태
            if (check == PackageManager.PERMISSION_DENIED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(permission_list, 0); // 권한 허용 여부 Dialog 출력
                }
            } else { // 권한 허용 상태
                permission_state = 1; // 권한 허용 상태로 변경
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                permission_state = 1; // 권한 허용 상태로 변경
                break;
            // 블루투스 권한
            case 1:
                if (grantResults.length > 0 && grantResults[4] == PackageManager.PERMISSION_GRANTED) { // 수정 필요
                    //권한 승인
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, BT_REQUEST_ENABLE); // 선택한 값이 onActivityResult 함수에서 콜백
                } else {
                    // 블루투스 연결 권한 리스트
                    String[] permission_list = {
                            Manifest.permission.BLUETOOTH_CONNECT,
                    };
                    // 각 권한의 허용 여부를 확인
                    for (String permission : permission_list) {
                        int check = checkCallingOrSelfPermission(permission); // 권한 혀용 여부

                        // 권한 거부 상태
                        if (check == PackageManager.PERMISSION_DENIED) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(permission_list, 1); // 권한 허용 여부 Dialog 출력
                            }
                        }
                    }
                    Toast.makeText(getApplicationContext(),  "권한 허용을 해야 기능을 사용할 수 있습니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BT_REQUEST_ENABLE:
                if (resultCode == RESULT_OK) { // 블루투스 활성화를 확인을 클릭하였다면
                    //Toast.makeText(getApplicationContext(), "블루투스 활성화", Toast.LENGTH_LONG).show();
                    binding.tvBluetoothStatus.setTextColor(Color.GREEN);
                    binding.tvBluetoothStatus.setText("Bluetooth ON");
                    binding.btnSearch.setEnabled(true);
                } else if (resultCode == RESULT_CANCELED) { // 블루투스 활성화를 취소를 클릭하였다면
                    //Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_LONG).show();
                    binding.tvBluetoothStatus.setTextColor(Color.RED);
                    binding.tvBluetoothStatus.setText("Bluetooth OFF");
                    binding.btnSearch.setEnabled(false);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
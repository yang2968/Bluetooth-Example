package com.example.bluetoothexample;

import static com.example.bluetoothexample.MainActivity.binding;
import static com.example.bluetoothexample.MainActivity.bluetoothAdapter;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.bluetoothexample.Bluetooth.BluetoothConnectService;
import com.example.bluetoothexample.Bluetooth.BluetoothDevicesSimpleAdapter;
import com.example.bluetoothexample.Bluetooth.ConnectThread;
import com.example.bluetoothexample.databinding.ActivityBluetoothSearchBinding;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class BluetoothSearchActivity extends AppCompatActivity {
    private String TAG = "BluetoothSearchActivity";
    private ActivityBluetoothSearchBinding absBinding;

    public static Context context_BS; // Context 변수 선언

    public BluetoothDevicesSimpleAdapter pairedBTArrayAdapter; // 등록된 블루투스 장치 이름 목록 어댑터
    public BluetoothDevicesSimpleAdapter searchedBTArrayAdapter; // 검색된 블루투스 장치 이름 목록 어댑터

    public ArrayList<HashMap<String, String>> pairedBluetoothDevice; // 등록된 블루투스 장치 이름 목록
    public HashMap<String, String> pairedHashMap = new HashMap<>();
    public ArrayList<HashMap<String, String>> searchedBluetoothDevice; // 검색된 블루투스 장치 이름 목록
    public HashMap<String, String> searchedHashMap  = new HashMap<>();

    public BluetoothDevice connectDevice; // 연결할 블루투스 장치

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context_BS = this;
        absBinding = ActivityBluetoothSearchBinding.inflate(getLayoutInflater());
        setContentView(absBinding.getRoot());
        pairedBluetoothDevice = new ArrayList<>(); // 등록된 블루투스 장치 이름 목록
        searchedBluetoothDevice = new ArrayList<>(); // 등록된 블루투스 장치 이름 목록
        // 블루투스 장치 검색 브로드캐스트 리시버 설정
        IntentFilter filter_search = new IntentFilter(BluetoothDevice.ACTION_FOUND);    // Register for broadcasts when a device is discovered.
        registerReceiver(receiver_search, filter_search);
        // 블루투스 연결 브로드캐스트 리시버 설정
        IntentFilter filter_connected = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED); // 인텐트 필터(블루투스 장치 연결 액션) 객체 생성
        registerReceiver(receiver_connected, filter_connected); // 인텐트 필터(블루투스 장치 연결 액션)를 브로드캐스트 리시버에 등록
        // 블루투스 연결 요청 브로드캐스트 리시버 설정
        IntentFilter filter_pairing_request = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST); // 인텐트 필터(블루투스 장치 연결 요청 액션) 객체 생성
        registerReceiver(receiver_pairing_request, filter_pairing_request); // 인텐트 필터(블루투스 장치 연결 요청 액션)를 브로드캐스트 리시버에 등록
        // 블루투스 연결 해제 브로드캐스트 리시버 설정
        IntentFilter filter_disconnected = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED); // 인텐트 필터(블루투스 장치 연결 해제 액션) 객체 생성
        registerReceiver(receiver_disconnected, filter_disconnected); // 인텐트 필터(블루투스 장치 연결 해제 액션)를 브로드캐스트 리시버에 등록
        // 등록된 기기 리스트뷰 설정
        pairedBTArrayAdapter = new BluetoothDevicesSimpleAdapter(this, pairedBluetoothDevice, R.layout.bluetooth_device_list_item_view, new String[]{"name", "address"}, new int[]{R.id.tvDeviceName, R.id.tvConnectState});
        absBinding.listView.setAdapter(pairedBTArrayAdapter);
        absBinding.listView.setOnItemClickListener(new OnItemClickListener());
        // 검색된 기기 리스트뷰 설정
        searchedBTArrayAdapter = new BluetoothDevicesSimpleAdapter(this, searchedBluetoothDevice, R.layout.bluetooth_device_list_item_view, new String[]{"name", "address"}, new int[]{R.id.tvDeviceName, R.id.tvConnectState});
        absBinding.listView2.setAdapter(searchedBTArrayAdapter);
        absBinding.listView2.setOnItemClickListener(new OnItemClickListener2());
        // 블루투스 장치 연결 상태 체크
        checkConnectState();

        absBinding.btnSearch.setOnClickListener(new View.OnClickListener() { // 주변 블루투스 장치 검색
            @Override
            public void onClick(View view) {
                if (!searchedBTArrayAdapter.isEmpty()) {
                    searchedBluetoothDevice.clear();
                    searchedBTArrayAdapter.notifyDataSetChanged();
                }
                search();
            }
        });
        search();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void checkConnectState() {
        @SuppressLint("MissingPermission")
        Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices(); // 등록된 블루투스 장치 목록 가져오기
        if (pairedDevices.size() > 0) {
            pairedBluetoothDevice.clear();

            for (BluetoothDevice device : pairedDevices) {
                if (isConnected(device)) {
                    @SuppressLint("MissingPermission")
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    Log.i("234", "paired device: " + deviceName + " at " + deviceHardwareAddress);
                    pairedHashMap = new HashMap<>();
                    pairedHashMap.put("name", deviceName);
                    pairedHashMap.put("address", deviceHardwareAddress);
                    pairedHashMap.put("connectState", "연결됨");
                    pairedBluetoothDevice.add(pairedHashMap);
                } else {
                    @SuppressLint("MissingPermission")
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    Log.i("234", "paired device: " + deviceName + " at " + deviceHardwareAddress);
                    pairedHashMap = new HashMap<>();
                    pairedHashMap.put("name", deviceName);
                    pairedHashMap.put("address", deviceHardwareAddress);
                    pairedHashMap.put("connectState", "연결 안됨");
                    pairedBluetoothDevice.add(pairedHashMap);
                }
            }
            pairedBTArrayAdapter.notifyDataSetChanged();
        }
    }

    // 연결 상태 확인
    public boolean isConnected(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("isConnected", (Class[]) null);
            boolean connected = (boolean) m.invoke(device, (Object[]) null);
            return connected; // 연결 상태 반환(true : 연결, false : 연결 해제)
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressLint("MissingPermission")
    public void search() {
        if (bluetoothAdapter.isDiscovering()) { // 이미 검색 중인 경우
            bluetoothAdapter.cancelDiscovery(); // 검색 중지
        }
        bluetoothAdapter.startDiscovery();
    }

    private BroadcastReceiver receiver_search = new BroadcastReceiver() { // Create a BroadcastReceiver for ACTION_FOUND.
        public void onReceive(Context context, Intent intent) { // 블루투스 장치 검색 리시버
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                @SuppressLint("MissingPermission")
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if(deviceName != null && deviceHardwareAddress != null) { // 이름과 주소가 null이 아닌 경우에만 등록
                    Log.i("검색된 기기: ", deviceName);
                    Log.i("검색된 기기 주소", deviceHardwareAddress);
                    searchedHashMap = new HashMap<>();
                    searchedHashMap.put("name", deviceName);
                    searchedHashMap.put("address", deviceHardwareAddress);
                    searchedHashMap.put("connectState", "연결 안됨");
                    searchedBluetoothDevice.add(searchedHashMap);

                    if(pairedBluetoothDevice.size() > 0) {
                        for(int i=0;i<pairedBluetoothDevice.size();i++) { // 등록된 블루투스 장치들과 검색된 블루투스 장치들을 비교하여 중복된 값이 있으면 없앰
                            String name = pairedBluetoothDevice.get(i).get("name");
                            String address = pairedBluetoothDevice.get(i).get("address");
                            if(deviceName.equals(name) && deviceHardwareAddress.equals(address)) {
                                for(int j=0;j<searchedBluetoothDevice.size();j++) {
                                    if (searchedBluetoothDevice.get(j).get("name").equals(name)) {
                                        searchedBluetoothDevice.remove(j);
                                    }
                                }
                            }
                        }
                    }
                    searchedBTArrayAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    private final BroadcastReceiver receiver_connected = new BroadcastReceiver() {  // 블루투스 장치 연결 브로드캐스트 리시버
        // 블루투스 장치가 연결되면 onReceive() 메소드가 자동 호출
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); // onResume() 메소드를 호출한 액션

            // 액션이 블루투스 장치 연결 액션일 때
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.i("커넥트", "커넥트");
                //pairingBluetoothListState(); // 현재 연결 상태 확인
            }
        }
    };
    private final BroadcastReceiver receiver_pairing_request = new BroadcastReceiver() {  // 블루투스 장치 연결 요청 브로드캐스트 리시버
        // 블루투스 장치가 연결 요청되면 onReceive() 메소드가 자동 호출
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); // onResume() 메소드를 호출한 액션

            // 액션이 블루투스 장치 연결 요청 액션일 때
            if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                Log.i("커넥트 요청", "커넥트 요청");
                //pairingBluetoothListState(); // 현재 연결 상태 확인
            }
        }
    };
    private final BroadcastReceiver receiver_disconnected = new BroadcastReceiver() {  // 블루투스 장치 연결 해제 브로드캐스트 리시버
        // 블루투스 장치가 연결 해제되면 onReceive() 메소드가 자동 호출
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); // onResume() 메소드를 호출한 액션

            // 액션이 블루투스 장치 연결 해제 액션일 때
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.i("디스커넥트", "디스커넥트");
                checkConnectState(); // 현재 연결 상태 확인
            }
        }
    };

    // 등록된 기기 리스트뷰 아이템 클릭 리스너
    public class OnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            connectDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(pairedBluetoothDevice.get(position).get("address"));
            if(pairedBluetoothDevice.get(position).get("connectState").equals("연결 안됨")) { // 블루투스 장치 연결 X
                pairedBluetoothDevice.get(position).put("connectState", "연결 중..."); // hashmap 값 변경
                pairedBTArrayAdapter.notifyDataSetChanged();
                binding.tvConnectedBluetoothDevice.setVisibility(View.GONE);
                int div = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent intent = new Intent(BluetoothSearchActivity.this, BluetoothConnectService.class);
                    intent.putExtra("position", position);
                    intent.putExtra("division", div);
                    startForegroundService(intent);
                } else {
                    Intent intent = new Intent(BluetoothSearchActivity.this, BluetoothConnectService.class);
                    intent.putExtra("position", position);
                    intent.putExtra("division", div);
                    startService(intent);
                }
            } else { // 블루투스 장치 연결 중일 때
                Intent intent = new Intent(BluetoothSearchActivity.this, BluetoothConnectService.class);
                stopService(intent);
                pairedBluetoothDevice.get(position).put("connectState", "연결 안됨"); // hashmap 값 변경
                pairedBTArrayAdapter.notifyDataSetChanged();
                binding.tvConnectedBluetoothDevice.setVisibility(View.GONE);
            }

        }
    }

    // 검색된 기기 리스트뷰 아이템 클릭 리스너
    public class OnItemClickListener2 implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            connectDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(searchedBluetoothDevice.get(position).get("address")); // 연결할 블루투스 장치 BluetoothDevice 설정
            if(searchedBluetoothDevice.get(position).get("connectState").equals("연결 안됨")) { // 블루투스 장치 연결 X
                searchedBluetoothDevice.get(position).put("connectState", "연결 중..."); // hashmap 값 변경
                searchedBTArrayAdapter.notifyDataSetChanged();
                int div = 1;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent intent = new Intent(BluetoothSearchActivity.this, BluetoothConnectService.class);
                    intent.putExtra("position", position);
                    intent.putExtra("division", div);
                    startForegroundService(intent);
                } else {
                    Intent intent = new Intent(BluetoothSearchActivity.this, BluetoothConnectService.class);
                    intent.putExtra("position", position);
                    intent.putExtra("division", div);
                    startService(intent);
                }
            } else { // 블루투스 장치 연결 중일 때
                Log.i(TAG, "코드 꼬임 GG");
//                Intent intent = new Intent(BluetoothSearchActivity.this, BluetoothConnectService.class);
//                stopService(intent);
//                searchedBluetoothDevice.get(position).put("connectState", "연결 안됨"); // hashmap 값 변경
//                searchedBTArrayAdapter.notifyDataSetChanged();
//                binding.tvConnectedBluetoothDevice.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver_search);
        unregisterReceiver(receiver_connected);
        unregisterReceiver(receiver_pairing_request);
        unregisterReceiver(receiver_disconnected);
    }
}
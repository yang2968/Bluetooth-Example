package com.example.bluetoothexample.Bluetooth;

import static com.example.bluetoothexample.MainActivity.binding;

import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bluetoothexample.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {
    private String TAG = "ConnectedThread";
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    private int readBufferPosition; // 버퍼 내 문자열 저장 위치
    private byte[] readBuffer; // 수신된 문자열을 저장하기 위한 버퍼

    public ConnectedThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        // 데이터를 수신하기 위한 버퍼 생성
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        while (true) {
            try {
                // 데이터 수신 확인
                int byteAvailable = mmInStream.available();
                // 데이터가 수신된 경우
                if(byteAvailable > 0) {
                    // 입력 스트림에서 바이트 단위로 읽음
                    byte[] bytes = new byte[byteAvailable];
                    mmInStream.read(bytes);
                    // 입력 스트림 바이트를 한 바이트씩 읽음
                    for(int i = 0; i < byteAvailable; i++) {
                        byte tempByte = bytes[i];

                        // 개행문자를 기준으로 나눔(한줄)
                        if(tempByte == '\n') {
                            // readBuffer 배열을 encodedBytes로 복사
                            byte[] encodedBytes = new byte[readBufferPosition];
                            System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);

                            // 인코딩된 바이트 배열을 문자열로 변환
                            final String text = new String(encodedBytes, "US-ASCII");
                            readBufferPosition = 0;
                            Log.i("수신받은 데이터 값 : ", text);

                            ((MainActivity) MainActivity.context_main).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText((MainActivity.context_main), "수신 값: " + text, Toast.LENGTH_SHORT).show();
                                }
                            });


                        } // 개행 문자가 아닐 경우
                        else {
                            readBuffer[readBufferPosition++] = tempByte;
                        }
                    }
                }
                Thread.sleep(80); // 0.08초 대기
            } catch (IOException e) {
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }
    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}

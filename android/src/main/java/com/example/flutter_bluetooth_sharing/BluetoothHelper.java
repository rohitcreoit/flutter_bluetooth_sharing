// BluetoothHelper.java
package com.example.flutter_bluetooth_sharing;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import java.io.IOException;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothHelper {
    private static final String TAG = "BluetoothHelper";
    private static final String SERVICE_NAME = "DataSharingService";
    private static final UUID SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothServerSocket serverSocket;
    private BluetoothSocket clientSocket;
    private Thread serverThread;
    private Thread clientThread;
    private Activity activity;

    public BluetoothHelper(Activity activity) {
        this.activity = activity;
    }

    public void startAdvertising() {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Device does not support Bluetooth.");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // You can request the user to enable Bluetooth here
            return;
        }

        if (serverThread == null) {
            serverThread = new Thread(this::startServer);
            serverThread.start();
        }
    }

    private void startServer() {
        try {
            serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID);
            BluetoothSocket socket = serverSocket.accept();

            if (socket != null) {
                manageConnectedSocket(socket);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error in startServer: " + e.getMessage());
        }
    }

    public void startDiscovery() {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Device does not support Bluetooth.");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // You can request the user to enable Bluetooth here
            return;
        }

        if (clientThread == null) {
            clientThread = new Thread(this::startClient);
            clientThread.start();
        }
    }

    private void startClient() {
        try {
            BluetoothDevice targetDevice = null;
            for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
                if (device.getName().equals("TARGET_DEVICE_NAME")) {
                    targetDevice = device;
                    break;
                }
            }

            if (targetDevice == null) {
                Log.e(TAG, "Target device not found.");
                return;
            }

            clientSocket = targetDevice.createRfcommSocketToServiceRecord(SERVICE_UUID);
            clientSocket.connect();

            if (clientSocket.isConnected()) {
                manageConnectedSocket(clientSocket);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error in startClient: " + e.getMessage());
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        // Implement data exchange logic here using InputStream and OutputStream
    }

    public void sendData(String data) {
        // Implement data sending logic here
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}

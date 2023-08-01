package com.example.flutter_bluetooth_sharing;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class FlutterBluetoothSharingPlugin implements FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware {
  private static final String TAG = "FlutterBluetoothSharingPlugin";
  private static final String CHANNEL_NAME = "flutter_bluetooth_sharing";
  private static final UUID SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

  private MethodChannel methodChannel;
  private BluetoothAdapter bluetoothAdapter;
  private BluetoothServerSocket serverSocket;
  private BluetoothSocket clientSocket;
  private Thread serverThread;
  private Thread clientThread;
  private Activity activity;
  private ExecutorService executorService = Executors.newFixedThreadPool(1);

  @Override
  public void onAttachedToEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
    methodChannel = new MethodChannel(binding.getBinaryMessenger(), CHANNEL_NAME);
    methodChannel.setMethodCallHandler(this);
    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
    switch (call.method) {
      case "startAdvertising":
        startAdvertising();
        result.success(null);
        break;
      case "startDiscovery":
        startDiscovery();
        result.success(null);
        break;
      case "getDiscoverableDevices":
        List<Map<String, String>> devices = getDiscoverableDevices();
        result.success(devices);
        break;
      case "connectToDevice":
        Map<String, String> deviceData = call.argument("device");
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceData.get("address"));
        connectToDevice(device);
        result.success(null);
        break;
      case "sendData":
        String data = call.argument("data");
        if (data != null) {
          sendData(data);
          result.success(null);
        } else {
          result.error("INVALID_ARGUMENT", "Data argument is null", null);
        }
        break;
      default:
        result.notImplemented();
        break;
    }
  }

  private void startAdvertising() {
    // ... (same as before)
  }

  private void startDiscovery() {
    // ... (same as before)
  }

  private List<Map<String, String>> getDiscoverableDevices() {
    List<Map<String, String>> devicesList = new ArrayList<>();
    for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
      Map<String, String> deviceData = new HashMap<>();
      deviceData.put("name", device.getName());
      deviceData.put("address", device.getAddress());
      devicesList.add(deviceData);
    }
    return devicesList;
  }

  private void connectToDevice(BluetoothDevice device) {
    executorService.execute(() -> {
      try {
        // Check if the device data is null
        if (device == null) {
          Log.e(TAG, "Device data is null");
          return;
        }

        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(SERVICE_UUID);
        socket.connect();

        if (socket.isConnected()) {
          manageConnectedSocket(socket);
        }
      } catch (IOException e) {
        Log.e(TAG, "Error in connectToDevice: " + e.getMessage());
      }
    });
  }

  private void manageConnectedSocket(BluetoothSocket socket) {
    // Implement data exchange logic here using InputStream and OutputStream
  }

  private void sendData(String data) {
    // Implement data sending logic here
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
    methodChannel.setMethodCallHandler(null);
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivity() {
    activity = null;
  }
}

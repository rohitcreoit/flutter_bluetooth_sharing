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
import java.io.InputStream;
import java.util.Set;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.lang.reflect.Method;
import java.io.IOException;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

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
        connectToDevice(device,result);
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
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    if (bluetoothAdapter == null) {
      Log.e("TAG", "Bluetooth is not available on this device.");
      return;
    }

    try {
      BluetoothServerSocket serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
              "BluetoothSharing", SERVICE_UUID);

      Log.d("TAG", "Advertising started. Waiting for incoming connections...");

      while (true) {
        BluetoothSocket socket = serverSocket.accept();
        if (socket != null) {
          Log.d("TAG", "Device connected: " + socket.getRemoteDevice().getName());

          // Handle the connected socket
          manageConnectedSocket(socket);
        }
      }
    } catch (IOException e) {
      Log.e("TAG", "Error in startAdvertising: " + e.getMessage());
    }
  }


  private void startDiscovery() {
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    if (bluetoothAdapter == null) {
      Log.e("TAG", "Bluetooth is not available on this device.");
      return;
    }

    if (!bluetoothAdapter.isEnabled()) {
      Log.e("TAG", "Bluetooth is not enabled.");
      return;
    }

    // Cancel ongoing discovery if it's active
    if (bluetoothAdapter.isDiscovering()) {
      bluetoothAdapter.cancelDiscovery();
    }

    // Start discovery
    boolean discoveryStarted = bluetoothAdapter.startDiscovery();
    if (discoveryStarted) {
      Log.d("TAG", "Discovery started. Scanning for nearby devices...");

      // Retrieve paired devices
      Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
      for (BluetoothDevice device : pairedDevices) {
        Log.d("TAG", "Paired device found: " + device.getName() + " (" + device.getAddress() + ")");
      }
    } else {
      Log.e("TAG", "Error starting discovery.");
    }
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
    if (device == null) {
      Log.e("TAG", "BluetoothDevice object is null");
      return;
    }

    executorService.execute(() -> {
      try {
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(SERVICE_UUID);
        socket.connect();

        if (socket.isConnected()) {
          manageConnectedSocket(socket);
        }
      } catch (IOException e) {
        Log.e("TAG", "Error in connectToDevice: " + e.getMessage());
      }
    });
  }


  private void manageConnectedSocket(BluetoothSocket socket) {
    try {
      InputStream inputStream = socket.getInputStream();
      OutputStream outputStream = socket.getOutputStream();


      // Example: Sending data
      String dataToSend = "Hello, this is data from Device A!";
      outputStream.write(dataToSend.getBytes());

      // Example: Receiving data
      byte[] buffer = new byte[1024];
      int bytesRead = inputStream.read(buffer);
      if (bytesRead > 0) {
        String receivedData = new String(buffer, 0, bytesRead);
        Log.d("TAG", "Received data: " + receivedData);
      }

      // Close the input and output streams when done
      inputStream.close();
      outputStream.close();
    } catch (IOException e) {
      Log.e("TAG", "Error in manageConnectedSocket: " + e.getMessage());
    }
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

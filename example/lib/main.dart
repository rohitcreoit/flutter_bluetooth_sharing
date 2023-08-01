import 'dart:io';

import 'package:device_info_plus/device_info_plus.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bluetooth_sharing/flutter_bluetooth_sharing.dart';
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  List<BluetoothDevice> discoveredDevices = [];

  @override
  void initState() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      Future.delayed(Duration(milliseconds: 1000), () async {
        await isBTPermissionGiven();
      });
    });
    super.initState();
  }

  Future<bool> isBTPermissionGiven() async {
    if (Platform.isIOS) {
      if (!await Permission.bluetooth.isRestricted) {
        return true;
      } else {
        var response = await [Permission.bluetooth].request();
        return response[Permission.bluetooth]?.isGranted == true;
      }
    } else if (Platform.isAndroid) {
      var isAndroidS = (int.tryParse(
          (await DeviceInfoPlugin().androidInfo).version.release) ??
          0) >=
          11;
      if (isAndroidS) {
        if (await Permission.bluetoothScan.isGranted) {
          return true;
        } else {
          var response = await [
            Permission.bluetoothScan,
            Permission.bluetoothConnect
          ].request();
          return response[Permission.bluetoothScan]?.isGranted == true &&
              response[Permission.bluetoothConnect]?.isGranted == true;
        }
      } else {
        return true;
      }
    }
    return false;
  }

  void _startDiscovery() async {
    try {
      await FlutterBluetoothSharing.startDiscovery();
      print('Discovery started.');
      _discoverDevices();
    } catch (e) {
      print('Error starting discovery: $e');
    }
  }

  void _connectToDevice(BluetoothDevice device) async {
    try {
      print('device: ${device.toMap()}');
      await FlutterBluetoothSharing.connectToDevice(device);
      print('Connected to device: ${device.name}');
    } catch (e) {
      print('Error connecting to device: $e');
    }
  }

  void _sendData() async {
    try {
      String data = 'Hello, this is data from Device A!';
      await FlutterBluetoothSharing.sendData(data);
      print('Data sent successfully: $data');
    } catch (e) {
      print('Error sending data: $e');
    }
  }

  void _discoverDevices() async {
    try {
      List<BluetoothDevice> devices = await FlutterBluetoothSharing.getDiscoverableDevices();
      setState(() {
        discoveredDevices = devices;
      });
    } catch (e) {
      print('Error discovering devices: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: Text('Bluetooth Sharing Example'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              ElevatedButton(
                onPressed: _startDiscovery,
                child: Text('Start Discovery'),
              ),
              ElevatedButton(
                onPressed: _sendData,
                child: Text('Send Data'),
              ),
              SizedBox(height: 20),
              Text('Discovered Devices:'),
              Expanded(
                child: ListView.builder(
                  itemCount: discoveredDevices.length,
                  itemBuilder: (context, index) {
                    final device = discoveredDevices[index];
                    return ListTile(
                      title: Text(device.name),
                      subtitle: Text(device.address),
                      onTap: () => _connectToDevice(device),
                    );
                  },
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

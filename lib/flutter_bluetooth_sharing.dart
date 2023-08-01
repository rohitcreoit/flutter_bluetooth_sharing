import 'package:flutter/services.dart';

class FlutterBluetoothSharing {
  static const MethodChannel _channel = MethodChannel('flutter_bluetooth_sharing');

  static Future<void> startAdvertising() async {
    await _channel.invokeMethod('startAdvertising');
  }

  static Future<void> startDiscovery() async {
    await _channel.invokeMethod('startDiscovery');
  }

  static Future<List<BluetoothDevice>> getDiscoverableDevices() async {
    final List<dynamic>? result = await _channel.invokeMethod('getDiscoverableDevices');
    if (result == null) return [];
    return result.map((deviceData) => BluetoothDevice.fromMap(deviceData)).toList();
  }

  static Future<void> connectToDevice(BluetoothDevice device) async {
    await _channel.invokeMethod('connectToDevice', device.toMap());
  }

  static Future<void> sendData(String data) async {
    await _channel.invokeMethod('sendData', {'data': data});
  }
}

class BluetoothDevice {
  final String name;
  final String address;

  BluetoothDevice(this.name, this.address);

  factory BluetoothDevice.fromMap(Map<dynamic, dynamic> map) {
    return BluetoothDevice(map['name'], map['address']);
  }

  Map<String, dynamic> toMap() {
    return {'name': name, 'address': address};
  }
}

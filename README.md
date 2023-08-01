# Flutter Bluetooth Sharing Plugin

A Flutter plugin for sharing data over Bluetooth between two mobile devices.

![Flutter Version](https://img.shields.io/badge/flutter-%5E2.0.0-blue.svg)

## Features

- Discover nearby Bluetooth devices.
- Start advertising to become discoverable to other devices.
- Connect to a discovered Bluetooth device and share data.

## Installation

Add the following line to your `pubspec.yaml` file:

`dependencies:
  flutter_bluetooth_sharing: ^1.0.0`

## Example

```

import 'package:flutter/material.dart';
import 'package:flutter_bluetooth_sharing/flutter_bluetooth_sharing.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
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
                onPressed: _startAdvertising,
                child: Text('Start Advertising'),
              ),
              ElevatedButton(
                onPressed: _sendData,
                child: Text('Send Data'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _startDiscovery() async {
    try {
      await FlutterBluetoothSharing.startDiscovery();
      print('Discovery started.');
    } catch (e) {
      print('Error starting discovery: $e');
    }
  }

  void _startAdvertising() async {
    try {
      await FlutterBluetoothSharing.startAdvertising();
      print('Advertising started.');
    } catch (e) {
      print('Error starting advertising: $e');
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
}


```
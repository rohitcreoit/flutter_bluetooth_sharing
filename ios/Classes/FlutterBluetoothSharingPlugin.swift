import Flutter
import CoreBluetooth

public class FlutterBluetoothSharingPlugin: NSObject, FlutterPlugin {
    var centralManager: CBCentralManager!
    var discoveredDevices: [CBPeripheral] = []
    var selectedPeripheral: CBPeripheral?

    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "flutter_bluetooth_sharing", binaryMessenger: registrar.messenger())
        let instance = FlutterBluetoothSharingPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }

    public override init() {
        super.init()
        centralManager = CBCentralManager(delegate: self, queue: nil)
    }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case "startAdvertising":
            startAdvertising()
            result(nil)
        case "startDiscovery":
            startDiscovery()
            result(nil)
        case "getDiscoverableDevices":
            result(getDiscoverableDevices())
        case "connectToDevice":
            if let deviceData = call.arguments as? [String: Any] {
                connectToDevice(deviceData)
            }
            result(nil)
        case "sendData":
            if let data = call.arguments as? [String: Any], let dataToSend = data["data"] as? String {
                sendData(dataToSend)
            }
            result(nil)
        default:
            result(FlutterMethodNotImplemented)
        }
    }

    // Implement Bluetooth operations here
    // ...
}

extension FlutterBluetoothSharingPlugin: CBCentralManagerDelegate {
    public func centralManagerDidUpdateState(_ central: CBCentralManager) {
        // Check if Bluetooth is available and powered on
        if central.state == .poweredOn {
            startDiscovery()
        }
    }

    public func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        if !discoveredDevices.contains(peripheral) {
            discoveredDevices.append(peripheral)
        }
    }
}

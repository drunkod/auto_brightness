import 'dart:async';
import 'package:flutter/services.dart';

class SensorService {
  static const EventChannel _sensorEventChannel = EventChannel('sensor_channel');
  Stream<double>? _sensorStream;

  Stream<double> getSensorStream() {
    _sensorStream ??= _sensorEventChannel
        .receiveBroadcastStream()
        .map<double>((dynamic event) => event as double);
    return _sensorStream!;
  }
}

import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class SensorService {
  static const EventChannel _sensorEventChannel = EventChannel('sensor_channel');

  // Test hook to override the platform stream.
  static Stream<double>? _overrideStream;
  static void setTestStream(Stream<double> stream) {
    _overrideStream = stream;
  }

  Stream<double>? _sensorStream;
  StreamSubscription<dynamic>? _platformSub;
  Timer? _fallbackTimer;
  bool _usingFallback = false;
  final StreamController<bool> _fallbackUsedCtrl = StreamController<bool>.broadcast();

  /// Emits true once when the service switches to the debug fallback stream.
  Stream<bool> getFallbackStatusStream() => _fallbackUsedCtrl.stream;
  bool get usingFallback => _usingFallback;

  Stream<double> getSensorStream() {
    if (_overrideStream != null) return _overrideStream!;
    if (_sensorStream != null) return _sensorStream!;

    final controller = StreamController<double>.broadcast(
      onListen: () {
        // Subscribe to platform sensor stream
        _platformSub = _sensorEventChannel
            .receiveBroadcastStream()
            .map<double>((dynamic event) => (event as num).toDouble())
            .listen(
          (value) {
            controller.add(value);
          },
          onError: (Object error, StackTrace st) {
            // If platform reports no sensor (or other error), switch to fallback in debug mode.
            if (kDebugMode && !_usingFallback) {
              _usingFallback = true;
              _fallbackUsedCtrl.add(true);
              // Emit a simple synthetic ramping light pattern.
              double v = 10;
              _fallbackTimer = Timer.periodic(const Duration(milliseconds: 500), (_) {
                controller.add(v);
                v = (v < 1000) ? (v * 1.5) : 10;
              });
            } else {
              controller.addError(error, st);
            }
          },
          onDone: () {
            controller.close();
          },
        );
      },
      onCancel: () async {
        await _platformSub?.cancel();
        _fallbackTimer?.cancel();
      },
    );

    _sensorStream = controller.stream;
    return _sensorStream!;
  }
}

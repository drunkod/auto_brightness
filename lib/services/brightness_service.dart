import 'package:flutter/services.dart';

class BrightnessService {
  static const MethodChannel _channel = MethodChannel('brightness_channel');

  Future<int> getBrightness() async {
    try {
      final int brightness = await _channel.invokeMethod('getBrightness');
      return brightness;
    } catch (e) {
      return 128; // Default brightness
    }
  }

  Future<bool> setBrightness(int brightness) async {
    try {
      final bool result = await _channel.invokeMethod('setBrightness', {
        'brightness': brightness,
      });
      return result;
    } catch (e) {
      return false;
    }
  }

  Future<bool> hasWritePermission() async {
    try {
      final bool hasPermission = await _channel.invokeMethod('hasWritePermission');
      return hasPermission;
    } catch (e) {
      return false;
    }
  }
}

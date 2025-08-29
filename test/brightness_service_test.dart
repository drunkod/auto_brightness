import 'package:auto_brightness/services/brightness_service.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  const chName = 'brightness_channel';
  const channel = MethodChannel(chName);

  setUp(() {
    TestWidgetsFlutterBinding.ensureInitialized();
  });

  tearDown(() {
    ServicesBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
  });

  test('BrightnessService get/set/permission/open settings over MethodChannel', () async {
    int? lastSet;
    bool opened = false;
    ServicesBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (MethodCall call) async {
      switch (call.method) {
        case 'getBrightness':
          return 123;
        case 'setBrightness':
          lastSet = (call.arguments as Map)['brightness'] as int;
          return true;
        case 'hasWritePermission':
          return true;
        case 'openWriteSettings':
          opened = true;
          return true;
      }
      throw PlatformException(code: 'unimplemented');
    });

    final svc = BrightnessService();
    expect(await svc.getBrightness(), 123);
    expect(await svc.hasWritePermission(), true);
    expect(await svc.setBrightness(200), true);
    expect(lastSet, 200);
    expect(await svc.openWriteSettings(), true);
    expect(opened, true);
  });

  test('BrightnessService getBrightness fallback on error', () async {
    ServicesBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (call) async {
      throw PlatformException(code: 'x');
    });

    final svc = BrightnessService();
    expect(await svc.getBrightness(), 128); // default fallback in code
  });
}

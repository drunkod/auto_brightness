import 'dart:async';
import 'package:auto_brightness/main.dart';
import 'package:auto_brightness/services/sensor_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  const brightnessChannel = MethodChannel('brightness_channel');
  bool opened = false;
  late StreamController<double> sensorController;

  setUp(() {
    TestWidgetsFlutterBinding.ensureInitialized();
    SharedPreferences.setMockInitialValues({});
    ServicesBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(brightnessChannel, (MethodCall call) async {
      switch (call.method) {
        case 'getBrightness':
          return 90;
        case 'setBrightness':
          return true;
        case 'hasWritePermission':
          return false; // force banner
        case 'openWriteSettings':
          opened = true;
          return true;
      }
      return null;
    });
    sensorController = StreamController<double>.broadcast();
    SensorService.setTestStream(sensorController.stream);
  });

  tearDown(() {
    ServicesBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(brightnessChannel, null);
    sensorController.close();
    opened = false;
  });

  testWidgets('Permission banner appears and Grant opens settings', (tester) async {
    await tester.pumpWidget(const MyApp());
    await tester.pumpAndSettle(); // NodeList.load

    expect(find.textContaining('permission not granted'), findsOneWidget);
    final grantBtn = find.widgetWithText(TextButton, 'Grant');
    expect(grantBtn, findsOneWidget);

    await tester.tap(grantBtn);
    await tester.pump();
    expect(opened, isTrue);
  });
}

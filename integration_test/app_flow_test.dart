import 'dart:async';
import 'package:auto_brightness/main.dart';
import 'package:auto_brightness/services/sensor_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  const brightnessChannel = MethodChannel('brightness_channel');
  int? lastSet;
  late StreamController<double> sensorController;

  setUpAll(() {
    SharedPreferences.setMockInitialValues({});
    ServicesBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(brightnessChannel, (call) async {
      switch (call.method) {
        case 'getBrightness':
          return 80;
        case 'setBrightness':
          lastSet = (call.arguments as Map)['brightness'] as int;
          return true;
        case 'hasWritePermission':
          return true; // bypass manual permission
        case 'openWriteSettings':
          return true;
      }
      return null;
    });

    // Feed a fake sensor stream so we don’t depend on emulator sensor support
    sensorController = StreamController<double>.broadcast();
    SensorService.setTestStream(sensorController.stream);
  });

  tearDownAll(() async {
    ServicesBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(brightnessChannel, null);
    await sensorController.close();
  });

  testWidgets('Happy path: open app, Auto, Save, slider', (tester) async {
    await tester.pumpWidget(const MyApp());
    await tester.pumpAndSettle(); // NodeList.load

    // After subscription, emit sensor values
    sensorController.add(20.0);
    sensorController.add(200.0);
    await tester.pump(const Duration(milliseconds: 150)); // allow average

    // Tap Auto -> should set some brightness
    await tester.tap(find.text('Auto'));
    await tester.pumpAndSettle();
    expect(lastSet, isNotNull);

    // Move slider
    await tester.drag(find.byType(Slider), const Offset(200, 0));
    await tester.pump();

    // Save mapping (Snackbar should appear)
    await tester.tap(find.text('Save'));
    await tester.pump();
    expect(find.byType(SnackBar), findsOneWidget);
  });
}

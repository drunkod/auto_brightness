import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:auto_brightness/main.dart';
import 'package:auto_brightness/services/sensor_service.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  const brightnessChannel = MethodChannel('brightness_channel');
  late StreamController<double> sensorController;

  setUp(() {
    TestWidgetsFlutterBinding.ensureInitialized();
    SharedPreferences.setMockInitialValues({});
    ServicesBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(brightnessChannel, (MethodCall call) async {
      switch (call.method) {
        case 'getBrightness':
          return 100;
        case 'setBrightness':
          return true;
        case 'hasWritePermission':
          return true;
        case 'openWriteSettings':
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
  });

  testWidgets('MainScreen has controls and loads after NodeList.init',
      (WidgetTester tester) async {
    await tester.pumpWidget(const MyApp());
    // Let FutureBuilder(NodeList.load) finish
    await tester.pumpAndSettle();

    // Provide some sensor samples (not strictly required just to show UI)
    sensorController.add(10.0);
    sensorController.add(20.0);

    // Verify UI
    expect(find.text('Show Node List'), findsOneWidget);
    expect(find.byType(Slider), findsOneWidget);
    expect(find.widgetWithText(ElevatedButton, 'Auto'), findsOneWidget);
    expect(find.widgetWithText(ElevatedButton, 'Save'), findsOneWidget);
  });
}

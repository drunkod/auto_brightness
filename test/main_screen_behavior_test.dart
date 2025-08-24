import 'dart:async';
import 'package:auto_brightness/main.dart';
import 'package:auto_brightness/services/sensor_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  const brightnessChannel = MethodChannel('brightness_channel');
  int? lastSet;
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
          lastSet = (call.arguments as Map)['brightness'] as int;
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

  testWidgets('Auto applies mapped brightness; slider sends setBrightness; Save shows SnackBar',
      (tester) async {
    await tester.pumpWidget(const MyApp());
    await tester.pumpAndSettle(); // NodeList.load

    // Emit sensor samples after subscription is active
    sensorController.add(25.0);
    sensorController.add(150.0);
    // Allow SensorValue to compute average (>=100ms)
    await tester.pump(const Duration(milliseconds: 150));

    // Tap Auto => should call setBrightness
    expect(lastSet, isNull);
    await tester.tap(find.text('Auto'));
    await tester.pumpAndSettle();
    expect(lastSet, isNotNull);
    final afterAuto = lastSet!;

    // Move the slider => calls setBrightness again (value likely changes)
    await tester.drag(find.byType(Slider), const Offset(200, 0));
    await tester.pump();
    expect(lastSet, isNotNull);
    expect(lastSet, isNot(afterAuto));

    // Save mapping => SnackBar
    await tester.tap(find.text('Save'));
    await tester.pump(); // show SnackBar
    expect(find.byType(SnackBar), findsOneWidget);
    expect(find.text('Node saved'), findsOneWidget);
  });
}

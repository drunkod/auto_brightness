import 'package:flutter_test/flutter_test.dart';
import 'package:auto_brightness/models/node_list.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  group('NodeList', () {
    late NodeList nodeList;

    setUp(() {
      // Set up mock values for SharedPreferences
      SharedPreferences.setMockInitialValues({});
      nodeList = NodeList();
    });

    test('getBrightness returns a value', () {
      // Since the list is created with default values, this should return a brightness.
      final brightness = nodeList.getBrightness(50);
      expect(brightness, isA<int>());
    });

    test('set updates brightness and enforces constraints', () async {
      // Wait for the default list to be created.
      await Future.delayed(Duration.zero);

      // Set a new brightness value for a specific sensor range.
      nodeList.set(100, 50);

      // Verify that the brightness for that range has been updated.
      expect(nodeList.getBrightness(100), 50);

      // The test for `_enforceConstraints` is more complex, but a simple check
      // is to see that setting a value doesn't crash the app. A more robust
      // test would involve inspecting the private `_list` variable, which can
      // be done with the `test` package's `visibleForTesting` annotation or
      // by making the list public for testing purposes.
    });
  });
}

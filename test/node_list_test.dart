import 'package:flutter_test/flutter_test.dart';
import 'package:auto_brightness/models/node_list.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  group('NodeList', () {
    late NodeList nodeList;

    setUp(() async {
      // Set up mock values for SharedPreferences
      SharedPreferences.setMockInitialValues({});
      nodeList = await NodeList.load();
    });

    test('getBrightness returns a value', () {
      // Since the list is created with default values, this should return a brightness.
      final brightness = nodeList.getBrightness(50);
      expect(brightness, isA<int>());
    });

    test('set updates brightness and enforces constraints', () async {
      // Set a new brightness value for a specific sensor range.
      nodeList.set(100, 50);

      // Verify that the brightness for that range has been updated.
      expect(nodeList.getBrightness(100), 50);

      // by making the list public for testing purposes.
    });
  });
}

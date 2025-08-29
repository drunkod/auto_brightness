// Integration test driver for running on real devices/emulators via `flutter drive`.
// Uses the extended driver to support screenshots/logs if needed.
import 'package:integration_test/integration_test_driver_extended.dart';

Future<void> main() => integrationDriver();

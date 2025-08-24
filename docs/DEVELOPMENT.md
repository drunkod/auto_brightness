# Development guide: Emulators, Tests, and CI

This guide explains how to:
- run the app on an Android emulator and handle permissions/sensor quirks,
- run unit, widget, and integration tests,
- understand and customize the GitHub Actions CI that builds and tests the app.

Repo name: auto_brightness
Flutter: stable (tested with 3.32.x)
Java: 17

Contents
- 1) Prerequisites
- 2) Run on Android emulator
- 3) Permissions and light sensor behavior
- 4) Tests (unit, widget, integration)
- 5) CI (GitHub Actions)
- 6) Troubleshooting
- 7) Command cheat sheet

1) Prerequisites

Install
- Flutter SDK (stable channel)
- Android Studio or Android SDK + AVD/Emulator
- Java 17

Verify your setup
- flutter doctor -v
- java -version  # should be 17
- adb version

Note on local.properties
- Android’s Gradle config reads flutter.sdk from android/local.properties.
- Running flutter pub get or flutter run will create/update this automatically.
- If you build Android with Gradle directly, ensure local.properties exists and points to your Flutter SDK.

2) Run on Android emulator

Create an AVD (CLI)
- Install a system image (example, API 33/35):
  - sdkmanager "system-images;android-35;google_apis;x86_64"
- Create an AVD:
  - avdmanager create avd -n Pixel_5_API_35 -k "system-images;android-35;google_apis;x86_64" -d pixel_5
- Launch:
  - emulator -avd Pixel_5_API_35
- Confirm device:
  - flutter devices

Run the app
- flutter run -d emulator-5554
- If only one device is connected: flutter run

3) Permissions and light sensor behavior

Write settings permission
- The app needs “Modify system settings” (WRITE_SETTINGS) to change brightness.
- In debug, the UI shows a red banner if permission is missing with a “Grant” button that opens the correct system screen.
- You can also grant via ADB:
  - adb shell appops set ru.yanus171.android.auto_brightness android:write_settings allow

Light sensor availability
- Some AVDs do not expose a light sensor (TYPE_LIGHT).
- In debug builds, if the sensor is missing, the app switches to a synthetic fallback stream and shows a yellow banner. This lets you exercise the UI and logic without a real sensor.
- On emulators that do expose a sensor, you can adjust it via:
  - Emulator Extended Controls > Sensors > Light (Ambient)
- Note: The fallback is debug-only (kDebugMode). Release builds will expect a real sensor.

4) Tests (unit, widget, integration)

Where tests live
- Unit & widget tests: test/
- Device/emulator integration test: integration_test/

Important testing hooks
- NodeList.load(): Async init that ensures preferences are loaded before use. The UI uses FutureBuilder around this.
- SensorService.setTestStream(): Lets tests inject a fake sensor stream instead of using platform channels.
- Brightness channel is mocked in tests via the default binary messenger; no real permission or sensor is required.

Run all unit and widget tests
- flutter test

Run integration test locally on an emulator
- Start an emulator (see section 2).
- Run with flutter drive (matches CI):
  - flutter drive --driver=integration_test/driver.dart --target=integration_test/app_flow_test.dart
- Alternatively (host mode):
  - flutter test integration_test
  - Note: Using flutter drive is preferred when you want parity with CI/emulator runs.

Key test files
- Unit:
  - test/sensor_value_test.dart (SensorValue timing/averaging)
  - test/brightness_service_test.dart (MethodChannel contract)
  - test/node_list_test.dart (NodeList rules; uses NodeList.load())
- Widget:
  - test/main_screen_test.dart (basic UI + async load)
  - test/main_screen_behavior_test.dart (Auto button, slider, Save flow)
  - test/permission_ui_test.dart (permission banner + Grant action)
- Integration (emulator):
  - integration_test/app_flow_test.dart
  - integration_test/driver.dart (used by flutter drive)

5) CI (GitHub Actions)

Workflow: .github/workflows/build-flutter.yml

Jobs and order
- test
  - Runs flutter analyze and flutter test (unit + widget + host integration tests)
- android_integration
  - Boots an Android emulator and runs the integration test via flutter drive.
- build
  - Builds a release APK.
  - Depends on android_integration (and therefore on test). APKs build only if all tests pass.

Triggers
- On push to main
- Manual dispatch from the Actions tab

Artifacts
- The release APK is uploaded as release-apk from build/app/outputs/flutter-apk/app-release.apk

Customize emulator settings
- Job android_integration uses reactivecircus/android-emulator-runner@v2.
- Adjust api-level, target, and arch to match project needs (currently api 33, google_apis, x86_64).
- You can speed up boot by keeping GPU swiftshader_indirect and disabling animations.

6) Troubleshooting

Gradle/Java issues
- Use Java 17 (CI and android/gradle.properties assume modern toolchain).
- If Gradle complains about missing flutter.sdk in local.properties, run flutter pub get or flutter run once to regenerate it.

WRITE_SETTINGS denied
- Use the built-in “Grant” button in debug or:
  - adb shell appops set ru.yanus171.android.auto_brightness android:write_settings allow
- On physical devices, go to Settings > Apps > Auto Brightness > Modify system settings.

No light sensor on emulator
- In debug, you’ll see a yellow banner and a synthetic light stream is used.
- For a real sensor feed, pick an AVD/device that supports it and use Extended Controls > Sensors.

Tests fail due to async init
- NodeList must be fully initialized. Use NodeList.load() in your code and await in tests where needed.

CI emulator flakes
- Rerun the job if the emulator runner times out.
- Try lowering API level (e.g., 30–33), or reduce animations further.
- Ensure you’re not hitting GitHub’s ephemeral CPU limits; the runner config already disables animations and snapshots.

7) Command cheat sheet

Environment
- flutter doctor -v
- flutter pub get
- flutter analyze

Run app
- emulator -avd Pixel_5_API_35
- flutter run -d emulator-5554

Grant permission
- adb shell appops set ru.yanus171.android.auto_brightness android:write_settings allow

Tests
- Unit + widget: flutter test
- Integration (emulator): flutter drive --driver=integration_test/driver.dart --target=integration_test/app_flow_test.dart
- Host-mode integration (optional): flutter test integration_test

Build APK locally
- flutter build apk --release
- Output: build/app/outputs/flutter-apk/app-release.apk

Notes for contributors
- Prefer NodeList.load() over the default constructor to avoid race conditions with SharedPreferences.
- For UI tests, inject a fake sensor stream via SensorService.setTestStream().
- BrightnessService exposes openWriteSettings() for a smoother permission UX in debug builds.

# Auto Brightness App

This document provides instructions for setting up and running the Auto Brightness app on an Android emulator, along with details on the test suite.

---

## Part 1: Running on an Android Emulator

### Prerequisites

-   **Flutter SDK**: Ensure the Flutter SDK is installed and that `flutter doctor` reports no issues.
-   **Android SDK/Emulator**: You need the Android SDK and Emulator, which can be installed via Android Studio or the `sdkmanager` command-line tool.

### Create and Launch an Emulator (CLI)

1.  **Install a system image** (e.g., for API 35):
    ```sh
    sdkmanager "system-images;android-35;google_apis;x86_64"
    ```

2.  **Create an Android Virtual Device (AVD)**:
    ```sh
    avdmanager create avd -n Pixel_5_API_35 -k "system-images;android-35;google_apis;x86_64" -d pixel_5
    ```

3.  **Launch the emulator**:
    ```sh
    emulator -avd Pixel_5_API_35
    ```

4.  **Verify the device is recognized by Flutter**:
    ```sh
    flutter devices
    ```

### Run the App

-   To run the app on a specific emulator (e.g., `emulator-5554`):
    ```sh
    flutter run -d emulator-5554
    ```
-   If you only have one device connected, you can simply run:
    ```sh
    flutter run
    ```

### Grant "Modify system settings" Permission

The app requires the `WRITE_SETTINGS` permission to function correctly. On Android 6.0 (API level 23) and higher, this is a special permission that must be granted manually.

-   **Manually in the Emulator**:
    1.  Go to **Settings > Apps**.
    2.  Select **Auto Brightness**.
    3.  Navigate to **App info**.
    4.  Tap on **Modify system settings** and toggle it to **Allow**.
-   **Via `adb`**:
    ```sh
    adb shell appops set ru.yanus171.android.auto_brightness android:write_settings allow
    ```
    If this permission is not granted, the app will display a red banner stating "Write Settings permission not granted."

### Using the Light Sensor in the Emulator

-   Not all AVDs include a light sensor. If yours does, you can control it via **Emulator Extended Controls > Sensors > Light (Ambient)**.
-   If your AVD lacks a light sensor, the sensor data stream will be empty. For testing purposes, the app includes a hook to inject a fake sensor stream.

---
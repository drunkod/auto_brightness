import 'package:flutter/material.dart';
import '../services/brightness_service.dart';
import '../services/sensor_service.dart';
import '../models/sensor_value.dart';
import '../models/node_list.dart';

class MainScreen extends StatefulWidget {
  const MainScreen({super.key});

  @override
  _MainScreenState createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  final SensorService _sensorService = SensorService();
  final BrightnessService _brightnessService = BrightnessService();
  final NodeList _nodeList = NodeList();
  SensorValue _sensorValue = SensorValue();

  double _currentBrightness = 128.0;
  bool _isNodeListVisible = false;
  bool _hasPermission = true;

  @override
  void initState() {
    super.initState();
    _sensorService.getSensorStream().listen((sensorReading) {
      if (_sensorValue.ready(sensorReading)) {
        setState(() {});
      }
    });
    _brightnessService.getBrightness().then((brightness) {
      setState(() {
        _currentBrightness = brightness.toDouble();
      });
    });
    _brightnessService.hasWritePermission().then((hasPermission) {
      setState(() {
        _hasPermission = hasPermission;
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: SingleChildScrollView(
          child: Center(
            child: Padding(
              padding: const EdgeInsets.all(20.0),
              child: Column(
                children: [
                  if (!_hasPermission)
                    Container(
                      padding: const EdgeInsets.all(10),
                      color: Colors.red,
                      child: const Text(
                        'Write Settings permission not granted',
                        style: TextStyle(color: Colors.white),
                      ),
                    ),
                  GestureDetector(
                    onTap: () {
                      setState(() {
                        _isNodeListVisible = !_isNodeListVisible;
                      });
                    },
                    child: Container(
                      padding: const EdgeInsets.all(10),
                      child: Text(
                        _isNodeListVisible ? _getNodeListString() : 'Show Node List',
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      ElevatedButton(
                        onPressed: _setAutoBrightness,
                        child: const Text('Auto'),
                      ),
                      const SizedBox(width: 10),
                      ElevatedButton(
                        onPressed: _saveCurrentMapping,
                        child: const Text('Save'),
                      ),
                    ],
                  ),
                  const SizedBox(height: 20),
                  Text(
                    'Sensor Data: ${_sensorValue.toString()}',
                    style: Theme.of(context).textTheme.bodyLarge,
                  ),
                  Text(
                    'Brightness: ${_currentBrightness.toInt()}',
                    style: Theme.of(context).textTheme.bodyLarge,
                  ),
                  Slider(
                    value: _brightnessToSlider(_currentBrightness),
                    min: 0,
                    max: 255,
                    divisions: 255,
                    onChanged: _onSliderChanged,
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  double _brightnessToSlider(double brightness) {
    return brightness; // Simplified for example
  }

  void _onSliderChanged(double value) {
    setState(() {
      _currentBrightness = value;
    });
    _brightnessService.setBrightness(value.toInt());
  }

  void _setAutoBrightness() {
    if (_sensorValue.hasValue()) {
      final brightness = _nodeList.getBrightness(_sensorValue.getValue());
      setState(() {
        _currentBrightness = brightness.toDouble();
      });
      _brightnessService.setBrightness(brightness);
    }
  }

  void _saveCurrentMapping() {
    if (_sensorValue.hasValue()) {
      _nodeList.set(_sensorValue.getValue(), _currentBrightness.toInt());
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Node saved')),
      );
    }
  }

  String _getNodeListString() {
    if (_sensorValue.hasValue()) {
      return _nodeList.getString(_sensorValue.getValue());
    }
    return '';
  }
}

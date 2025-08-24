import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
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
  late final Future<NodeList> _nodeListFuture = NodeList.load();
  SensorValue _sensorValue = SensorValue();

  double _currentBrightness = 128.0;
  bool _isNodeListVisible = false;
  bool _hasPermission = true;
  bool _usingFallback = false;

  @override
  void initState() {
    super.initState();
    _sensorService.getSensorStream().listen((sensorReading) {
      if (_sensorValue.ready(sensorReading)) {
        setState(() {});
      }
    });
    _sensorService.getFallbackStatusStream().listen((usingFallback) {
      if (mounted) {
        setState(() {
          _usingFallback = usingFallback;
        });
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
        child: FutureBuilder<NodeList>(
          future: _nodeListFuture,
          builder: (context, snapshot) {
            if (snapshot.connectionState != ConnectionState.done) {
              return const Center(child: Padding(
                padding: EdgeInsets.all(24.0),
                child: CircularProgressIndicator(),
              ));
            }
            if (!snapshot.hasData) {
              return const Center(child: Text('Failed to load settings'));
            }
            final nodeList = snapshot.data!;
            return SingleChildScrollView(
              child: Center(
                child: Padding(
                  padding: const EdgeInsets.all(20.0),
                  child: Column(
                    children: [
                      if (!_hasPermission)
                        Container(
                          padding: const EdgeInsets.all(10),
                          color: Colors.red,
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              const Expanded(
                                child: Text(
                                  'Write Settings permission not granted',
                                  style: TextStyle(color: Colors.white),
                                ),
                              ),
                              TextButton(
                                onPressed: () async {
                                  await _brightnessService.openWriteSettings();
                                },
                                child: const Text(
                                  'Grant',
                                  style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                                ),
                              ),
                            ],
                          ),
                        ),
                      if (_usingFallback)
                        Container(
                          padding: const EdgeInsets.all(10),
                          color: Colors.amber.shade700,
                          child: const Text(
                            'Light sensor not available; using debug fallback stream.',
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
                            _isNodeListVisible ? _getNodeListString(nodeList) : 'Show Node List',
                            textAlign: TextAlign.center,
                          ),
                        ),
                      ),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          ElevatedButton(
                            onPressed: () => _setAutoBrightness(nodeList),
                            child: const Text('Auto'),
                          ),
                          const SizedBox(width: 10),
                          ElevatedButton(
                            onPressed: () => _saveCurrentMapping(nodeList),
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
            );
          },
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

  void _setAutoBrightness(NodeList nodeList) {
    if (_sensorValue.hasValue()) {
      final brightness = nodeList.getBrightness(_sensorValue.getValue());
      setState(() {
        _currentBrightness = brightness.toDouble();
      });
      _brightnessService.setBrightness(brightness);
    }
  }

  void _saveCurrentMapping(NodeList nodeList) {
    if (_sensorValue.hasValue()) {
      nodeList.set(_sensorValue.getValue(), _currentBrightness.toInt());
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Node saved')),
      );
    }
  }

  String _getNodeListString(NodeList nodeList) {
    if (_sensorValue.hasValue()) {
      return nodeList.getString(_sensorValue.getValue());
    }
    return '';
  }
}

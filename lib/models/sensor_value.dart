class SensorValue {
  static const int SENSOR_READ_DURATION_MS = 100;

  DateTime? _readTimer;
  List<int> _valueList = [];
  int _value = -1;

  int getValue() => _value;

  bool ready(double sensorReading) {
    _valueList.add(sensorReading.toInt());

    if (_readTimer == null) {
      _readTimer = DateTime.now();
    } else if (_isReady()) {
      _readTimer = DateTime.now();
      _value = (_valueList.reduce((a, b) => a + b) / _valueList.length).toInt();
      _valueList.clear();
      return true;
    }
    return false;
  }

  bool _isReady() {
    return _readTimer != null &&
        DateTime.now().difference(_readTimer!).inMilliseconds > SENSOR_READ_DURATION_MS;
  }

  bool hasValue() => _value != -1;

  @override
  String toString() {
    return hasValue() ? '$_value' : 'Calculating...';
  }
}

import 'package:shared_preferences/shared_preferences.dart';

class Node {
  int brightness;
  int sensorValue;

  Node(this.sensorValue, this.brightness);

  Node.fromString(String s)
      : brightness = int.parse(s.split(';')[0]),
        sensorValue = int.parse(s.split(';')[1]);

  String save() => '$brightness;$sensorValue';
  String getString() => 's=$sensorValue, b=$brightness';
}

class NodeList {
  static const String nodeListPref = 'node_list5';
  static const int maxBrightness = 255;
  static const int minBrightness = 0;
  static const int nodeCount = 20;

  List<Node> _list = [];

  // Legacy constructor keeps background async loading to avoid breaking callers,
  // but prefer using `NodeList.load()` for explicit, awaited initialization.
  NodeList() {
    _loadFromPreferences();
  }

  NodeList._internal();

  static Future<NodeList> load() async {
    final nl = NodeList._internal();
    await nl._loadFromPreferences();
    return nl;
  }

  int getBrightness(int sensorValue) {
    final node = _getNodeFor(sensorValue);
    return node?.brightness ?? maxBrightness;
  }

  Node? _getNodeFor(int sensorValue) {
    for (final node in _list) {
      if (sensorValue < node.sensorValue) {
        return node;
      }
    }
    return null;
  }

  void set(int sensorValue, int newBrightness) {
    for (final node in _list) {
      if (sensorValue < node.sensorValue) {
        node.brightness = newBrightness;
        _enforceConstraints(node);
        break;
      }
    }
    _save();
  }

  void _enforceConstraints(Node node) {
    // makePrevNonesNotBrighterThen
    for (final it in _list.reversed) {
      if (it.sensorValue < node.sensorValue && it.brightness > node.brightness) {
        it.brightness = node.brightness;
      }
    }
    // makeNextNonesNotDarkerThen
    for (final it in _list) {
      if (it.sensorValue > node.sensorValue && it.brightness < node.brightness) {
        it.brightness = node.brightness;
      }
    }
  }

  String getString(int sensorValue) {
    final currentNode = _getNodeFor(sensorValue);
    return _list.map((node) {
      final current = (currentNode?.sensorValue == node.sensorValue) ? '* ' : '';
      return current + node.getString();
    }).join('\n');
  }

  Future<void> _loadFromPreferences() async {
    final prefs = await SharedPreferences.getInstance();
    final nodeListString = prefs.getString(nodeListPref);

    if (nodeListString == null) {
      _createDefaultList();
    } else {
      _list = nodeListString.split('|').map((item) => Node.fromString(item)).toList();
    }
  }

  void _createDefaultList() {
    _list.clear();
    final bStep = maxBrightness ~/ nodeCount;
    int b = 0;
    int s = 10;

    while (s < 1000) { // Assuming max sensor value
      s = (s * 1.5).toInt();
      b += bStep;
      _list.add(Node(s, b));
    }
  }

  Future<void> _save() async {
    final prefs = await SharedPreferences.getInstance();
    final nodeListString = _list.map((node) => node.save()).join('|');
    await prefs.setString(nodeListPref, nodeListString);
  }
}

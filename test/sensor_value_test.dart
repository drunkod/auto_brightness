import 'package:flutter_test/flutter_test.dart';
import 'package:auto_brightness/models/sensor_value.dart';
import 'package:fake_async/fake_async.dart';

void main() {
  test('SensorValue averages readings after 100ms', () {
    final sv = SensorValue();

    fakeAsync((fa) {
      // Feed multiple readings quickly
      expect(sv.ready(10.0), false);
      expect(sv.ready(20.0), false);
      expect(sv.ready(30.0), false);

      // Not ready yet (needs 100ms)
      expect(sv.hasValue(), false);

      // Advance fake time
      fa.elapse(const Duration(milliseconds: 110));

      // Next call will compute the average of the buffered values
      final becameReady = sv.ready(40.0);
      expect(becameReady, true);
      expect(sv.hasValue(), true);

      // The computed average should match the (10,20,30) list -> 20
      expect(sv.getValue(), 20);
    });
  });
}

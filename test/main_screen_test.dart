import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:auto_brightness/screens/main_screen.dart';

void main() {
  testWidgets('MainScreen has a title and buttons', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(MaterialApp(home: const MainScreen()));

    // Verify that the main screen has the expected widgets.
    expect(find.text('Show Node List'), findsOneWidget);
    expect(find.byType(Slider), findsOneWidget);
    expect(find.widgetWithText(ElevatedButton, 'Auto'), findsOneWidget);
    expect(find.widgetWithText(ElevatedButton, 'Save'), findsOneWidget);
  });
}

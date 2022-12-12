import 'package:flutter/material.dart';

import 'package:foreground/foreground.dart';

void main() => runApp(const MyApp());

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

@pragma('vm:entry-point')
void foregroundCallback() {
  Foreground.setTaskHandler(
    onStarted: () {
      print('Foreground service started.\nThis method is called '
          'from a different Dart isolate');
    },
    onStopped: () {
      print('Foreground service stopped.\nThis method is called '
          'from a different Dart isolate');
    },
  );
}

class _MyAppState extends State<MyApp> {
  Future<bool> _startForeground() async {
    const channelConfig = ChannelConfiguration(
      id: 'channel_id',
      name: 'Foreground Notification',
      description: 'Foreground service is running',
      importance: Importance.normal,
    );

    const notificationConfig = NotificationConfiguration(
      channelConfiguration: channelConfig,
      title: 'Notification Title',
      text: 'Notification Text',
      visibility: NotificationVisibility.private,
    );

    const configuration = ForegroundConfiguration(
      notification: notificationConfig,
      runOnBoot: true,
      callback: foregroundCallback,
    );

    return await Foreground.startService(
      configuration: configuration,
    );
  }

  Future<bool> _stopForeground() async {
    return await Foreground.stopService();
  }

  @override
  Widget build(BuildContext context) {
    buttonBuilder(String text, {VoidCallback? onPressed}) {
      return ElevatedButton(
        onPressed: onPressed,
        child: Text(text),
      );
    }

    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Foreground Service Example'),
          centerTitle: true,
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              buttonBuilder('start', onPressed: _startForeground),
              buttonBuilder('stop', onPressed: _stopForeground),
            ],
          ),
        ),
      ),
    );
  }
}

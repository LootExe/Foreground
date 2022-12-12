import 'dart:ui';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

class Foreground {
  static const MethodChannel _methodChannel =
      MethodChannel('com.lootexe.foreground.method', JSONMethodCodec());

  /// Start the foreground service
  static Future<bool> startService({
    required ForegroundConfiguration configuration,
  }) async =>
      await _methodChannel.invokeMethod('startService', configuration.toJson());

  /// Stops a running service
  static Future<bool> stopService() async =>
      await _methodChannel.invokeMethod('stopService');

  /// Returns true if the service is currently active
  static Future<bool> get isRunning async =>
      await _methodChannel.invokeMethod('isRunning');

  /// Sets the task handler that gets executed on service start and stop
  /// Both methods run in an isolate different from the main app isolate
  static void setTaskHandler({
    VoidCallback? onStarted,
    VoidCallback? onStopped,
  }) {
    const channel = MethodChannel('com.lootexe.foreground.method.background');

    WidgetsFlutterBinding.ensureInitialized();

    channel.setMethodCallHandler((handler) async {
      switch (handler.method) {
        case 'onStarted':
          if (onStarted != null) {
            onStarted();
          }
          break;
        case 'onStopped':
          if (onStopped != null) {
            onStopped();
          }
          break;
      }
    });

    channel.invokeMethod('initialize');
  }
}

class ForegroundConfiguration {
  const ForegroundConfiguration({
    required this.notification,
    this.runOnBoot = true,
    this.callback,
  });

  final NotificationConfiguration notification;
  final bool runOnBoot;
  final VoidCallback? callback;

  Map toJson() {
    final Map json = {
      'notification': notification.toJson(),
      'runOnBoot': runOnBoot,
    };

    if (callback != null) {
      json['callback'] =
          PluginUtilities.getCallbackHandle(callback!)?.toRawHandle();
    }

    return json;
  }
}

class NotificationConfiguration {
  const NotificationConfiguration({
    this.channelConfiguration,
    this.iconConfiguration,
    required this.title,
    required this.text,
    this.visibility = NotificationVisibility.private,
  });

  final ChannelConfiguration? channelConfiguration;
  final IconConfiguration? iconConfiguration;
  final String title;
  final String text;
  final NotificationVisibility visibility;

  Map toJson() {
    final Map json = {};

    if (channelConfiguration != null) {
      json['channel'] = channelConfiguration!.toJson();
    }

    if (iconConfiguration != null) {
      json['icon'] = iconConfiguration!.toJson();
    }

    json['title'] = title;
    json['text'] = text;
    json['visibility'] = visibility.value;

    return json;
  }
}

class ChannelConfiguration {
  const ChannelConfiguration({
    this.id = '',
    this.name = '',
    this.description = '',
    this.importance = Importance.normal,
  });

  final String id;
  final String name;
  final String description;
  final Importance importance;

  Map toJson() => {
        'id': id,
        'name': name,
        'description': description,
        'importance': importance.value,
      };
}

class IconConfiguration {
  const IconConfiguration({
    this.name = '',
    this.resourceType = '',
  });

  final String name;
  final String resourceType;

  Map toJson() => {
        'name': name,
        'resourceType': resourceType,
      };
}

enum Importance {
  none(0),
  min(1),
  low(2),
  normal(3),
  high(4),
  max(5);

  const Importance(this.value);
  final int value;
}

enum NotificationVisibility {
  private(0),
  public(1),
  secret(-1);

  const NotificationVisibility(this.value);
  final int value;
}

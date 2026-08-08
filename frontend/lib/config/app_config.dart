class AppConfig {
  static const String apiBaseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://localhost:8080',
  );

  static const String fcmVapidKey = String.fromEnvironment(
    'FCM_VAPID_KEY',
    defaultValue: 'BKxw4J0Iowd9QmSr4nyFo7IPh2DJ3aNbRjkXoMacQMONC5SArelXafFYiJOe6Ce_GKl3bQEuRR1UgarOfH0kJ2w',
  );

  static const String appName = 'Jarvis Assistant';
}

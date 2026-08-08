class AppNotification {
  final String id;
  final String type;
  final String title;
  final String body;
  final bool isRead;
  final String scheduledFor;

  AppNotification({
    required this.id,
    required this.type,
    required this.title,
    required this.body,
    required this.isRead,
    required this.scheduledFor,
  });

  factory AppNotification.fromJson(Map<String, dynamic> json) {
    return AppNotification(
      id: json['id'] ?? '',
      type: json['type'] ?? '',
      title: json['title'] ?? '',
      body: json['body'] ?? '',
      isRead: json['isRead'] ?? false,
      scheduledFor: json['scheduledFor'] ?? '',
    );
  }
}

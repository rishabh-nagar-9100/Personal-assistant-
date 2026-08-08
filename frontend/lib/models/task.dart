class TaskItem {
  final String id;
  final String title;
  final String description;
  final String priority;
  final String status;
  final String dueDate;

  TaskItem({
    required this.id,
    required this.title,
    required this.description,
    required this.priority,
    required this.status,
    required this.dueDate,
  });

  factory TaskItem.fromJson(Map<String, dynamic> json) {
    return TaskItem(
      id: json['id'] ?? '',
      title: json['title'] ?? '',
      description: json['description'] ?? '',
      priority: json['priority'] ?? 'MEDIUM',
      status: json['status'] ?? 'PENDING',
      dueDate: json['dueDate'] ?? '',
    );
  }
}

class PriorityEvent {
  final String id;
  final String name;
  final String eventDate;
  final String type;

  PriorityEvent({
    required this.id,
    required this.name,
    required this.eventDate,
    required this.type,
  });

  factory PriorityEvent.fromJson(Map<String, dynamic> json) {
    return PriorityEvent(
      id: json['id'] ?? '',
      name: json['name'] ?? '',
      eventDate: json['eventDate'] ?? '',
      type: json['type'] ?? '',
    );
  }
}

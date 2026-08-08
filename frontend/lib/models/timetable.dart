class TimetableSlot {
  final String id;
  final String dayOfWeek;
  final String startTime;
  final String endTime;
  final String type;
  final String label;

  TimetableSlot({
    required this.id,
    required this.dayOfWeek,
    required this.startTime,
    required this.endTime,
    required this.type,
    required this.label,
  });

  factory TimetableSlot.fromJson(Map<String, dynamic> json) {
    return TimetableSlot(
      id: json['id'] ?? '',
      dayOfWeek: json['dayOfWeek'] ?? '',
      startTime: json['startTime'] ?? '',
      endTime: json['endTime'] ?? '',
      type: json['type'] ?? '',
      label: json['label'] ?? '',
    );
  }
}

class FreeSlot {
  final String dayOfWeek;
  final String startTime;
  final String endTime;
  final int durationMinutes;

  FreeSlot({
    required this.dayOfWeek,
    required this.startTime,
    required this.endTime,
    required this.durationMinutes,
  });

  factory FreeSlot.fromJson(Map<String, dynamic> json) {
    return FreeSlot(
      dayOfWeek: json['dayOfWeek'] ?? '',
      startTime: json['startTime'] ?? '',
      endTime: json['endTime'] ?? '',
      durationMinutes: json['durationMinutes'] ?? 0,
    );
  }
}

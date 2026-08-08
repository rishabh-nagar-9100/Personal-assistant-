class ScheduledSlotItem {
  final String startTime;
  final String endTime;
  final int durationMinutes;
  final String itemType;
  final String title;
  final String details;
  final String referenceId;

  ScheduledSlotItem({
    required this.startTime,
    required this.endTime,
    required this.durationMinutes,
    required this.itemType,
    required this.title,
    required this.details,
    required this.referenceId,
  });

  factory ScheduledSlotItem.fromJson(Map<String, dynamic> json) {
    return ScheduledSlotItem(
      startTime: json['startTime'] ?? '',
      endTime: json['endTime'] ?? '',
      durationMinutes: json['durationMinutes'] ?? 0,
      itemType: json['itemType'] ?? '',
      title: json['title'] ?? '',
      details: json['details'] ?? '',
      referenceId: json['referenceId'] ?? '',
    );
  }
}

class OverflowItem {
  final String itemType;
  final String title;
  final String reason;
  final String referenceId;
  final String suggestedCarryOverDate;

  OverflowItem({
    required this.itemType,
    required this.title,
    required this.reason,
    required this.referenceId,
    required this.suggestedCarryOverDate,
  });

  factory OverflowItem.fromJson(Map<String, dynamic> json) {
    return OverflowItem(
      itemType: json['itemType'] ?? '',
      title: json['title'] ?? '',
      reason: json['reason'] ?? '',
      referenceId: json['referenceId'] ?? '',
      suggestedCarryOverDate: json['suggestedCarryOverDate'] ?? '',
    );
  }
}

class DailySchedule {
  final String date;
  final String dayOfWeek;
  final List<ScheduledSlotItem> scheduledItems;
  final List<OverflowItem> overflowItems;
  final bool hasPriorityEvents;
  final String summaryText;

  DailySchedule({
    required this.date,
    required this.dayOfWeek,
    required this.scheduledItems,
    required this.overflowItems,
    required this.hasPriorityEvents,
    required this.summaryText,
  });

  factory DailySchedule.fromJson(Map<String, dynamic> json) {
    return DailySchedule(
      date: json['date'] ?? '',
      dayOfWeek: json['dayOfWeek'] ?? '',
      scheduledItems: (json['scheduledItems'] as List? ?? [])
          .map((i) => ScheduledSlotItem.fromJson(i))
          .toList(),
      overflowItems: (json['overflowItems'] as List? ?? [])
          .map((i) => OverflowItem.fromJson(i))
          .toList(),
      hasPriorityEvents: json['hasPriorityEvents'] ?? false,
      summaryText: json['summaryText'] ?? '',
    );
  }
}

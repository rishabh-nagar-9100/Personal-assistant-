class Topic {
  final String id;
  final String subjectId;
  final String subjectName;
  final String name;
  final String status;
  final String lastStudiedAt;
  final String nextRevisionAt;
  final double easeFactor;
  final int intervalDays;
  final int repetitionCount;

  Topic({
    required this.id,
    required this.subjectId,
    required this.subjectName,
    required this.name,
    required this.status,
    required this.lastStudiedAt,
    required this.nextRevisionAt,
    required this.easeFactor,
    required this.intervalDays,
    required this.repetitionCount,
  });

  factory Topic.fromJson(Map<String, dynamic> json) {
    return Topic(
      id: json['id'] ?? '',
      subjectId: json['subjectId'] ?? '',
      subjectName: json['subjectName'] ?? '',
      name: json['name'] ?? '',
      status: json['status'] ?? '',
      lastStudiedAt: json['lastStudiedAt'] ?? '',
      nextRevisionAt: json['nextRevisionAt'] ?? '',
      easeFactor: (json['easeFactor'] as num?)?.toDouble() ?? 2.5,
      intervalDays: json['intervalDays'] ?? 1,
      repetitionCount: json['repetitionCount'] ?? 0,
    );
  }
}

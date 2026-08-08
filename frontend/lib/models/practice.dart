class PracticeQuotaStatus {
  final String date;
  final int dsaTarget;
  final int dsaDone;
  final int dsaRemaining;
  final int sqlTarget;
  final int sqlDone;
  final int sqlRemaining;
  final int aptitudeTarget;
  final int aptitudeDone;
  final int aptitudeRemaining;

  PracticeQuotaStatus({
    required this.date,
    required this.dsaTarget,
    required this.dsaDone,
    required this.dsaRemaining,
    required this.sqlTarget,
    required this.sqlDone,
    required this.sqlRemaining,
    required this.aptitudeTarget,
    required this.aptitudeDone,
    required this.aptitudeRemaining,
  });

  factory PracticeQuotaStatus.fromJson(Map<String, dynamic> json) {
    return PracticeQuotaStatus(
      date: json['date'] ?? '',
      dsaTarget: json['dsaTarget'] ?? 5,
      dsaDone: json['dsaDone'] ?? 0,
      dsaRemaining: json['dsaRemaining'] ?? 5,
      sqlTarget: json['sqlTarget'] ?? 5,
      sqlDone: json['sqlDone'] ?? 0,
      sqlRemaining: json['sqlRemaining'] ?? 5,
      aptitudeTarget: json['aptitudeTarget'] ?? 5,
      aptitudeDone: json['aptitudeDone'] ?? 0,
      aptitudeRemaining: json['aptitudeRemaining'] ?? 5,
    );
  }
}

class PracticeQuestion {
  final String id;
  final String categoryType;
  final String subCategory;
  final String title;
  final String difficulty;
  final String status;
  final double easeFactor;

  PracticeQuestion({
    required this.id,
    required this.categoryType,
    required this.subCategory,
    required this.title,
    required this.difficulty,
    required this.status,
    required this.easeFactor,
  });

  factory PracticeQuestion.fromJson(Map<String, dynamic> json) {
    return PracticeQuestion(
      id: json['id'] ?? '',
      categoryType: json['categoryType'] ?? '',
      subCategory: json['subCategory'] ?? '',
      title: json['title'] ?? '',
      difficulty: json['difficulty'] ?? 'MEDIUM',
      status: json['status'] ?? 'NOT_STARTED',
      easeFactor: (json['easeFactor'] as num?)?.toDouble() ?? 2.5,
    );
  }
}

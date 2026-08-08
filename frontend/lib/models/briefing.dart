class DailyBriefing {
  final String date;
  final String briefingText;
  final bool isCached;
  final String generatedAt;

  DailyBriefing({
    required this.date,
    required this.briefingText,
    required this.isCached,
    required this.generatedAt,
  });

  factory DailyBriefing.fromJson(Map<String, dynamic> json) {
    return DailyBriefing(
      date: json['date'] ?? '',
      briefingText: json['briefingText'] ?? '',
      isCached: json['isCached'] ?? false,
      generatedAt: json['generatedAt'] ?? '',
    );
  }
}

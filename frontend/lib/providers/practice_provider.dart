import 'package:flutter/material.dart';
import '../models/practice.dart';
import '../services/api_service.dart';

class PracticeQuotaProvider extends ChangeNotifier {
  PracticeQuotaStatus? _quotaStatus;
  List<PracticeQuestion> _questions = [];
  bool _isLoading = false;

  PracticeQuotaStatus? get quotaStatus => _quotaStatus;
  List<PracticeQuestion> get questions => _questions;
  bool get isLoading => _isLoading;

  Future<void> fetchTodayQuota() async {
    _isLoading = true;
    notifyListeners();

    try {
      final json = await ApiService.get('/practice/today-quota');
      _quotaStatus = PracticeQuotaStatus.fromJson(json);
    } catch (_) {} finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> fetchPracticeQuestions(String categoryType) async {
    try {
      final list = await ApiService.get('/practice/questions?categoryType=$categoryType') as List;
      _questions = list.map((i) => PracticeQuestion.fromJson(i)).toList();
      notifyListeners();
    } catch (_) {}
  }

  Future<void> reviewPracticeQuestion(String id, String quality) async {
    try {
      await ApiService.post('/practice/questions/$id/review', body: {'quality': quality});
      await fetchTodayQuota();
    } catch (_) {}
  }
}

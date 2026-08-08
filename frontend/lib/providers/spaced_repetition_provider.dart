import 'package:flutter/material.dart';
import '../models/topic.dart';
import '../services/api_service.dart';

class SpacedRepetitionProvider extends ChangeNotifier {
  List<Topic> _dueTopics = [];
  bool _isLoading = false;

  List<Topic> get dueTopics => _dueTopics;
  bool get isLoading => _isLoading;

  Future<void> fetchDueTopics() async {
    _isLoading = true;
    notifyListeners();

    try {
      final list = await ApiService.get('/topics/due-for-revision') as List;
      _dueTopics = list.map((i) => Topic.fromJson(i)).toList();
    } catch (_) {} finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> reviewTopic(String topicId, String quality) async {
    try {
      await ApiService.post('/topics/$topicId/review', body: {'quality': quality});
      await fetchDueTopics();
    } catch (_) {}
  }
}

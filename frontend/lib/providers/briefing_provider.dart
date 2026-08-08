import 'package:flutter/material.dart';
import '../models/briefing.dart';
import '../services/api_service.dart';

class BriefingProvider extends ChangeNotifier {
  DailyBriefing? _briefing;
  bool _isLoading = false;
  String? _error;

  DailyBriefing? get briefing => _briefing;
  bool get isLoading => _isLoading;
  String? get error => _error;

  Future<void> fetchTodayBriefing() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final json = await ApiService.get('/briefing/today');
      _briefing = DailyBriefing.fromJson(json);
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> regenerateBriefing() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final json = await ApiService.post('/briefing/today/regenerate');
      _briefing = DailyBriefing.fromJson(json);
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }
}

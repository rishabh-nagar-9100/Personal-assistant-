import 'package:flutter/material.dart';
import '../models/schedule.dart';
import '../services/api_service.dart';

class ScheduleProvider extends ChangeNotifier {
  DailySchedule? _schedule;
  bool _isLoading = false;
  String? _error;

  DailySchedule? get schedule => _schedule;
  bool get isLoading => _isLoading;
  String? get error => _error;

  Future<void> fetchTodaySchedule() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final json = await ApiService.get('/schedule/today');
      _schedule = DailySchedule.fromJson(json);
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }
}

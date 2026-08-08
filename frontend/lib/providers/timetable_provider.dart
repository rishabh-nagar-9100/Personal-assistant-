import 'package:flutter/material.dart';
import '../models/timetable.dart';
import '../services/api_service.dart';

class TimetableProvider extends ChangeNotifier {
  List<TimetableSlot> _slots = [];
  List<FreeSlot> _freeSlots = [];
  bool _isLoading = false;

  List<TimetableSlot> get slots => _slots;
  List<FreeSlot> get freeSlots => _freeSlots;
  bool get isLoading => _isLoading;

  Future<void> fetchTimetable() async {
    _isLoading = true;
    notifyListeners();

    try {
      final list = await ApiService.get('/timetable') as List;
      _slots = list.map((i) => TimetableSlot.fromJson(i)).toList();
    } catch (_) {} finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> fetchFreeSlots(String day) async {
    try {
      final list = await ApiService.get('/timetable/free-slots?day=$day') as List;
      _freeSlots = list.map((i) => FreeSlot.fromJson(i)).toList();
      notifyListeners();
    } catch (_) {}
  }
}

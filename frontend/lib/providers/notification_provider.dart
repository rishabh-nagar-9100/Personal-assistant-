import 'package:flutter/material.dart';
import '../models/notification.dart';
import '../services/api_service.dart';

class NotificationProvider extends ChangeNotifier {
  List<AppNotification> _pending = [];
  bool _isLoading = false;

  List<AppNotification> get pending => _pending;
  int get unreadCount => _pending.where((n) => !n.isRead).length;
  bool get isLoading => _isLoading;

  Future<void> fetchPendingNotifications() async {
    _isLoading = true;
    notifyListeners();

    try {
      final list = await ApiService.get('/notifications/pending') as List;
      _pending = list.map((i) => AppNotification.fromJson(i)).toList();
    } catch (_) {} finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> markAsRead(String id) async {
    try {
      await ApiService.post('/notifications/$id/read');
      await fetchPendingNotifications();
    } catch (_) {}
  }
}

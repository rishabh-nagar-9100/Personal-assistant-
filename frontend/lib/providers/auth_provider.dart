import 'package:flutter/material.dart';
import '../services/storage_service.dart';

class AuthProvider extends ChangeNotifier {
  String? _token;
  bool _isAuthenticated = false;

  String? get token => _token;
  bool get isAuthenticated => _isAuthenticated;

  Future<void> loadToken() async {
    _token = await StorageService.getToken();
    _isAuthenticated = _token != null && _token!.isNotEmpty;
    notifyListeners();
  }

  Future<void> login(String token) async {
    await StorageService.saveToken(token);
    _token = token;
    _isAuthenticated = true;
    notifyListeners();
  }

  Future<void> logout() async {
    await StorageService.clearToken();
    _token = null;
    _isAuthenticated = false;
    notifyListeners();
  }
}

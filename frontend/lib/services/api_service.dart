import 'dart:convert';
import 'package:http/http.dart' as http;
import '../config/app_config.dart';
import 'storage_service.dart';

class ApiService {
  static Future<Map<String, String>> _getHeaders() async {
    final token = await StorageService.getToken();
    return {
      'Content-Type': 'application/json',
      if (token != null && token.isNotEmpty) 'Authorization': 'Bearer $token',
    };
  }

  static Future<dynamic> get(String endpoint) async {
    final headers = await _getHeaders();
    final response = await http.get(
      Uri.parse('${AppConfig.apiBaseUrl}$endpoint'),
      headers: headers,
    );
    return _handleResponse(response);
  }

  static Future<dynamic> post(String endpoint, {Map<String, dynamic>? body}) async {
    final headers = await _getHeaders();
    final response = await http.post(
      Uri.parse('${AppConfig.apiBaseUrl}$endpoint'),
      headers: headers,
      body: body != null ? jsonEncode(body) : null,
    );
    return _handleResponse(response);
  }

  static Future<dynamic> put(String endpoint, {Map<String, dynamic>? body}) async {
    final headers = await _getHeaders();
    final response = await http.put(
      Uri.parse('${AppConfig.apiBaseUrl}$endpoint'),
      headers: headers,
      body: body != null ? jsonEncode(body) : null,
    );
    return _handleResponse(response);
  }

  static Future<dynamic> delete(String endpoint) async {
    final headers = await _getHeaders();
    final response = await http.delete(
      Uri.parse('${AppConfig.apiBaseUrl}$endpoint'),
      headers: headers,
    );
    return _handleResponse(response);
  }

  static dynamic _handleResponse(http.Response response) {
    if (response.statusCode >= 200 && response.statusCode < 300) {
      if (response.body.isEmpty) return null;
      return jsonDecode(response.body);
    } else {
      throw Exception('API Error [${response.statusCode}]: ${response.body}');
    }
  }
}

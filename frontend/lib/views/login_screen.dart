import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _tokenController = TextEditingController();

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(Icons.auto_awesome, size: 72, color: theme.colorScheme.primary),
              const SizedBox(height: 16),
              Text(
                'JARVIS',
                style: theme.textTheme.headlineLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                  letterSpacing: 2.0,
                ),
              ),
              Text(
                'AI Personal Study & Executive Assistant',
                style: theme.textTheme.bodyMedium?.copyWith(color: theme.colorScheme.secondary),
              ),
              const SizedBox(height: 36),
              TextField(
                controller: _tokenController,
                decoration: InputDecoration(
                  labelText: 'Supabase JWT Bearer Token',
                  hintText: 'Paste JWT token or demo token',
                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                  prefixIcon: const Icon(Icons.key),
                ),
                maxLines: 3,
              ),
              const SizedBox(height: 20),
              SizedBox(
                width: double.infinity,
                height: 48,
                child: ElevatedButton.icon(
                  icon: const Icon(Icons.login),
                  label: const Text('Connect to Jarvis'),
                  style: ElevatedButton.styleFrom(
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                  ),
                  onPressed: () {
                    final token = _tokenController.text.trim();
                    if (token.isNotEmpty) {
                      context.read<AuthProvider>().login(token);
                    }
                  },
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

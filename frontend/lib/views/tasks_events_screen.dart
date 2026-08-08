import 'package:flutter/material.dart';

class TasksEventsScreen extends StatelessWidget {
  const TasksEventsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('Tasks & Priority Events', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
            const SizedBox(height: 16),
            Card(
              color: Colors.amber.shade50,
              child: const Padding(
                padding: EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Icon(Icons.event, color: Colors.amber),
                        SizedBox(width: 8),
                        Text('Upcoming Priority Exam/Test Events', style: TextStyle(fontWeight: FontWeight.bold)),
                      ],
                    ),
                    SizedBox(height: 8),
                    Text('Priority events scheduled within 7 days automatically trigger priority prep blocks in the daily schedule engine.'),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),
            const Text('Pending Tasks', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            const Expanded(
              child: Center(
                child: Text('No pending tasks. Connect to Spring Boot backend API.'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

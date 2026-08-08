import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/spaced_repetition_provider.dart';

class SpacedRepetitionScreen extends StatefulWidget {
  const SpacedRepetitionScreen({super.key});

  @override
  State<SpacedRepetitionScreen> createState() => _SpacedRepetitionScreenState();
}

class _SpacedRepetitionScreenState extends State<SpacedRepetitionScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<SpacedRepetitionProvider>().fetchDueTopics();
    });
  }

  @override
  Widget build(BuildContext context) {
    final srProv = context.watch<SpacedRepetitionProvider>();

    return Scaffold(
      body: RefreshIndicator(
        onRefresh: () => srProv.fetchDueTopics(),
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('SM-2 Spaced Repetition Queue', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
              const Text('Topics due for active recall revision today', style: TextStyle(color: Colors.grey)),
              const SizedBox(height: 16),
              if (srProv.isLoading)
                const Center(child: CircularProgressIndicator())
              else if (srProv.dueTopics.isEmpty)
                const Expanded(
                  child: Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.check_circle_outline, size: 64, color: Colors.green),
                        SizedBox(height: 12),
                        Text('Queue Empty! All due topics revised.', style: TextStyle(fontSize: 16)),
                      ],
                    ),
                  ),
                )
              else
                Expanded(
                  child: ListView.builder(
                    itemCount: srProv.dueTopics.length,
                    itemBuilder: (ctx, i) {
                      final topic = srProv.dueTopics[i];
                      return Card(
                        margin: const EdgeInsets.only(bottom: 12),
                        child: Padding(
                          padding: const EdgeInsets.all(16.0),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(topic.name, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                              Text('Subject: ${topic.subjectName} • Ease Factor: ${topic.easeFactor.toStringAsFixed(1)} • Interval: ${topic.intervalDays}d'),
                              const SizedBox(height: 12),
                              Row(
                                mainAxisAlignment: MainAxisAlignment.end,
                                children: [
                                  OutlinedButton.icon(
                                    icon: const Icon(Icons.warning, color: Colors.orange, size: 16),
                                    label: const Text('Struggled'),
                                    onPressed: () => srProv.reviewTopic(topic.id, 'STRUGGLED'),
                                  ),
                                  const SizedBox(width: 8),
                                  ElevatedButton.icon(
                                    icon: const Icon(Icons.check, size: 16),
                                    label: const Text('Good'),
                                    onPressed: () => srProv.reviewTopic(topic.id, 'GOOD'),
                                  ),
                                ],
                              ),
                            ],
                          ),
                        ),
                      );
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

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/practice_provider.dart';

class PracticeScreen extends StatefulWidget {
  const PracticeScreen({super.key});

  @override
  State<PracticeScreen> createState() => _PracticeScreenState();
}

class _PracticeScreenState extends State<PracticeScreen> {
  String _selectedCategory = 'SQL';

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<PracticeQuotaProvider>().fetchTodayQuota();
      context.read<PracticeQuotaProvider>().fetchPracticeQuestions(_selectedCategory);
    });
  }

  @override
  Widget build(BuildContext context) {
    final practiceProv = context.watch<PracticeQuotaProvider>();

    return Scaffold(
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('Daily Practice & Quota', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
                ElevatedButton.icon(
                  icon: const Icon(Icons.upload_file, size: 16),
                  label: const Text('Import DSA Excel'),
                  onPressed: () {
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Excel Import trigger ready. Send POST /dsa/import with .xlsx file.')),
                    );
                  },
                ),
              ],
            ),
            const SizedBox(height: 16),
            if (practiceProv.quotaStatus != null)
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Column(
                    children: [
                      _ProgressTile('DSA Quota', practiceProv.quotaStatus!.dsaDone, practiceProv.quotaStatus!.dsaTarget, Colors.blue),
                      const SizedBox(height: 8),
                      _ProgressTile('SQL Quota', practiceProv.quotaStatus!.sqlDone, practiceProv.quotaStatus!.sqlTarget, Colors.green),
                      const SizedBox(height: 8),
                      _ProgressTile('Aptitude Quota', practiceProv.quotaStatus!.aptitudeDone, practiceProv.quotaStatus!.aptitudeTarget, Colors.orange),
                    ],
                  ),
                ),
              ),
            const SizedBox(height: 16),
            Row(
              children: [
                ChoiceChip(
                  label: const Text('SQL Questions'),
                  selected: _selectedCategory == 'SQL',
                  onSelected: (selected) {
                    if (selected) {
                      setState(() => _selectedCategory = 'SQL');
                      practiceProv.fetchPracticeQuestions('SQL');
                    }
                  },
                ),
                const SizedBox(width: 8),
                ChoiceChip(
                  label: const Text('Aptitude Questions'),
                  selected: _selectedCategory == 'APTITUDE',
                  onSelected: (selected) {
                    if (selected) {
                      setState(() => _selectedCategory = 'APTITUDE');
                      practiceProv.fetchPracticeQuestions('APTITUDE');
                    }
                  },
                ),
              ],
            ),
            const SizedBox(height: 12),
            Expanded(
              child: ListView.builder(
                itemCount: practiceProv.questions.length,
                itemBuilder: (ctx, i) {
                  final q = practiceProv.questions[i];
                  return Card(
                    child: ListTile(
                      title: Text(q.title, style: const TextStyle(fontWeight: FontWeight.bold)),
                      subtitle: Text('${q.subCategory} • Difficulty: ${q.difficulty} • Status: ${q.status}'),
                      trailing: ElevatedButton(
                        child: const Text('Solve'),
                        onPressed: () => practiceProv.reviewPracticeQuestion(q.id, 'GOOD'),
                      ),
                    ),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _ProgressTile(String title, int done, int target, Color color) {
    final val = target > 0 ? (done / target).clamp(0.0, 1.0) : 0.0;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(title, style: const TextStyle(fontWeight: FontWeight.bold)),
            Text('$done / $target completed'),
          ],
        ),
        const SizedBox(height: 4),
        LinearProgressIndicator(value: val, color: color, backgroundColor: color.withOpacity(0.2), minHeight: 6),
      ],
    );
  }
}

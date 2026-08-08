import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/schedule_provider.dart';

class ScheduleScreen extends StatelessWidget {
  const ScheduleScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final scheduleProv = context.watch<ScheduleProvider>();
    final theme = Theme.of(context);

    return Scaffold(
      body: RefreshIndicator(
        onRefresh: () => scheduleProv.fetchTodaySchedule(),
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(16.0),
          physics: const AlwaysScrollableScrollPhysics(),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Deterministic Daily Schedule', style: theme.textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold)),
              if (scheduleProv.schedule != null)
                Text('${scheduleProv.schedule!.date} (${scheduleProv.schedule!.dayOfWeek})', style: TextStyle(color: Colors.grey.shade600)),
              const SizedBox(height: 16),

              if (scheduleProv.schedule?.hasPriorityEvents == true)
                Card(
                  color: Colors.amber.shade100,
                  child: const Padding(
                    padding: EdgeInsets.all(12.0),
                    child: Row(
                      children: [
                        Icon(Icons.warning_amber, color: Colors.amber),
                        SizedBox(width: 8),
                        Expanded(child: Text('Priority Exam/Test Prep boosted to top free slots today!')),
                      ],
                    ),
                  ),
                ),

              const SizedBox(height: 16),
              Text('Time-Blocked Items', style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
              const SizedBox(height: 8),

              if (scheduleProv.isLoading)
                const Center(child: CircularProgressIndicator())
              else if (scheduleProv.schedule?.scheduledItems.isEmpty ?? true)
                const Padding(
                  padding: EdgeInsets.symmetric(vertical: 24),
                  child: Center(child: Text('No time-blocked study slots available today.')),
                )
              else
                ListView.builder(
                  shrinkWrap: true,
                  physics: const NeverScrollableScrollPhysics(),
                  itemCount: scheduleProv.schedule!.scheduledItems.length,
                  itemBuilder: (ctx, i) {
                    final item = scheduleProv.schedule!.scheduledItems[i];
                    return Card(
                      margin: const EdgeInsets.only(bottom: 8),
                      child: ListTile(
                        leading: Chip(label: Text('${item.startTime}\n${item.endTime}', style: const TextStyle(fontSize: 10))),
                        title: Text(item.title, style: const TextStyle(fontWeight: FontWeight.bold)),
                        subtitle: Text('${item.itemType} • ${item.details}'),
                      ),
                    );
                  },
                ),

              const SizedBox(height: 24),
              Text('Carry-over Overflow Items', style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold, color: Colors.red)),
              const SizedBox(height: 8),

              if (scheduleProv.schedule?.overflowItems.isEmpty ?? true)
                const Padding(
                  padding: EdgeInsets.symmetric(vertical: 12),
                  child: Text('Zero overflow items! All tasks fit into today\'s free slots.'),
                )
              else
                ListView.builder(
                  shrinkWrap: true,
                  physics: const NeverScrollableScrollPhysics(),
                  itemCount: scheduleProv.schedule!.overflowItems.length,
                  itemBuilder: (ctx, i) {
                    final item = scheduleProv.schedule!.overflowItems[i];
                    return Card(
                      color: Colors.red.shade50,
                      margin: const EdgeInsets.only(bottom: 8),
                      child: ListTile(
                        leading: const Icon(Icons.next_plan, color: Colors.red),
                        title: Text(item.title, style: const TextStyle(fontWeight: FontWeight.bold)),
                        subtitle: Text('${item.reason} • Carry-over date: ${item.suggestedCarryOverDate}'),
                      ),
                    );
                  },
                ),
            ],
          ),
        ),
      ),
    );
  }
}

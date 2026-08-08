import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/briefing_provider.dart';
import '../providers/schedule_provider.dart';
import '../providers/practice_provider.dart';
import '../providers/notification_provider.dart';
import '../providers/auth_provider.dart';
import 'schedule_screen.dart';
import 'timetable_screen.dart';
import 'spaced_repetition_screen.dart';
import 'practice_screen.dart';
import 'tasks_events_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _currentIndex = 0;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<BriefingProvider>().fetchTodayBriefing();
      context.read<ScheduleProvider>().fetchTodaySchedule();
      context.read<PracticeQuotaProvider>().fetchTodayQuota();
      context.read<NotificationProvider>().fetchPendingNotifications();
    });
  }

  @override
  Widget build(BuildContext context) {
    final screens = [
      const DashboardTab(),
      const ScheduleScreen(),
      const TimetableScreen(),
      const SpacedRepetitionScreen(),
      const PracticeScreen(),
      const TasksEventsScreen(),
    ];

    final unreadNotifs = context.watch<NotificationProvider>().unreadCount;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Jarvis AI'),
        actions: [
          Stack(
            alignment: Alignment.center,
            children: [
              IconButton(
                icon: const Icon(Icons.notifications_outlined),
                onPressed: () => _showNotificationSheet(context),
              ),
              if (unreadNotifs > 0)
                Positioned(
                  right: 8,
                  top: 8,
                  child: Container(
                    padding: const EdgeInsets.all(4),
                    decoration: const BoxDecoration(
                      color: Colors.red,
                      shape: BoxShape.circle,
                    ),
                    child: Text(
                      '$unreadNotifs',
                      style: const TextStyle(color: Colors.white, fontSize: 10, fontWeight: FontWeight.bold),
                    ),
                  ),
                ),
            ],
          ),
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () => context.read<AuthProvider>().logout(),
          ),
        ],
      ),
      body: screens[_currentIndex],
      bottomNavigationBar: NavigationBar(
        selectedIndex: _currentIndex,
        onDestinationSelected: (idx) => setState(() => _currentIndex = idx),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.dashboard_outlined), selectedIcon: Icon(Icons.dashboard), label: 'Briefing'),
          NavigationDestination(icon: Icon(Icons.calendar_today_outlined), selectedIcon: Icon(Icons.calendar_today), label: 'Schedule'),
          NavigationDestination(icon: Icon(Icons.table_chart_outlined), selectedIcon: Icon(Icons.table_chart), label: 'Timetable'),
          NavigationDestination(icon: Icon(Icons.psychology_outlined), selectedIcon: Icon(Icons.psychology), label: 'Review'),
          NavigationDestination(icon: Icon(Icons.code_outlined), selectedIcon: Icon(Icons.code), label: 'Practice'),
          NavigationDestination(icon: Icon(Icons.task_alt), label: 'Tasks'),
        ],
      ),
    );
  }

  void _showNotificationSheet(BuildContext context) {
    final notifProvider = context.read<NotificationProvider>();
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(20))),
      builder: (ctx) {
        return Container(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              const Text('Pending Push Notifications', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
              const SizedBox(height: 12),
              if (notifProvider.pending.isEmpty)
                const Padding(
                  padding: EdgeInsets.symmetric(vertical: 24),
                  child: Center(child: Text('No unread notifications.')),
                )
              else
                Flexible(
                  child: ListView.builder(
                    shrinkWrap: true,
                    itemCount: notifProvider.pending.length,
                    itemBuilder: (context, i) {
                      final n = notifProvider.pending[i];
                      return ListTile(
                        leading: const Icon(Icons.circle, color: Colors.blue, size: 12),
                        title: Text(n.title, style: const TextStyle(fontWeight: FontWeight.bold)),
                        subtitle: Text(n.body),
                        trailing: IconButton(
                          icon: const Icon(Icons.check),
                          onPressed: () => notifProvider.markAsRead(n.id),
                        ),
                      );
                    },
                  ),
                ),
            ],
          ),
        );
      },
    );
  }
}

class DashboardTab extends StatelessWidget {
  const DashboardTab({super.key});

  @override
  Widget build(BuildContext context) {
    final briefingProv = context.watch<BriefingProvider>();
    final scheduleProv = context.watch<ScheduleProvider>();
    final practiceProv = context.watch<PracticeQuotaProvider>();
    final theme = Theme.of(context);

    return RefreshIndicator(
      onRefresh: () async {
        await briefingProv.fetchTodayBriefing();
        await scheduleProv.fetchTodaySchedule();
        await practiceProv.fetchTodayQuota();
      },
      child: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        physics: const AlwaysScrollableScrollPhysics(),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Daily Briefing Card
            Card(
              elevation: 4,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Row(
                          children: [
                            Icon(Icons.auto_awesome, color: theme.colorScheme.primary),
                            const SizedBox(width: 8),
                            Text('Daily Executive Briefing', style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
                          ],
                        ),
                        if (briefingProv.briefing?.isCached == true)
                          Chip(label: const Text('Cached', style: TextStyle(fontSize: 10)), backgroundColor: Colors.grey.shade200),
                      ],
                    ),
                    const Divider(height: 24),
                    if (briefingProv.isLoading)
                      const Center(child: Padding(padding: EdgeInsets.all(24), child: CircularProgressIndicator()))
                    else if (briefingProv.briefing != null)
                      Text(briefingProv.briefing!.briefingText, style: theme.textTheme.bodyMedium?.copyWith(height: 1.5))
                    else
                      const Text('Tap regenerate to generate today\'s AI briefing.'),
                    const SizedBox(height: 12),
                    Align(
                      alignment: Alignment.centerRight,
                      child: TextButton.icon(
                        icon: const Icon(Icons.refresh, size: 16),
                        label: const Text('Regenerate AI Briefing'),
                        onPressed: () => briefingProv.regenerateBriefing(),
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 20),

            // Quotas Quick Overview
            Text('Today\'s Practice Quotas', style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
            const SizedBox(height: 12),
            if (practiceProv.quotaStatus != null)
              Row(
                children: [
                  _QuotaMiniCard('DSA', practiceProv.quotaStatus!.dsaDone, practiceProv.quotaStatus!.dsaTarget, Colors.blue),
                  const SizedBox(width: 8),
                  _QuotaMiniCard('SQL', practiceProv.quotaStatus!.sqlDone, practiceProv.quotaStatus!.sqlTarget, Colors.green),
                  const SizedBox(width: 8),
                  _QuotaMiniCard('Aptitude', practiceProv.quotaStatus!.aptitudeDone, practiceProv.quotaStatus!.aptitudeTarget, Colors.orange),
                ],
              ),
            const SizedBox(height: 24),

            // Today's Time-Blocked Schedule Snippet
            Text('Today\'s Schedule Timeline', style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
            const SizedBox(height: 12),
            if (scheduleProv.isLoading)
              const CircularProgressIndicator()
            else if (scheduleProv.schedule != null && scheduleProv.schedule!.scheduledItems.isNotEmpty)
              ListView.builder(
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                itemCount: scheduleProv.schedule!.scheduledItems.length,
                itemBuilder: (ctx, i) {
                  final item = scheduleProv.schedule!.scheduledItems[i];
                  return Card(
                    margin: const EdgeInsets.only(bottom: 8),
                    child: ListTile(
                      leading: CircleAvatar(child: Text('${item.durationMinutes}m', style: const TextStyle(fontSize: 12))),
                      title: Text(item.title, style: const TextStyle(fontWeight: FontWeight.bold)),
                      subtitle: Text('${item.startTime} - ${item.endTime} • ${item.details}'),
                    ),
                  );
                },
              )
            else
              const Text('No study items scheduled for today yet.'),
          ],
        ),
      ),
    );
  }

  Widget _QuotaMiniCard(String label, int done, int target, Color color) {
    final progress = target > 0 ? (done / target).clamp(0.0, 1.0) : 0.0;
    return Expanded(
      child: Card(
        child: Padding(
          padding: const EdgeInsets.all(12.0),
          child: Column(
            children: [
              Text(label, style: const TextStyle(fontWeight: FontWeight.bold)),
              const SizedBox(height: 8),
              CircularProgressIndicator(value: progress, color: color, backgroundColor: color.withOpacity(0.2)),
              const SizedBox(height: 8),
              Text('$done / $target', style: const TextStyle(fontSize: 12)),
            ],
          ),
        ),
      ),
    );
  }
}

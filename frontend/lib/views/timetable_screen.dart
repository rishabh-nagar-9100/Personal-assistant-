import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/timetable_provider.dart';

class TimetableScreen extends StatefulWidget {
  const TimetableScreen({super.key});

  @override
  State<TimetableScreen> createState() => _TimetableScreenState();
}

class _TimetableScreenState extends State<TimetableScreen> {
  String _selectedDay = 'MONDAY';

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<TimetableProvider>().fetchTimetable();
      context.read<TimetableProvider>().fetchFreeSlots(_selectedDay);
    });
  }

  @override
  Widget build(BuildContext context) {
    final timetableProv = context.watch<TimetableProvider>();
    final days = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

    return Scaffold(
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('Weekly Timetable & Free Slots', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
            const SizedBox(height: 12),
            SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: Row(
                children: days.map((day) {
                  final isSelected = day == _selectedDay;
                  return Padding(
                    padding: const EdgeInsets.only(right: 8.0),
                    child: ChoiceChip(
                      label: Text(day.substring(0, 3)),
                      selected: isSelected,
                      onSelected: (selected) {
                        if (selected) {
                          setState(() => _selectedDay = day);
                          timetableProv.fetchFreeSlots(day);
                        }
                      },
                    ),
                  );
                }).toList(),
              ),
            ),
            const SizedBox(height: 16),
            const Text('Calculated Free Time Windows (08:00 - 22:00)', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            if (timetableProv.freeSlots.isEmpty)
              const Text('No free slots computed for this day.')
            else
              Expanded(
                child: ListView.builder(
                  itemCount: timetableProv.freeSlots.length,
                  itemBuilder: (ctx, i) {
                    final free = timetableProv.freeSlots[i];
                    return Card(
                      color: Colors.green.shade50,
                      child: ListTile(
                        leading: const Icon(Icons.access_time, color: Colors.green),
                        title: Text('${free.startTime} - ${free.endTime}', style: const TextStyle(fontWeight: FontWeight.bold)),
                        subtitle: Text('Available Study Duration: ${free.durationMinutes} minutes'),
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
}

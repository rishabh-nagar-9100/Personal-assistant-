/* ═══════════════════════════════════════════
   JARVIS AI Dashboard & SPA — app.js
   ═══════════════════════════════════════════ */

const DEFAULT_TEST_TOKEN = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMTExMTExMS0xMTExLTExMTEtMTExMS0xMTExMTExMTExMTEiLCJlbWFpbCI6InJpc2hhYmhAZXhhbXBsZS5jb20iLCJyb2xlIjoiYXV0aGVudGljYXRlZCIsImlhdCI6MTc4NjE2MzE5MiwiZXhwIjoxODE3Njk5MTkyfQ.9QqZQAZHpR_B7LY7bdkIkgIp7iXDuXBiYQo8RSUaUHg';
const API_BASE = 'http://localhost:8080';
const API_BASE_URL = API_BASE;

let authToken = localStorage.getItem('jarvis_jwt') || DEFAULT_TEST_TOKEN;
let chatHistory = [];
let currentPracticeCategory = 'DSA';
let currentTaskFilter = 'ALL';

function getJwtToken() {
  return authToken || localStorage.getItem('jarvis_jwt') || DEFAULT_TEST_TOKEN;
}

function getHeaders() {
  const headers = { 'Content-Type': 'application/json' };
  const token = getJwtToken();
  if (token) headers['Authorization'] = `Bearer ${token}`;
  return headers;
}

async function apiFetch(endpoint, options = {}) {
  options.headers = { ...getHeaders(), ...options.headers };
  try {
    const res = await fetch(`${API_BASE}${endpoint}`, options);
    if (!res.ok) {
      const errText = await res.text();
      throw new Error(`[${res.status}] ${errText || res.statusText}`);
    }
    if (res.status === 204 || res.headers.get('content-length') === '0') return null;
    return await res.json();
  } catch (err) {
    console.warn(`API Error on ${endpoint}:`, err.message);
    throw err;
  }
}

function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

/* ─── Greeting & Date ─── */
function updateGreeting() {
  const now = new Date();
  const hour = now.getHours();
  let greeting = 'Good evening';
  if (hour < 12) greeting = 'Good morning';
  else if (hour < 17) greeting = 'Good afternoon';
  
  document.getElementById('greetingText').textContent = `${greeting}, Rishabh! 👋`;
  
  const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
  document.getElementById('greetingDate').textContent = now.toLocaleDateString('en-US', options);
}

/* ─── Sidebar & Section Switching ─── */
function toggleSidebar() {
  document.body.classList.toggle('sidebar-open');
}

function switchSection(sectionId, element) {
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
  if (element) element.classList.add('active');
  
  // Hide all sections
  document.querySelectorAll('.section-view').forEach(sec => sec.classList.add('hidden'));
  document.querySelectorAll('.section-view').forEach(sec => sec.classList.remove('active'));
  
  // Show target section
  const targetView = document.getElementById(`view-${sectionId}`);
  if (targetView) {
    targetView.classList.remove('hidden');
    targetView.classList.add('active');
  }

  // Load section specific data
  switch (sectionId) {
    case 'dashboard':
      loadDashboardView();
      break;
    case 'schedule':
      loadScheduleView();
      break;
    case 'studyplan':
      loadStudyPlanView();
      break;
    case 'topics':
      loadTopicsView();
      break;
    case 'tasks':
      loadTasksView();
      break;
    case 'analytics':
      loadAnalyticsView();
      break;
  }
}

function toggleFocusMode() {
  document.getElementById('focusModeToggle').classList.toggle('active');
}

/* ─── Modal Handlers ─── */
function openModal(modalId) {
  document.getElementById('modalBackdrop').classList.add('active');
  document.querySelectorAll('.modal-content').forEach(m => m.style.display = 'none');
  const target = document.getElementById(modalId);
  if (target) target.style.display = 'block';

  // If topic modal, load subjects into dropdown
  if (modalId === 'topicModal') {
    populateSubjectDropdown();
  }
  // If quota modal, load current quota config
  if (modalId === 'quotaModal') {
    populateQuotaModal();
  }
}

function closeModal(modalId) {
  const target = document.getElementById(modalId);
  if (target) target.style.display = 'none';
  document.getElementById('modalBackdrop').classList.remove('active');
}

function closeModalOnBackdrop(event) {
  if (event.target.id === 'modalBackdrop') {
    document.querySelectorAll('.modal-content').forEach(m => m.style.display = 'none');
    document.getElementById('modalBackdrop').classList.remove('active');
  }
}

/* ═══════════════════════════════════════════
   1. DASHBOARD VIEW LOADER
   ═══════════════════════════════════════════ */
function loadDashboardView() {
  updateGreeting();
  loadDashboardMetrics();
  loadBriefing();
  initDayOrderSelector();
}

async function loadDashboardMetrics() {
  try {
    const m = await apiFetch('/dashboard/metrics');
    
    // Study Time
    const hours = Math.floor(m.studyTimeMinutes / 60);
    const mins = m.studyTimeMinutes % 60;
    document.getElementById('kpiStudyTime').textContent = hours > 0 ? `${hours}h ${mins}m` : `${mins}m`;
    
    // Topics Studied
    document.getElementById('kpiTopicsStudied').textContent = m.topicsStudiedCount;
    
    // Tasks
    document.getElementById('kpiTasksCompleted').textContent = `${m.tasksCompletedCount}/${m.tasksTotalCount}`;
    
    // Revision Queue
    document.getElementById('kpiRevisionQueue').textContent = m.revisionQueueSize;
    
    // Sidebar focus bars
    updateFocusBar('focusDsa', m.dsaDone, m.dsaTarget);
    updateFocusBar('focusSql', m.sqlDone, m.sqlTarget);
    updateFocusBar('focusApt', m.aptitudeDone, m.aptitudeTarget);
  } catch (err) {}
}

function updateFocusBar(id, done, target) {
  const pct = target > 0 ? Math.min(100, Math.round((done / target) * 100)) : 0;
  const el = document.getElementById(id);
  if (el) el.style.width = `${pct}%`;
}

async function loadBriefing() {
  const box = document.getElementById('briefingContent');
  const badge = document.getElementById('briefingCacheBadge');
  try {
    const data = await apiFetch('/briefing/today');
    box.innerText = data.briefingText || 'No briefing generated today.';
    badge.innerText = data.isCached ? 'Cached (1/Day)' : 'Fresh';
  } catch (err) {
    box.innerText = 'Unable to fetch briefing. Ensure backend is running.';
  }
}

async function regenerateBriefing() {
  const box = document.getElementById('briefingContent');
  box.innerText = '🤖 Generating AI briefing via Groq LLM...';
  try {
    const data = await apiFetch('/briefing/today/regenerate', { method: 'POST' });
    box.innerText = data.briefingText;
    document.getElementById('briefingCacheBadge').innerText = 'Fresh';
  } catch (err) {
    box.innerText = 'Regeneration failed. Check LLM API key.';
    loadBriefing();
  }
}

async function loadPracticeQuotas() {
  try {
    const q = await apiFetch('/practice/today-quota');
    updateStudyPlan('dsa', q.dsaDone, q.dsaTarget);
    updateStudyPlan('sql', q.sqlDone, q.sqlTarget);
    updateStudyPlan('apt', q.aptitudeDone, q.aptitudeTarget);
  } catch (err) {}
}

function updateStudyPlan(prefix, done, target) {
  const pct = target > 0 ? Math.min(100, Math.round((done / target) * 100)) : 0;
  const fill = document.getElementById(`${prefix}PlanFill`);
  const text = document.getElementById(`${prefix}CountText`);
  if (fill) fill.style.width = `${pct}%`;
  if (text) text.textContent = `${done}/${target}`;
}

/* ─── Live Clock ─── */
function startLiveClock() {
  updateClockDisplay();
  setInterval(updateClockDisplay, 1000);
}

function updateClockDisplay() {
  const clockEl = document.getElementById('headerClock');
  if (clockEl) {
    const now = new Date();
    clockEl.textContent = now.toLocaleTimeString('en-US', { hour12: true, hour: '2-digit', minute: '2-digit', second: '2-digit' });
  }
}

function getSlotTimeStatus(startTimeStr, endTimeStr, dateStr) {
  const now = new Date();
  const todayStr = now.toISOString().split('T')[0];

  if (dateStr && dateStr !== todayStr) {
    if (dateStr < todayStr) return 'PAST';
    return 'UPCOMING';
  }

  const currentHHMMSS = now.toTimeString().split(' ')[0]; // "12:34:31"
  
  // Format times to HH:mm:ss for clean lexicographical comparison
  const startNorm = startTimeStr.length === 5 ? `${startTimeStr}:00` : startTimeStr;
  const endNorm = endTimeStr.length === 5 ? `${endTimeStr}:00` : endTimeStr;

  if (currentHHMMSS >= startNorm && currentHHMMSS <= endNorm) {
    return 'NOW';
  } else if (currentHHMMSS > endNorm) {
    return 'PAST';
  } else {
    return 'UPCOMING';
  }
}

function updateCurrentFocusBanner(scheduledItems, dateStr) {
  const titleEl = document.getElementById('focusCurrentTitle');
  const subEl = document.getElementById('focusCurrentSub');
  if (!titleEl || !subEl) return;

  if (!scheduledItems || scheduledItems.length === 0) {
    titleEl.textContent = '☕ No Study Tasks Scheduled';
    subEl.textContent = 'Add timetable slots or topics to generate your study plan.';
    return;
  }

  const activeItem = scheduledItems.find(item => getSlotTimeStatus(item.startTime, item.endTime, dateStr) === 'NOW');
  if (activeItem) {
    titleEl.innerHTML = `<span style="color: #34D399;">🟢 NOW ACTIVE:</span> ${activeItem.title}`;
    titleEl.style.color = '#FFF';
    subEl.textContent = `${activeItem.itemType} • ${activeItem.startTime} - ${activeItem.endTime} (${activeItem.durationMinutes} mins) • ${activeItem.details || ''}`;
    return;
  }

  const nextItem = scheduledItems.find(item => getSlotTimeStatus(item.startTime, item.endTime, dateStr) === 'UPCOMING');
  if (nextItem) {
    titleEl.textContent = `☕ Free Window / Break`;
    subEl.textContent = `Next study focus at ${nextItem.startTime}: ${nextItem.title} (${nextItem.durationMinutes} mins)`;
    return;
  }

  titleEl.textContent = `🎉 All Today's Tasks Completed!`;
  subEl.textContent = `Great work! All scheduled blocks for today are complete.`;
}

async function loadSchedule() {
  const container = document.getElementById('scheduleTimeline');
  const dayBadge = document.getElementById('scheduleDayBadge');
  try {
    const data = await apiFetch('/schedule/today');
    if (dayBadge) dayBadge.innerText = `${data.date} (${data.dayOfWeek})`;
    
    if (!data.scheduledItems || data.scheduledItems.length === 0) {
      container.innerHTML = '<div class="timeline-placeholder"><i class="fa-solid fa-calendar-xmark"></i> No scheduled slots today. Add timetable entries or topics!</div>';
      updateCurrentFocusBanner([], data.date);
      return;
    }

    updateCurrentFocusBanner(data.scheduledItems, data.date);

    container.innerHTML = data.scheduledItems.map(item => {
      const dotClass = getTimelineDotClass(item.itemType);
      const status = getSlotTimeStatus(item.startTime, item.endTime, data.date);
      
      let statusBadgeHtml = '<span class="slot-badge-upcoming">⏳ Upcoming</span>';
      let itemClass = '';
      if (status === 'NOW') {
        statusBadgeHtml = '<span class="slot-badge-now">🟢 NOW ACTIVE</span>';
        itemClass = 'active-now';
      } else if (status === 'PAST') {
        statusBadgeHtml = '<span class="slot-badge-past">✓ Completed</span>';
        itemClass = 'past-slot';
      }

      return `
        <div class="timeline-item ${itemClass}">
          <div class="timeline-dot ${dotClass}"></div>
          <div class="timeline-time">${item.startTime} - ${item.endTime}</div>
          <div class="timeline-content">
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <div class="timeline-title">${item.title}</div>
              ${statusBadgeHtml}
            </div>
            <div class="timeline-sub">${item.itemType} • ${item.details || ''} (${item.durationMinutes} mins)</div>
          </div>
        </div>
      `;
    }).join('');
  } catch (err) {
    if (container) container.innerHTML = '<div class="timeline-placeholder" style="color: #F87171;">Could not load schedule.</div>';
  }
}

function getTimelineDotClass(type) {
  const t = (type || '').toLowerCase();
  if (t.includes('class') || t.includes('timetable')) return 'class';
  if (t.includes('dsa')) return 'dsa';
  if (t.includes('revis') || t.includes('topic')) return 'revision';
  if (t.includes('sql') || t.includes('practice') || t.includes('aptitude')) return 'practice';
  if (t.includes('task')) return 'task';
  return 'default';
}

async function loadDueTopics() {
  const container = document.getElementById('revisionQueue');
  try {
    const topics = await apiFetch('/topics/due-for-revision');
    if (!topics || topics.length === 0) {
      container.innerHTML = '<div style="color: #34D399; font-weight: 600; padding: 12px 0; font-size: 0.88rem;"><i class="fa-solid fa-circle-check"></i> All topics revised! 🎉</div>';
      return;
    }

    container.innerHTML = topics.slice(0, 5).map(t => `
      <div class="flashcard">
        <div>
          <div style="font-weight: 600; font-size: 0.88rem;">${t.name}</div>
          <div style="font-size: 0.76rem; color: var(--text-dim);">${t.subjectName || 'General'} • Ease: ${(t.easeFactor || 2.5).toFixed(1)} • ${t.intervalDays || 1}d</div>
        </div>
        <div style="display: flex; gap: 6px;">
          <button class="btn btn-glass btn-sm" onclick="reviewTopic('${t.id}', 'STRUGGLED')">Struggled</button>
          <button class="btn btn-primary btn-sm" onclick="reviewTopic('${t.id}', 'GOOD')">Good</button>
        </div>
      </div>
    `).join('');
  } catch (err) {
    if (container) container.innerHTML = '<div style="color: var(--text-dim); font-size: 0.85rem;">No due topics.</div>';
  }
}

async function reviewTopic(topicId, quality) {
  try {
    await apiFetch(`/topics/${topicId}/review`, {
      method: 'POST',
      body: JSON.stringify({ quality })
    });
    loadDueTopics();
    loadDashboardMetrics();
    loadTopicsView();
  } catch (err) {}
}

async function selectDay(day, chip) {
  document.querySelectorAll('#dayTabs .tab-chip').forEach(c => c.classList.remove('active'));
  if (chip) chip.classList.add('active');

  const container = document.getElementById('freeSlotsList');
  container.innerHTML = '<div style="color: var(--text-muted); font-size: 0.85rem;">Calculating...</div>';

  try {
    const freeSlots = await apiFetch(`/timetable/free-slots?day=${day}`);
    if (!freeSlots || freeSlots.length === 0) {
      container.innerHTML = '<div style="color: var(--text-muted); font-size: 0.85rem;">No free slots on this day.</div>';
      return;
    }

    container.innerHTML = freeSlots.map(s => `
      <div style="background: rgba(16,185,129,0.06); border: 1px solid rgba(16,185,129,0.2); border-radius: 10px; padding: 10px 14px; margin-bottom: 8px;">
        <div style="font-weight: 600; color: #34D399; font-size: 0.88rem;">${s.startTime} - ${s.endTime}</div>
        <div style="font-size: 0.78rem; color: var(--text-dim);">${s.durationMinutes} mins free</div>
      </div>
    `).join('');
  } catch (err) {
    container.innerHTML = '<div style="color: var(--text-muted); font-size: 0.85rem;">Set token to view free slots.</div>';
  }
}

async function loadNotifications() {
  const container = document.getElementById('notifList');
  const badge = document.getElementById('notifBadge');
  try {
    const notifs = await apiFetch('/notifications/pending');
    if (badge) badge.innerText = `${notifs ? notifs.length : 0} Unread`;
    
    if (!notifs || notifs.length === 0) {
      container.innerHTML = '<div style="color: var(--text-muted); font-size: 0.85rem;">No unread alerts.</div>';
      return;
    }

    container.innerHTML = notifs.map(n => `
      <div style="background: rgba(11,15,25,0.5); padding: 10px 14px; border-radius: 10px; margin-bottom: 8px; border-left: 3px solid var(--cyan);">
        <div style="font-weight: 600; font-size: 0.84rem;">${n.title}</div>
        <div style="font-size: 0.78rem; color: var(--text-dim);">${n.body}</div>
      </div>
    `).join('');
  } catch (err) {}
}

/* ═══════════════════════════════════════════
   2. LIVE SCHEDULE VIEW LOADER
   ═══════════════════════════════════════════ */
function loadScheduleView() {
  const picker = document.getElementById('scheduleDatePicker');
  if (picker && !picker.value) {
    picker.value = new Date().toISOString().split('T')[0];
  }
  loadScheduleForDate(picker ? picker.value : null);
  loadTimetableSlots();
  initDayOrderSelector();
}

/* ═══════════════════════════════════════════
   DAY ORDER SELECTOR & COLLEGE TIMETABLE
   ═══════════════════════════════════════════ */
async function initDayOrderSelector() {
  try {
    const data = await apiFetch('/timetable/day-order/current');
    if (data && data.dayOrder) {
      highlightActiveDayOrder(data.dayOrder);
      const summaryEl = document.getElementById('dayOrderSummaryText');
      if (summaryEl) {
        if (data.dayOrder === 'HOLIDAY' || data.dayOrder === 'WEEKEND') {
          summaryEl.innerText = '🎉 Holiday / Weekend Active: 100% Free Study Day (14h optimal study blocks)';
        } else {
          const classCount = data.classSlots ? data.classSlots.length : 0;
          const freeMins = data.freeSlots ? data.freeSlots.reduce((acc, f) => acc + (f.durationMinutes || 0), 0) : 0;
          const freeHours = (freeMins / 60).toFixed(1);
          summaryEl.innerText = `${data.dayOrder.replace('_', ' ')} Active • ${classCount} College Classes • ${freeHours}h Free Study Time allocated`;
        }
      }
    }
  } catch (err) {
    console.warn('Could not load active day order:', err);
  }
}

async function selectDayOrder(dayOrder) {
  try {
    highlightActiveDayOrder(dayOrder);
    const data = await apiFetch('/timetable/day-order/select', {
      method: 'POST',
      body: JSON.stringify({ dayOrder })
    });
    showToast(`🎓 Switched to ${dayOrder.replace('_', ' ')}!`, `College classes updated & study schedule recalculated.`);
    loadDashboardMetrics();
    loadScheduleForDate();
    loadBriefing();
    initDayOrderSelector();
  } catch (err) {
    console.error('Error selecting day order:', err);
  }
}

function highlightActiveDayOrder(dayOrder) {
  const badge = document.getElementById('activeDayOrderBadge');
  if (badge) badge.innerText = dayOrder.replace('_', ' ');

  const pills = document.querySelectorAll('.dayorder-btn');
  pills.forEach(p => p.classList.remove('active'));

  let activeBtn = null;
  if (dayOrder === 'DAY_1') activeBtn = document.getElementById('btnDay1');
  else if (dayOrder === 'DAY_2') activeBtn = document.getElementById('btnDay2');
  else if (dayOrder === 'DAY_3') activeBtn = document.getElementById('btnDay3');
  else if (dayOrder === 'DAY_4') activeBtn = document.getElementById('btnDay4');
  else if (dayOrder === 'DAY_5') activeBtn = document.getElementById('btnDay5');
  else if (dayOrder === 'HOLIDAY' || dayOrder === 'WEEKEND') activeBtn = document.getElementById('btnHoliday');

  if (activeBtn) activeBtn.classList.add('active');
}

/* ═══════════════════════════════════════════
   LIVE SCHEDULE GENERATOR
   ═══════════════════════════════════════════ */
async function loadScheduleForDate(dateStr) {
  const container = document.getElementById('fullScheduleTimeline');
  if (!container) return;
  container.innerHTML = '<div class="timeline-placeholder"><i class="fa-solid fa-spinner fa-spin"></i> Generating schedule...</div>';

  try {
    const validDate = (dateStr && dateStr.trim() !== '') ? dateStr.trim() : null;
    const endpoint = validDate ? `/schedule/date?date=${validDate}` : '/schedule/today';
    const data = await apiFetch(endpoint);

    if (!data.scheduledItems || data.scheduledItems.length === 0) {
      container.innerHTML = '<div class="timeline-placeholder"><i class="fa-solid fa-calendar-xmark"></i> No study blocks scheduled for this date.</div>';
      updateCurrentFocusBanner([], validDate || data.date);
      return;
    }

    updateCurrentFocusBanner(data.scheduledItems, validDate || data.date);

    container.innerHTML = data.scheduledItems.map(item => {
      const isClass = item.itemType === 'COLLEGE_CLASS';
      const status = getSlotTimeStatus(item.startTime, item.endTime, data.date);
      
      let badgeHtml = '';
      let itemClass = isClass ? 'timeline-item is-class' : 'timeline-item is-study';

      if (isClass) {
        let classColorBadge = 'class-badge-industrial';
        const t = (item.title || '').toLowerCase();
        if (t.includes('deep learning')) classColorBadge = 'class-badge-deeplearning';
        else if (t.includes('solar')) classColorBadge = 'class-badge-solar';
        else if (t.includes('sw measurements') || t.includes('metrics')) classColorBadge = 'class-badge-sw-measure';
        else if (t.includes('v&v') || t.includes('verification')) classColorBadge = 'class-badge-sw-vv';
        else if (t.includes('psych')) classColorBadge = 'class-badge-psychology';

        badgeHtml = `<span class="class-badge ${classColorBadge}"><i class="fa-solid fa-graduation-cap"></i> College Class</span>`;
      } else {
        if (status === 'NOW') {
          badgeHtml = '<span class="slot-badge-now">🟢 NOW ACTIVE</span>';
          itemClass += ' active-now';
        } else if (status === 'PAST') {
          badgeHtml = '<span class="slot-badge-past">✓ Past</span>';
          itemClass += ' past-slot';
        } else {
          badgeHtml = '<span class="slot-badge-upcoming">⏳ Study Block</span>';
        }
      }

      // Checkbox for study sessions
      const checkboxHtml = isClass ? '' : `
        <div class="schedule-task-check">
          <input type="checkbox" class="study-checkbox" title="Mark as Completed" 
            data-ref-id="${item.referenceId || ''}"
            data-item-type="${item.itemType || ''}"
            data-title="${escapeHtml(item.title || '')}"
            onchange="toggleScheduledStudyItem(this)">
        </div>
      `;

      return `
        <div class="${itemClass}" id="slot-${item.referenceId || Math.random().toString(36).substr(2, 9)}">
          <div class="timeline-dot ${getTimelineDotClass(item.itemType)}"></div>
          <div class="timeline-time">${item.startTime} - ${item.endTime}</div>
          <div class="timeline-content">
            <div style="display: flex; justify-content: space-between; align-items: center; gap: 8px;">
              <div style="display: flex; align-items: center; gap: 10px;">
                ${checkboxHtml}
                <div class="timeline-title">${escapeHtml(item.title)}</div>
              </div>
              ${badgeHtml}
            </div>
            <div class="timeline-sub">${item.itemType} • ${escapeHtml(item.details || '')} (${item.durationMinutes} mins)</div>
          </div>
        </div>
      `;
    }).join('');

    // Schedule local notifications for items starting today
    scheduleClientNotifications(data.scheduledItems);

  } catch (err) {
    console.error('Failed to load schedule for date:', err);
    container.innerHTML = '<div class="timeline-placeholder" style="color: #F87171;">Failed to load schedule for date.</div>';
  }
}

async function toggleScheduledStudyItem(checkbox) {
  if (!checkbox.checked) return;
  const refId = checkbox.dataset.refId || '';
  const itemType = checkbox.dataset.itemType || '';
  const title = checkbox.dataset.title || 'Study Task';

  const parent = checkbox.closest('.timeline-item');
  if (parent) parent.classList.add('completed-task');

  playSuccessChime();
  showToast('🎉 Completed Task!', `Great job finishing: ${title}`);

  try {
    if (itemType === 'DSA_PRACTICE') {
      await apiFetch(`/dsa/${refId}/review`, { method: 'POST', body: JSON.stringify({ quality: 5 }) });
    } else if (itemType === 'SQL_PRACTICE' || itemType === 'APTITUDE_PRACTICE') {
      await apiFetch(`/practice/questions/${refId}/review`, { method: 'POST', body: JSON.stringify({ quality: 5 }) });
    } else if (itemType === 'TOPIC_REVISION') {
      await apiFetch(`/topics/${refId}/review`, { method: 'POST', body: JSON.stringify({ quality: 5 }) });
    } else if (itemType === 'ADHOC_TASK') {
      await apiFetch(`/tasks/${refId}/status?status=DONE`, { method: 'PATCH' });
    }
    // Update live metrics & study plan
    loadDashboardMetrics();
    if (typeof loadTasksView === 'function') loadTasksView();
    if (typeof loadStudyPlanView === 'function') loadStudyPlanView();
  } catch (err) {
    console.warn('Error updating item review state:', err);
  }
}

/* ═══════════════════════════════════════════
   DESKTOP NOTIFICATIONS & SOUND ENGINE
   ═══════════════════════════════════════════ */
let notifiedSlots = new Set();

function scheduleClientNotifications(scheduledItems) {
  if (!scheduledItems || scheduledItems.length === 0) return;
  if ('Notification' in window && Notification.permission === 'default') {
    Notification.requestPermission();
  }

  const now = new Date();
  const currentMinutes = now.getHours() * 60 + now.getMinutes();

  scheduledItems.forEach(item => {
    if (!item.startTime) return;
    const [h, m] = item.startTime.split(':').map(Number);
    const itemMinutes = h * 60 + m;

    // Trigger notification if slot starts within current minute
    if (itemMinutes === currentMinutes && !notifiedSlots.has(`${item.title}-${item.startTime}`)) {
      notifiedSlots.add(`${item.title}-${item.startTime}`);
      playAlertChime();
      showToast(`⏰ Time for ${item.title}`, `Scheduled slot: ${item.startTime} - ${item.endTime} (${item.details || ''})`);

      if ('Notification' in window && Notification.permission === 'granted') {
        new Notification(`JARVIS: ${item.title}`, {
          body: `Starting now (${item.startTime} - ${item.endTime}) • ${item.details || ''}`,
          icon: '/favicon.ico'
        });
      }
    }
  });
}

// Check every 30 seconds for task start
setInterval(() => {
  if (typeof loadScheduleForDate === 'function') {
    const picker = document.getElementById('scheduleDatePicker');
    loadScheduleForDate(picker ? picker.value : null);
  }
}, 30000);

function playSuccessChime() {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = 'sine';
    osc.frequency.setValueAtTime(587.33, ctx.currentTime); // D5
    osc.frequency.setValueAtTime(880.00, ctx.currentTime + 0.1); // A5
    gain.gain.setValueAtTime(0.2, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.35);
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + 0.35);
  } catch (e) {}
}

function playAlertChime() {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = 'triangle';
    osc.frequency.setValueAtTime(440.00, ctx.currentTime);
    osc.frequency.setValueAtTime(659.25, ctx.currentTime + 0.15);
    gain.gain.setValueAtTime(0.25, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.4);
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + 0.4);
  } catch (e) {}
}

function showToast(title, body) {
  const existing = document.querySelector('.task-alert-toast');
  if (existing) existing.remove();

  const toast = document.createElement('div');
  toast.className = 'task-alert-toast';
  toast.innerHTML = `
    <div style="font-size: 1.5rem; color: #38BDF8;"><i class="fa-solid fa-bell"></i></div>
    <div>
      <div style="font-weight: 700; color: #fff; font-size: 0.9rem;">${title}</div>
      <div style="color: var(--text-muted); font-size: 0.8rem; margin-top: 2px;">${body}</div>
    </div>
  `;
  document.body.appendChild(toast);
  setTimeout(() => toast.remove(), 4500);
}

async function loadTimetableSlots() {
  const container = document.getElementById('timetableSlotList');
  const badge = document.getElementById('slotCountBadge');
  if (!container) return;

  try {
    const slots = await apiFetch('/timetable');
    if (badge) badge.innerText = `${slots ? slots.length : 0} Slots`;

    if (!slots || slots.length === 0) {
      container.innerHTML = '<div class="timeline-placeholder">No timetable slots configured. Add classes to compute free time!</div>';
      return;
    }

    container.innerHTML = slots.map(s => `
      <div class="item-card">
        <div>
          <div class="item-title">${s.label}</div>
          <div class="item-sub">${s.dayOfWeek} • ${s.startTime} - ${s.endTime} (${s.type})</div>
        </div>
        <button class="btn btn-danger btn-sm" onclick="deleteTimetableSlot('${s.id}')">
          <i class="fa-solid fa-trash"></i>
        </button>
      </div>
    `).join('');
  } catch (err) {
    container.innerHTML = '<div class="timeline-placeholder" style="color: #F87171;">Failed to load timetable slots.</div>';
  }
}

async function handleTimetableSubmit(event) {
  event.preventDefault();
  const body = {
    dayOfWeek: document.getElementById('ttDay').value,
    startTime: document.getElementById('ttStart').value,
    endTime: document.getElementById('ttEnd').value,
    type: document.getElementById('ttType').value,
    label: document.getElementById('ttLabel').value
  };

  try {
    await apiFetch('/timetable', { method: 'POST', body: JSON.stringify(body) });
    closeModal('timetableModal');
    loadTimetableSlots();
    loadScheduleView();
  } catch (err) {
    alert(`Failed to create timetable slot: ${err.message}`);
  }
}

async function deleteTimetableSlot(id) {
  if (!confirm('Delete this timetable slot?')) return;
  try {
    await apiFetch(`/timetable/${id}`, { method: 'DELETE' });
    loadTimetableSlots();
    loadScheduleView();
  } catch (err) {
    alert(`Delete failed: ${err.message}`);
  }
}

/* ═══════════════════════════════════════════
   3. STUDY PLAN VIEW LOADER & 7-COL QUESTION MANAGER
   ═══════════════════════════════════════════ */
let studyPlanSubjects = [];
let currentStudyPlanSubjectId = 'ALL';
let currentStudyPlanSubjectName = 'All Subjects';
let studyPlanQuestionsCache = [];
let currentStudyPlanStatusFilter = 'ALL';

async function loadStudyPlanView() {
  try {
    // 1. Fetch Subject Summaries
    const summaries = await apiFetch('/subjects/summary').catch(() => []);
    studyPlanSubjects = summaries || [];

    // 2. Render Subject Tabs / Pills
    const tabsRow = document.getElementById('subjectTabsRow');
    if (tabsRow) {
      let totalAllQuestions = 0;
      studyPlanSubjects.forEach(s => totalAllQuestions += (s.totalQuestions || 0));

      const tabAllActive = currentStudyPlanSubjectId === 'ALL' ? 'active' : '';
      let tabsHtml = `
        <div class="subject-tab ${tabAllActive}" onclick="selectStudyPlanSubject('ALL', this)">
          <i class="fa-solid fa-layer-group"></i>
          <span>All Questions</span>
          <span class="tab-badge" id="tabAllBadge">${totalAllQuestions}</span>
        </div>
      `;

      studyPlanSubjects.forEach(s => {
        const isActive = currentStudyPlanSubjectId === s.id ? 'active' : '';
        tabsHtml += `
          <div class="subject-tab ${isActive}" onclick="selectStudyPlanSubject('${s.id}', this)">
            <i class="fa-solid fa-book"></i>
            <span>${s.name}</span>
            <span class="tab-badge">${s.totalQuestions || 0}</span>
          </div>
        `;
      });

      // Dedicated + New Subject button in tab bar
      tabsHtml += `
        <div class="subject-tab" style="border: 1px dashed rgba(56, 189, 248, 0.4); color: var(--cyan); background: rgba(56, 189, 248, 0.08);" onclick="openModal('createSubjectModal')" title="Create a new study subject">
          <i class="fa-solid fa-plus"></i>
          <span>New Subject</span>
        </div>
      `;

      tabsRow.innerHTML = tabsHtml;
    }

    // 3. Update Active Subject Overview Card
    updateSubjectHeroStats();

    // 4. Fetch and render questions for current active subject
    await fetchAndRenderStudyPlanQuestions();

    // 5. Also refresh quota targets
    populateQuotaModal();
  } catch (err) {
    console.error('Error loading study plan view:', err);
  }
}

function selectStudyPlanSubject(subjectId, element) {
  currentStudyPlanSubjectId = subjectId;
  if (element) {
    document.querySelectorAll('#subjectTabsRow .subject-tab').forEach(t => t.classList.remove('active'));
    element.classList.add('active');
  }

  const found = studyPlanSubjects.find(s => s.id === subjectId);
  currentStudyPlanSubjectName = found ? found.name : 'All Subjects';

  const btnDelete = document.getElementById('btnDeleteCurrentSubject');
  if (btnDelete) {
    btnDelete.style.display = subjectId !== 'ALL' ? 'inline-flex' : 'none';
  }

  const heroUploadText = document.getElementById('heroSubjectUploadText');
  if (heroUploadText) {
    heroUploadText.textContent = subjectId !== 'ALL' ? `Upload Excel for ${currentStudyPlanSubjectName}` : 'Upload 7-Col Excel';
  }

  updateSubjectHeroStats();
  fetchAndRenderStudyPlanQuestions();
}

async function clearAllPreviousData() {
  if (!confirm('⚠️ Are you sure you want to clear all subjects, topics, and practice questions?\n\nThis will remove all previous dummy/uploaded data so you can start completely fresh with your new Excel sheets.')) {
    return;
  }

  try {
    await Promise.all([
      apiFetch('/subjects/all', { method: 'DELETE' }).catch(() => {}),
      apiFetch('/practice/clear-all', { method: 'DELETE' }).catch(() => {}),
      apiFetch('/dsa/all', { method: 'DELETE' }).catch(() => {})
    ]);

    currentStudyPlanSubjectId = 'ALL';
    currentStudyPlanSubjectName = 'All Subjects';
    alert('🧹 All previous subjects and questions have been cleared! You can now create your subjects and upload fresh Excel sheets.');

    // Reload all views
    await loadStudyPlanView();
    loadDashboardView();
    loadSubjects();
    loadScheduleView();
  } catch (err) {
    alert(`Failed to clear data: ${err.message}`);
  }
}

function updateSubjectHeroStats() {
  const heroTitle = document.getElementById('subjectHeroTitle');
  const topicCountBadge = document.getElementById('subjectHeroTopicCount');
  const statTotal = document.getElementById('statTotal');
  const statSolved = document.getElementById('statSolved');
  const statInProgress = document.getElementById('statInProgress');
  const statRevision = document.getElementById('statRevision');
  const statNotStarted = document.getElementById('statNotStarted');
  const progressBar = document.getElementById('subjectProgressBar');

  let total = 0, solved = 0, inProgress = 0, needsRevision = 0, notStarted = 0, topicCount = 0;

  if (currentStudyPlanSubjectId === 'ALL') {
    if (heroTitle) heroTitle.textContent = 'All Subjects & Topics';
    studyPlanSubjects.forEach(s => {
      total += s.totalQuestions || 0;
      solved += s.solvedQuestions || 0;
      inProgress += s.inProgressQuestions || 0;
      needsRevision += s.needsRevisionQuestions || 0;
      notStarted += s.notStartedQuestions || 0;
      topicCount += s.topicCount || 0;
    });
  } else {
    const s = studyPlanSubjects.find(sub => sub.id === currentStudyPlanSubjectId);
    if (s) {
      if (heroTitle) heroTitle.textContent = s.name;
      total = s.totalQuestions || 0;
      solved = s.solvedQuestions || 0;
      inProgress = s.inProgressQuestions || 0;
      needsRevision = s.needsRevisionQuestions || 0;
      notStarted = s.notStartedQuestions || 0;
      topicCount = s.topicCount || 0;
    }
  }

  if (topicCountBadge) topicCountBadge.textContent = `${topicCount} Topics`;
  if (statTotal) statTotal.textContent = total;
  if (statSolved) statSolved.textContent = solved;
  if (statInProgress) statInProgress.textContent = inProgress;
  if (statRevision) statRevision.textContent = needsRevision;
  if (statNotStarted) statNotStarted.textContent = notStarted;

  const pct = total > 0 ? Math.round((solved / total) * 100) : 0;
  if (progressBar) progressBar.style.width = `${pct}%`;
}

async function fetchAndRenderStudyPlanQuestions() {
  const container = document.getElementById('studyPlanQuestionsList');
  if (!container) return;
  container.innerHTML = '<div class="timeline-placeholder"><i class="fa-solid fa-spinner fa-spin"></i> Loading subject questions...</div>';

  try {
    let questions = [];
    if (currentStudyPlanSubjectId !== 'ALL') {
      questions = await apiFetch(`/subjects/${currentStudyPlanSubjectId}/questions`);
    } else {
      // Aggregate practice questions
      const [dsa, sql, apt] = await Promise.all([
        apiFetch('/practice/questions?categoryType=DSA').catch(() => []),
        apiFetch('/practice/questions?categoryType=SQL').catch(() => []),
        apiFetch('/practice/questions?categoryType=APTITUDE').catch(() => [])
      ]);
      questions = [...(dsa || []), ...(sql || []), ...(apt || [])];
    }

    studyPlanQuestionsCache = questions || [];
    renderStudyPlanQuestions();
  } catch (err) {
    container.innerHTML = '<div class="timeline-placeholder" style="color: #F87171;">Could not load questions. Add a question or upload an Excel sheet.</div>';
  }
}

function renderStudyPlanQuestions() {
  const container = document.getElementById('studyPlanQuestionsList');
  if (!container) return;

  const searchVal = (document.getElementById('questionSearchInput')?.value || '').toLowerCase().trim();
  const diffVal = document.getElementById('questionDifficultyFilter')?.value || 'ALL';

  let filtered = studyPlanQuestionsCache.filter(q => {
    // Status filter
    if (currentStudyPlanStatusFilter !== 'ALL') {
      const st = q.status || 'NOT_STARTED';
      if (st !== currentStudyPlanStatusFilter) return false;
    }
    // Difficulty filter
    if (diffVal !== 'ALL') {
      const diff = (q.difficulty || 'MEDIUM').toUpperCase();
      if (diff !== diffVal) return false;
    }
    // Text search filter (title, topic, problemNumber)
    if (searchVal) {
      const title = (q.title || '').toLowerCase();
      const topic = (q.topicName || q.subCategory || q.topic || '').toLowerCase();
      const num = (q.problemNumber || '').toLowerCase();
      const sub = (q.subjectName || '').toLowerCase();
      if (!title.includes(searchVal) && !topic.includes(searchVal) && !num.includes(searchVal) && !sub.includes(searchVal)) {
        return false;
      }
    }
    return true;
  });

  if (filtered.length === 0) {
    container.innerHTML = `
      <div class="timeline-placeholder" style="padding: 40px 20px;">
        <i class="fa-solid fa-folder-open" style="font-size: 2rem; color: var(--text-muted); margin-bottom: 12px;"></i>
        <div>No questions found matching current filters.</div>
        <div style="margin-top: 14px; display: flex; gap: 10px; justify-content: center;">
          <button class="btn btn-primary btn-sm" onclick="openStudyPlanQuestionModal()">
            <i class="fa-solid fa-plus"></i> Add Question
          </button>
          <button class="btn btn-glass btn-sm" onclick="openSubjectExcelModal()">
            <i class="fa-solid fa-file-excel"></i> Upload 7-Col Excel
          </button>
        </div>
      </div>
    `;
    return;
  }

  container.innerHTML = filtered.map((q, idx) => {
    const numDisplay = q.problemNumber ? q.problemNumber : `#${idx + 1}`;
    const diffUpper = (q.difficulty || 'MEDIUM').toUpperCase();
    let diffClass = 'q-diff-medium';
    if (diffUpper === 'EASY') diffClass = 'q-diff-easy';
    else if (diffUpper === 'HARD') diffClass = 'q-diff-hard';

    const topicDisplay = q.topicName || q.subCategory || q.topic || 'General';
    const currentStatus = q.status || 'NOT_STARTED';
    const statusClass = `status-${currentStatus.toLowerCase()}`;

    return `
      <div class="question-row-card">
        <div class="q-left">
          <div class="q-num-badge">${numDisplay}</div>
          <div class="q-info">
            <div class="q-title">
              <span>${q.title}</span>
              ${q.sourceLink ? `
                <a href="${q.sourceLink}" target="_blank" class="q-link-btn" title="Open problem link">
                  <i class="fa-solid fa-arrow-up-right-from-square"></i>
                </a>
              ` : ''}
            </div>
            <div class="q-meta">
              <span class="q-topic-pill"><i class="fa-solid fa-tag"></i> ${topicDisplay}</span>
              ${q.subjectName ? `<span class="q-topic-pill" style="color: #A78BFA;"><i class="fa-solid fa-book"></i> ${q.subjectName}</span>` : ''}
              <span class="q-diff-badge ${diffClass}">${diffUpper}</span>
            </div>
          </div>
        </div>

        <div class="q-right">
          <select class="q-status-select ${statusClass}" onchange="updateQuestionStatus('${q.id}', this.value, this)">
            <option value="NOT_STARTED" ${currentStatus === 'NOT_STARTED' ? 'selected' : ''}>Not Started</option>
            <option value="IN_PROGRESS" ${currentStatus === 'IN_PROGRESS' ? 'selected' : ''}>In Progress</option>
            <option value="SOLVED" ${currentStatus === 'SOLVED' ? 'selected' : ''}>Solved</option>
            <option value="NEEDS_REVISION" ${currentStatus === 'NEEDS_REVISION' ? 'selected' : ''}>Needs Revision</option>
          </select>

          <button class="btn btn-danger btn-sm" onclick="deleteStudyPlanQuestion('${q.id}')" title="Delete question">
            <i class="fa-solid fa-trash"></i>
          </button>
        </div>
      </div>
    `;
  }).join('');
}

function setQuestionStatusFilter(status, chip) {
  currentStudyPlanStatusFilter = status;
  document.querySelectorAll('#view-studyplan .tab-chip').forEach(c => c.classList.remove('active'));
  if (chip) chip.classList.add('active');
  renderStudyPlanQuestions();
}

function filterStudyPlanQuestions() {
  renderStudyPlanQuestions();
}

async function updateQuestionStatus(questionId, newStatus, selectElement) {
  try {
    await apiFetch(`/practice/questions/${questionId}/status?status=${newStatus}`, { method: 'PATCH' });
    
    // Update local cache
    const item = studyPlanQuestionsCache.find(q => q.id === questionId);
    if (item) item.status = newStatus;

    if (selectElement) {
      selectElement.className = `q-status-select status-${newStatus.toLowerCase()}`;
    }

    // Refresh subjects summary for stats
    const summaries = await apiFetch('/subjects/summary').catch(() => []);
    studyPlanSubjects = summaries || [];
    updateSubjectHeroStats();
    loadDashboardView();
  } catch (err) {
    alert(`Failed to update status: ${err.message}`);
  }
}

async function handleStudyPlanSubjectCreate(event) {
  event.preventDefault();
  const input = document.getElementById('newSubjectNameInput');
  const name = input ? input.value.trim() : '';
  if (!name) return;

  try {
    const res = await apiFetch('/subjects', {
      method: 'POST',
      body: JSON.stringify({ name })
    });

    closeModal('createSubjectModal');
    input.value = '';
    alert(`🎉 Subject '${name}' created successfully!`);

    await loadStudyPlanView();
    if (res && res.id) {
      selectStudyPlanSubject(res.id);
    }
  } catch (err) {
    alert(`Failed to create subject: ${err.message}`);
  }
}

async function deleteCurrentSubject() {
  if (currentStudyPlanSubjectId === 'ALL') return;
  if (!confirm(`Are you sure you want to delete subject '${currentStudyPlanSubjectName}' and all its questions?`)) return;

  try {
    await apiFetch(`/subjects/${currentStudyPlanSubjectId}`, { method: 'DELETE' });
    alert(`Subject '${currentStudyPlanSubjectName}' deleted.`);
    currentStudyPlanSubjectId = 'ALL';
    loadStudyPlanView();
    loadDashboardView();
  } catch (err) {
    alert(`Delete failed: ${err.message}`);
  }
}

function openStudyPlanQuestionModal() {
  const select = document.getElementById('spqSubjectSelect');
  if (select) {
    select.innerHTML = '<option value="">Select Subject</option>' +
      studyPlanSubjects.map(s => `
        <option value="${s.id}" ${currentStudyPlanSubjectId === s.id ? 'selected' : ''}>${s.name}</option>
      `).join('');
  }
  openModal('studyPlanQuestionModal');
}

async function handleStudyPlanQuestionSubmit(event) {
  event.preventDefault();
  const subjectId = document.getElementById('spqSubjectSelect').value;
  const topicName = document.getElementById('spqTopicInput').value.trim();
  const title = document.getElementById('spqTitleInput').value.trim();
  const problemNumber = document.getElementById('spqNumInput').value.trim();
  const difficulty = document.getElementById('spqDifficultySelect').value;
  const status = document.getElementById('spqStatusSelect').value;
  const sourceLink = document.getElementById('spqLinkInput').value.trim();

  const selectedSub = studyPlanSubjects.find(s => s.id === subjectId);
  const subjectName = selectedSub ? selectedSub.name : 'General Study';

  try {
    await apiFetch('/practice/questions', {
      method: 'POST',
      body: JSON.stringify({
        categoryType: 'DSA',
        subCategory: topicName,
        title,
        difficulty,
        subjectId,
        subjectName,
        problemNumber: problemNumber || null,
        sourceLink: sourceLink || null,
        status: status || 'NOT_STARTED'
      })
    });

    closeModal('studyPlanQuestionModal');
    loadStudyPlanView();
    loadDashboardView();
  } catch (err) {
    alert(`Failed to add question: ${err.message}`);
  }
}

function openSubjectExcelModal() {
  const select = document.getElementById('excelTargetSubjectSelect');
  if (select) {
    select.innerHTML = '<option value="">Auto-Detect from Column 1 / File Name</option>' +
      studyPlanSubjects.map(s => `
        <option value="${s.name}" ${currentStudyPlanSubjectId === s.id ? 'selected' : ''}>${s.name}</option>
      `).join('');
  }
  openModal('subjectExcelModal');
}

async function handleSubjectExcelSubmit(event) {
  event.preventDefault();
  let targetSubject = document.getElementById('excelTargetSubjectSelect').value;
  if (!targetSubject && currentStudyPlanSubjectId !== 'ALL') {
    targetSubject = currentStudyPlanSubjectName;
  }

  const fileInput = document.getElementById('subjectExcelFileInput');
  const file = fileInput ? fileInput.files[0] : null;

  if (!file) {
    alert('Please select a spreadsheet file (.xlsx, .xls, .pdf).');
    return;
  }

  const formData = new FormData();
  formData.append('file', file);
  if (targetSubject) formData.append('subjectName', targetSubject);

  const submitBtn = event.target.querySelector('button[type="submit"]');
  const origText = submitBtn ? submitBtn.innerHTML : '';
  if (submitBtn) {
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Parsing 7 Columns...';
  }

  try {
    const res = await fetch(`${API_BASE_URL}/practice/import`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${getJwtToken()}`
      },
      body: formData
    });

    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.innerHTML = origText;
    }

    if (!res.ok) {
      const errJson = await res.json().catch(() => ({}));
      throw new Error(errJson.message || `Import failed with status ${res.status}`);
    }

    const data = await res.json();
    alert(`🎉 ${data.message || '7-Column Excel imported successfully!'}`);
    closeModal('subjectExcelModal');
    if (fileInput) fileInput.value = '';

    // Refresh all views
    await loadStudyPlanView();
    if (targetSubject) {
      const found = studyPlanSubjects.find(s => s.name.toLowerCase() === targetSubject.toLowerCase());
      if (found) selectStudyPlanSubject(found.id);
    }
    loadSubjects();
    loadDashboardView();
  } catch (err) {
    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.innerHTML = origText;
    }
    alert(`Upload failed: ${err.message}`);
  }
}

async function populateQuotaModal() {
  try {
    const q = await apiFetch('/practice/quota-config');
    document.getElementById('quotaDsaTarget').value = q.dsaTarget || 5;
    document.getElementById('quotaSqlTarget').value = q.sqlTarget || 5;
    document.getElementById('quotaAptTarget').value = q.aptitudeTarget || 5;
  } catch (err) {}
}

async function handleQuotaSubmit(event) {
  event.preventDefault();
  const body = {
    dsaTarget: parseInt(document.getElementById('quotaDsaTarget').value),
    sqlTarget: parseInt(document.getElementById('quotaSqlTarget').value),
    aptitudeTarget: parseInt(document.getElementById('quotaAptTarget').value)
  };

  try {
    await apiFetch('/practice/quota-config', { method: 'PUT', body: JSON.stringify(body) });
    closeModal('quotaModal');
    loadStudyPlanView();
    loadDashboardView();
  } catch (err) {
    alert(`Failed to update quotas: ${err.message}`);
  }
}

/* ═══════════════════════════════════════════
   4. TOPICS TRACKER VIEW LOADER
   ═══════════════════════════════════════════ */
function loadTopicsView() {
  loadSubjects();
  loadFullRevisionQueue();
}

async function loadSubjects() {
  const container = document.getElementById('subjectsList');
  if (!container) return;

  try {
    const subjects = await apiFetch('/subjects');
    if (!subjects || subjects.length === 0) {
      container.innerHTML = '<div class="timeline-placeholder">No subjects created yet. Click "Add Subject" to begin!</div>';
      return;
    }

    let html = '';
    for (const sub of subjects) {
      const topics = await apiFetch(`/topics/subject/${sub.id}`).catch(() => []);
      html += `
        <div style="background: rgba(11,15,25,0.6); border: 1px solid var(--card-border); border-radius: var(--radius-md); padding: 14px 16px; margin-bottom: 12px;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
            <div style="font-weight: 700; font-size: 0.95rem; color: #A78BFA;">${sub.name}</div>
            <button class="btn btn-danger btn-sm" onclick="deleteSubject('${sub.id}')"><i class="fa-solid fa-trash"></i></button>
          </div>
          <div style="display: flex; flex-direction: column; gap: 6px; padding-left: 8px; border-left: 2px solid rgba(167,139,250,0.3);">
            ${topics.length === 0 ? '<div style="font-size: 0.78rem; color: var(--text-dim);">No topics added to this subject.</div>' : topics.map(t => `
              <div style="display: flex; justify-content: space-between; align-items: center; font-size: 0.84rem; padding: 4px 0;">
                <div>
                  <span style="font-weight: 500;">${t.name}</span>
                  <span style="font-size: 0.72rem; color: var(--text-dim); margin-left: 8px;">Ease: ${(t.easeFactor || 2.5).toFixed(1)} • ${t.intervalDays || 1}d interval</span>
                </div>
                <button class="btn btn-glass btn-sm" style="padding: 2px 6px; font-size: 0.7rem;" onclick="deleteTopic('${t.id}')"><i class="fa-solid fa-xmark"></i></button>
              </div>
            `).join('')}
          </div>
        </div>
      `;
    }
    container.innerHTML = html;
  } catch (err) {
    container.innerHTML = '<div class="timeline-placeholder" style="color: #F87171;">Could not load subjects.</div>';
  }
}

async function populateSubjectDropdown() {
  const select = document.getElementById('topicSubjectSelect');
  if (!select) return;
  try {
    const subjects = await apiFetch('/subjects');
    select.innerHTML = subjects.map(s => `<option value="${s.id}">${s.name}</option>`).join('');
  } catch (err) {
    select.innerHTML = '<option value="">Failed to load subjects</option>';
  }
}

async function handleSubjectSubmit(event) {
  event.preventDefault();
  const name = document.getElementById('subjectName').value;
  try {
    await apiFetch('/subjects', { method: 'POST', body: JSON.stringify({ name }) });
    closeModal('subjectModal');
    loadSubjects();
  } catch (err) {
    alert(`Failed to add subject: ${err.message}`);
  }
}

async function deleteSubject(id) {
  if (!confirm('Delete subject and all its topics?')) return;
  try {
    await apiFetch(`/subjects/${id}`, { method: 'DELETE' });
    loadSubjects();
    loadDueTopics();
    if (typeof loadScheduleView === 'function') loadScheduleView();
    if (typeof loadDashboardView === 'function') loadDashboardView();
  } catch (err) {
    alert(`Delete failed: ${err.message}`);
  }
}

async function handleTopicSubmit(event) {
  event.preventDefault();
  const subjectId = document.getElementById('topicSubjectSelect').value;
  const name = document.getElementById('topicName').value;
  try {
    await apiFetch('/topics', {
      method: 'POST',
      body: JSON.stringify({ subjectId, name, status: 'NOT_STARTED' })
    });
    closeModal('topicModal');
    loadSubjects();
    loadDueTopics();
    if (typeof loadScheduleView === 'function') loadScheduleView();
    if (typeof loadDashboardView === 'function') loadDashboardView();
  } catch (err) {
    alert(`Failed to add topic: ${err.message}`);
  }
}

async function deleteTopic(id) {
  if (!confirm('Delete this topic?')) return;
  try {
    await apiFetch(`/topics/${id}`, { method: 'DELETE' });
    loadSubjects();
    loadDueTopics();
    if (typeof loadScheduleView === 'function') loadScheduleView();
    if (typeof loadDashboardView === 'function') loadDashboardView();
  } catch (err) {
    alert(`Delete failed: ${err.message}`);
  }
}

async function loadFullRevisionQueue() {
  const container = document.getElementById('fullRevisionQueue');
  const badge = document.getElementById('dueRevisionBadge');
  if (!container) return;

  try {
    const topics = await apiFetch('/topics/due-for-revision');
    if (badge) badge.innerText = `${topics ? topics.length : 0} Due`;

    if (!topics || topics.length === 0) {
      container.innerHTML = '<div style="color: #34D399; font-weight: 600; padding: 20px 0; text-align: center;"><i class="fa-solid fa-circle-check"></i> All topics revised! SM-2 algorithm is up to date.</div>';
      return;
    }

    container.innerHTML = topics.map(t => `
      <div class="flashcard">
        <div>
          <div style="font-weight: 600; font-size: 0.88rem;">${t.name}</div>
          <div style="font-size: 0.76rem; color: var(--text-dim);">${t.subjectName || 'General'} • Ease: ${(t.easeFactor || 2.5).toFixed(1)} • ${t.intervalDays || 1}d interval</div>
        </div>
        <div style="display: flex; gap: 6px;">
          <button class="btn btn-glass btn-sm" onclick="reviewTopic('${t.id}', 'STRUGGLED')">Struggled</button>
          <button class="btn btn-glass btn-sm" onclick="reviewTopic('${t.id}', 'GOOD')">Good</button>
          <button class="btn btn-primary btn-sm" onclick="reviewTopic('${t.id}', 'EASY')">Easy</button>
        </div>
      </div>
    `).join('');
  } catch (err) {
    container.innerHTML = '<div class="timeline-placeholder" style="color: #F87171;">Could not load revision queue.</div>';
  }
}

/* ═══════════════════════════════════════════
   5. TASKS & PRIORITY EVENTS VIEW LOADER
   ═══════════════════════════════════════════ */
function loadTasksView() {
  loadTasksManager();
  loadPriorityEvents();
}

function switchTaskFilter(filter, chip) {
  currentTaskFilter = filter;
  document.querySelectorAll('#view-tasks .tab-chip').forEach(c => c.classList.remove('active'));
  if (chip) chip.classList.add('active');
  loadTasksManager();
}

async function loadTasksManager() {
  const container = document.getElementById('tasksManagerList');
  if (!container) return;

  try {
    const filterParam = currentTaskFilter === 'ALL' ? '' : `?status=${currentTaskFilter}`;
    const tasks = await apiFetch(`/tasks${filterParam}`);

    if (!tasks || tasks.length === 0) {
      container.innerHTML = '<div class="timeline-placeholder">No tasks found. Click "Add Task" to create one!</div>';
      return;
    }

    container.innerHTML = tasks.map(t => `
      <div class="item-card">
        <div style="display: flex; align-items: center; gap: 12px;">
          <input type="checkbox" ${t.status === 'DONE' ? 'checked' : ''} onchange="toggleTaskStatus('${t.id}', this.checked ? 'DONE' : 'PENDING')" style="width: 18px; height: 18px; cursor: pointer;">
          <div>
            <div class="item-title" style="${t.status === 'DONE' ? 'text-decoration: line-through; color: var(--text-dim);' : ''}">${t.title}</div>
            <div class="item-sub">${t.description || ''} • Priority: ${t.priority} ${t.dueDate ? `• Due: ${new Date(t.dueDate).toLocaleDateString()}` : ''}</div>
          </div>
        </div>
        <button class="btn btn-danger btn-sm" onclick="deleteTask('${t.id}')">
          <i class="fa-solid fa-trash"></i>
        </button>
      </div>
    `).join('');
  } catch (err) {
    container.innerHTML = '<div class="timeline-placeholder" style="color: #F87171;">Could not load tasks.</div>';
  }
}

async function handleTaskSubmit(event) {
  event.preventDefault();
  const title = document.getElementById('taskTitle').value;
  const description = document.getElementById('taskDesc').value;
  const dueDateInput = document.getElementById('taskDueDate').value;
  const priority = document.getElementById('taskPriority').value;

  const dueDate = dueDateInput ? new Date(dueDateInput).toISOString() : null;

  try {
    await apiFetch('/tasks', {
      method: 'POST',
      body: JSON.stringify({ title, description, dueDate, priority })
    });
    closeModal('taskModal');
    loadTasksManager();
    loadDashboardMetrics();
  } catch (err) {
    alert(`Failed to create task: ${err.message}`);
  }
}

async function toggleTaskStatus(id, newStatus) {
  try {
    await apiFetch(`/tasks/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status: newStatus })
    });
    loadTasksManager();
    loadDashboardMetrics();
  } catch (err) {
    alert(`Failed to update status: ${err.message}`);
  }
}

async function deleteTask(id) {
  if (!confirm('Delete task?')) return;
  try {
    await apiFetch(`/tasks/${id}`, { method: 'DELETE' });
    loadTasksManager();
    loadDashboardMetrics();
  } catch (err) {
    alert(`Delete failed: ${err.message}`);
  }
}

async function loadPriorityEvents() {
  const container = document.getElementById('priorityEventsList');
  if (!container) return;

  try {
    const events = await apiFetch('/priority-events/upcoming');
    if (!events || events.length === 0) {
      container.innerHTML = '<div class="timeline-placeholder">No upcoming placement exams or priority events.</div>';
      return;
    }

    container.innerHTML = events.map(e => `
      <div class="item-card" style="border-left: 3px solid var(--amber);">
        <div>
          <div class="item-title">${e.name}</div>
          <div class="item-sub">${e.type} • Date: ${new Date(e.eventDate).toLocaleString()}</div>
        </div>
        <button class="btn btn-danger btn-sm" onclick="deletePriorityEvent('${e.id}')">
          <i class="fa-solid fa-trash"></i>
        </button>
      </div>
    `).join('');
  } catch (err) {
    container.innerHTML = '<div class="timeline-placeholder" style="color: #F87171;">Could not load priority events.</div>';
  }
}

async function handlePriorityEventSubmit(event) {
  event.preventDefault();
  const name = document.getElementById('eventName').value;
  const eventDateInput = document.getElementById('eventDate').value;
  const type = document.getElementById('eventType').value;
  const jdText = document.getElementById('eventJdText').value;

  const eventDate = eventDateInput ? new Date(eventDateInput).toISOString() : new Date().toISOString();

  try {
    await apiFetch('/priority-events', {
      method: 'POST',
      body: JSON.stringify({ name, eventDate, type, jdText })
    });
    closeModal('priorityEventModal');
    loadPriorityEvents();
    loadSchedule();
  } catch (err) {
    alert(`Failed to create priority event: ${err.message}`);
  }
}

async function deletePriorityEvent(id) {
  if (!confirm('Delete priority event?')) return;
  try {
    await apiFetch(`/priority-events/${id}`, { method: 'DELETE' });
    loadPriorityEvents();
    loadSchedule();
  } catch (err) {
    alert(`Delete failed: ${err.message}`);
  }
}

/* ═══════════════════════════════════════════
   6. ANALYTICS VIEW LOADER
   ═══════════════════════════════════════════ */
async function loadAnalyticsView() {
  try {
    const m = await apiFetch('/dashboard/metrics');
    
    const hours = (m.studyTimeMinutes / 60).toFixed(1);
    document.getElementById('analyticsTotalTime').textContent = `${hours} hours`;

    const totalQuotas = m.dsaTarget + m.sqlTarget + m.aptitudeTarget;
    const doneQuotas = m.dsaDone + m.sqlDone + m.aptitudeDone;
    const quotaPct = totalQuotas > 0 ? Math.round((doneQuotas / totalQuotas) * 100) : 0;
    document.getElementById('analyticsQuotaPct').textContent = `${quotaPct}%`;

    const taskPct = m.tasksTotalCount > 0 ? Math.round((m.tasksCompletedCount / m.tasksTotalCount) * 100) : 0;
    document.getElementById('analyticsTaskPct').textContent = `${taskPct}%`;

    const breakdown = document.getElementById('analyticsBreakdown');
    if (breakdown) {
      breakdown.innerHTML = `
        <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px;">
          <div style="background: rgba(11,15,25,0.6); padding: 16px; border-radius: 12px; border: 1px solid var(--card-border);">
            <div style="font-weight: 700; color: #38BDF8; font-size: 1.1rem;">${m.dsaDone}/${m.dsaTarget}</div>
            <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 4px;">DSA Problems Solved</div>
          </div>
          <div style="background: rgba(11,15,25,0.6); padding: 16px; border-radius: 12px; border: 1px solid var(--card-border);">
            <div style="font-weight: 700; color: #10B981; font-size: 1.1rem;">${m.sqlDone}/${m.sqlTarget}</div>
            <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 4px;">SQL Queries Executed</div>
          </div>
          <div style="background: rgba(11,15,25,0.6); padding: 16px; border-radius: 12px; border: 1px solid var(--card-border);">
            <div style="font-weight: 700; color: #F59E0B; font-size: 1.1rem;">${m.aptitudeDone}/${m.aptitudeTarget}</div>
            <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 4px;">Aptitude Questions Completed</div>
          </div>
        </div>
      `;
    }
  } catch (err) {}
}

/* ═══════════════════════════════════════════
   7. AI CHAT ASSISTANT
   ═══════════════════════════════════════════ */
function sendQuickPrompt(text) {
  document.getElementById('chatInput').value = text;
  sendChatMessage();
}

async function sendChatMessage() {
  const input = document.getElementById('chatInput');
  const message = input.value.trim();
  if (!message) return;

  const messagesContainer = document.getElementById('chatMessages');
  
  // Add user message bubble
  appendChatBubble('user', message);
  input.value = '';
  
  // Show typing indicator
  const typingId = 'typing-' + Date.now();
  messagesContainer.innerHTML += `
    <div class="chat-bubble assistant" id="${typingId}">
      <div class="bubble-avatar"><i class="fa-solid fa-robot"></i></div>
      <div class="bubble-content">
        <div class="typing-indicator"><span></span><span></span><span></span></div>
      </div>
    </div>
  `;
  scrollChatToBottom();

  const sendBtn = document.getElementById('chatSendBtn');
  sendBtn.disabled = true;

  try {
    const response = await apiFetch('/chat/message', {
      method: 'POST',
      body: JSON.stringify({ message })
    });
    
    const typingEl = document.getElementById(typingId);
    if (typingEl) typingEl.remove();
    
    appendChatBubble('assistant', response.reply);
    
    if (response.actionExecuted) {
      loadDashboardView();
    }
  } catch (err) {
    const typingEl = document.getElementById(typingId);
    if (typingEl) typingEl.remove();
    
    appendChatBubble('assistant', '⚠️ Unable to connect to backend on port 8080.');
  }
  
  sendBtn.disabled = false;
}

function appendChatBubble(role, text) {
  const messagesContainer = document.getElementById('chatMessages');
  const avatarContent = role === 'assistant' ? '<i class="fa-solid fa-robot"></i>' : 'R';
  const formattedText = formatChatText(text);
  
  messagesContainer.innerHTML += `
    <div class="chat-bubble ${role}">
      <div class="bubble-avatar">${avatarContent}</div>
      <div class="bubble-content">${formattedText}</div>
    </div>
  `;
  scrollChatToBottom();
}

function formatChatText(text) {
  if (!text) return '<p>...</p>';
  text = text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
  text = text.replace(/\*(.*?)\*/g, '<em>$1</em>');
  const paragraphs = text.split('\n').filter(p => p.trim());
  return paragraphs.map(p => `<p>${p}</p>`).join('');
}

function scrollChatToBottom() {
  const container = document.getElementById('chatMessages');
  if (container) {
    setTimeout(() => { container.scrollTop = container.scrollHeight; }, 50);
  }
}

/* ═══════════════════════════════════════════
   8. EXCEL UPLOAD
   ═══════════════════════════════════════════ */
async function handleExcelUpload(event) {
  const file = event.target.files[0];
  if (!file) return;

  const targetInput = event.target;
  const originalLabel = targetInput.parentElement.innerHTML;
  
  // Show uploading state on button label
  targetInput.parentElement.style.pointerEvents = 'none';
  targetInput.parentElement.style.opacity = '0.7';

  const formData = new FormData();
  formData.append('file', file);

  try {
    const res = await fetch(`${API_BASE}/dsa/import`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${authToken}` },
      body: formData
    });

    if (!res.ok) {
      const errText = await res.text();
      alert(`Upload Failed: ${errText}`);
      return;
    }

    const data = await res.json();
    alert(`✅ ${data.message || `Imported ${data.importedCount || 'all'} items into JARVIS!`}`);
    
    // Refresh all views to show imported items immediately
    loadDashboardView();
    if (typeof loadTopicsView === 'function') loadTopicsView();
    if (typeof loadStudyPlanView === 'function') loadStudyPlanView();
    if (typeof loadScheduleView === 'function') loadScheduleView();
  } catch (err) {
    alert(`Upload Error: ${err.message}`);
  } finally {
    targetInput.parentElement.style.pointerEvents = 'auto';
    targetInput.parentElement.style.opacity = '1';
    targetInput.value = ''; // Always clear file selection so user can re-upload
  }
}

/* ═══════════════════════════════════════════
   INIT ON DOM LOAD
   ═══════════════════════════════════════════ */
document.addEventListener('DOMContentLoaded', () => {
  startLiveClock();
  loadDashboardView();
});

/**
 * LOCALJOBS — Master Application Logic
 * Implements interactive hyperlocal micro-task marketplace with complete lifecycle support.
 */

// Categories metadata with icons
const CATEGORIES = [
  { name: 'All', icon: 'fa-shapes' },
  { name: 'Restaurants', icon: 'fa-utensils' },
  { name: 'Retail', icon: 'fa-store' },
  { name: 'Events', icon: 'fa-champagne-glasses' },
  { name: 'Office', icon: 'fa-building' },
  { name: 'Construction', icon: 'fa-helmet-safety' },
  { name: 'Delivery', icon: 'fa-truck-fast' },
  { name: 'IT & Software', icon: 'fa-laptop-code' },
  { name: 'Data Entry', icon: 'fa-keyboard' },
  { name: 'Education', icon: 'fa-graduation-cap' },
  { name: 'Warehouse', icon: 'fa-boxes-stacked' },
  { name: 'Sales', icon: 'fa-chart-column' },
  { name: 'Other', icon: 'fa-briefcase' }
];

let selectedRating = 5;
let currentRatingContext = null; // { taskId, toUserId, role }
let debounceTimer = null;
let currentModalTaskId = null;

// ============================================================================
// BOOTSTRAP APPLICATION
// ============================================================================
document.addEventListener('DOMContentLoaded', async () => {
  initApp();
});

async function initApp() {
  try {
    // 1. Fetch Users
    const users = await API.getUsers();
    State.allUsers = users || [];

    // 2. Select initial active user (if stored in localStorage)
    const savedUserId = localStorage.getItem('localjobs_user_id');
    let initialUser = State.allUsers.find(u => u.id == savedUserId);
    
    // Set initial user if found
    if (initialUser) {
      State.currentUser = initialUser;
      await State.refreshNotifications();
    }

    // 3. Update Navbar based on logged in status
    updateNavbarState();

    // 4. Render Categories scrollbar
    renderCategoriesBar();

    // 5. Initial Navigation & Data Load
    renderHeaderProfile();
    
    // Check URL Hash (e.g. #landing, #admin, #mytasks, #find)
    const initialHash = window.location.hash.replace('#', '').trim();
    if (initialHash && ['landing', 'home', 'find', 'post', 'mytasks', 'earnings', 'profile', 'admin'].includes(initialHash)) {
      navigateTo(initialHash);
    } else {
      if (State.currentUser) {
        navigateTo('home');
      } else {
        navigateTo('landing');
      }
    }

    // Hash change listener
    window.addEventListener('hashchange', () => {
      const hash = window.location.hash.replace('#', '').trim();
      if (hash && ['landing', 'home', 'find', 'post', 'mytasks', 'earnings', 'profile', 'admin'].includes(hash)) {
        navigateTo(hash);
      }
    });

    // 6. Subscribe to State Changes
    State.subscribe(onStateChange);

  } catch (error) {
    console.error('Failed to initialize app:', error);
    showToast('Connecting to local server...', 'info');
  }
}

function updateNavbarState() {
  const isLoggedIn = !!State.currentUser;
  const navGuest = document.getElementById('nav-guest');
  const navUser = document.getElementById('nav-user');
  const guestActions = document.getElementById('header-guest-actions');
  const userActions = document.getElementById('header-user-actions');

  if (navGuest) navGuest.style.display = isLoggedIn ? 'none' : 'flex';
  if (navUser) navUser.style.display = isLoggedIn ? 'flex' : 'none';
  if (guestActions) guestActions.style.display = isLoggedIn ? 'none' : 'flex';
  if (userActions) userActions.style.display = isLoggedIn ? 'flex' : 'none';
}

function handleBrandClick() {
  if (State.currentUser) {
    navigateTo('home');
  } else {
    navigateTo('landing');
  }
}

function scrollToSection(sectionId) {
  if (State.activeScreen !== 'landing') {
    navigateTo('landing');
  }
  setTimeout(() => {
    const el = document.getElementById(sectionId);
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }, 100);
}

function onStateChange(event, payload) {
  if (event === 'USER_CHANGED' || event === 'USER_UPDATED') {
    updateNavbarState();
    renderHeaderProfile();
    refreshActiveScreenData();
  } else if (event === 'USER_LOGGED_OUT') {
    updateNavbarState();
    navigateTo('landing');
  } else if (event === 'SCREEN_CHANGED') {
    updateNavSelection(payload);
    refreshActiveScreenData();
  } else if (event === 'NOTIFICATIONS_UPDATED') {
    updateNotificationBadge(payload.unreadCount);
  }
}

// Helper: Is user fully verified
function isUserVerified(user) {
  if (!user) return false;
  return user.verified === true || user.verificationStatus === 'VERIFIED';
}

// ============================================================================
// NAVIGATION & SCREEN SWITCHER
// ============================================================================
function navigateTo(screenName) {
  // If requesting admin screen, route directly to dedicated admin portal
  if (screenName === 'admin') {
    window.location.href = 'admin.html';
    return;
  }

  // Intercept unverified user trying to post tasks
  if (screenName === 'post' && !isUserVerified(State.currentUser)) {
    if (!State.currentUser) {
      openAuthModal('login');
      showToast('Please sign in first.', 'info');
    } else {
      openModal('modal-verification-required');
    }
    return;
  }

  // Update view containers
  document.querySelectorAll('.screen-view').forEach(view => {
    view.classList.remove('active');
  });
  const targetView = document.getElementById(`screen-${screenName}`);
  if (targetView) {
    targetView.classList.add('active');
  }

  // Update navigation items
  updateNavSelection(screenName);
  State.setScreen(screenName);
  if (window.location.hash.replace('#', '') !== screenName) {
    history.pushState(null, null, `#${screenName}`);
  }
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function updateNavSelection(screenName) {
  document.querySelectorAll('.desktop-nav .nav-link').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.screen === screenName);
  });
  document.querySelectorAll('.mobile-bottom-nav .mobile-nav-item').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.screen === screenName);
  });
}

function refreshActiveScreenData() {
  switch (State.activeScreen) {
    case 'landing':
      loadLandingPageData();
      break;
    case 'home':
      loadHomeScreenData();
      break;
    case 'find':
      loadFindWorkData();
      break;
    case 'mytasks':
      loadMyTasksData();
      break;
    case 'earnings':
      loadEarningsData();
      break;
    case 'profile':
      loadProfileData();
      break;
    case 'verify':
      loadVerificationScreenData();
      break;
  }
}

// ============================================================================
// HEADER & USER PROFILE
// ============================================================================
function renderHeaderProfile() {
  if (!State.currentUser) return;
  const user = State.currentUser;

  const headerAvatar = document.getElementById('header-avatar');
  const headerName = document.getElementById('header-user-name');
  const heroName = document.getElementById('hero-user-name');
  const heroLocation = document.getElementById('hero-location-text');
  const heroVerified = document.getElementById('hero-verified-badge');
  const heroVerifyBtn = document.getElementById('hero-verify-btn');
  const navVerifyBadge = document.getElementById('nav-verify-badge');

  const verified = isUserVerified(user);

  if (headerAvatar) headerAvatar.src = user.avatarUrl || `https://api.dicebear.com/7.x/avataaars/svg?seed=${user.name}`;
  if (headerName) headerName.textContent = user.name;
  if (heroName) heroName.textContent = user.name.split(' ')[0];
  if (heroLocation) heroLocation.textContent = user.location || 'Ongole';

  if (heroVerified) heroVerified.style.display = verified ? 'inline-block' : 'none';
  if (heroVerifyBtn) heroVerifyBtn.style.display = verified ? 'none' : 'inline-flex';
  if (navVerifyBadge) navVerifyBadge.style.display = verified ? 'none' : 'inline-block';
}

function updateNotificationBadge(unreadCount) {
  const badge = document.getElementById('notif-badge');
  if (!badge) return;
  if (unreadCount > 0) {
    badge.textContent = unreadCount;
    badge.style.display = 'flex';
  } else {
    badge.style.display = 'none';
  }
}

// ============================================================================
// 1. HOME SCREEN
// ============================================================================
function renderCategoriesBar() {
  const container = document.getElementById('home-categories-list');
  if (!container) return;

  container.innerHTML = CATEGORIES.map(cat => `
    <button class="category-chip ${cat.name === State.filters.category ? 'active' : ''}" 
            onclick="selectCategoryFilter('${cat.name}')">
      <i class="fa-solid ${cat.icon}"></i> ${cat.name}
    </button>
  `).join('');
}

async function loadHomeScreenData() {
  const grid = document.getElementById('home-tasks-grid');
  if (!grid) return;

  grid.innerHTML = `<div style="grid-column:1/-1; text-align:center; padding:2rem; color:var(--text-muted);">
    <i class="fa-solid fa-spinner fa-spin fa-2x"></i><br>Finding nearby opportunities...
  </div>`;

  try {
    const tasks = await API.getTasks({
      status: 'OPEN',
      userLat: State.currentUser ? State.currentUser.latitude : 15.5057,
      userLng: State.currentUser ? State.currentUser.longitude : 80.0499
    });

    if (!tasks || tasks.length === 0) {
      grid.innerHTML = `
        <div class="empty-state" style="grid-column: 1 / -1;">
          <div class="empty-icon"><i class="fa-solid fa-briefcase"></i></div>
          <h3 class="empty-title">No tasks nearby right now</h3>
          <p class="empty-desc">Be the first in your area to post a task, or check back soon.</p>
          <button class="btn btn-primary" onclick="navigateTo('post')"><i class="fa-solid fa-plus"></i> Post a Task</button>
        </div>
      `;
      return;
    }

    grid.innerHTML = tasks.map(task => renderTaskCard(task, { showDetailsBtn: true })).join('');
  } catch (error) {
    grid.innerHTML = `<div class="empty-state" style="grid-column: 1 / -1;">Failed to load tasks. Please ensure the backend server is running.</div>`;
  }
}

function handleHomeSearch() {
  const input = document.getElementById('home-search-input');
  const term = input ? input.value.trim() : '';
  State.setFilters({ keyword: term });
  const findInput = document.getElementById('find-search-input');
  if (findInput) findInput.value = term;
  navigateTo('find');
}

function selectCategoryFilter(category) {
  State.setFilters({ category });
  renderCategoriesBar();
  const select = document.getElementById('filter-category-select');
  if (select) select.value = category;
  navigateTo('find');
}

// ============================================================================
// 2. FIND WORK SCREEN & FILTERS
// ============================================================================
async function loadFindWorkData() {
  const grid = document.getElementById('find-tasks-grid');
  if (!grid) return;

  grid.innerHTML = `<div style="grid-column:1/-1; text-align:center; padding:2rem; color:var(--text-muted);">
    <i class="fa-solid fa-spinner fa-spin fa-2x"></i><br>Searching tasks...
  </div>`;

  try {
    const tasks = await API.getTasks({
      status: 'OPEN',
      category: State.filters.category,
      maxDistance: State.filters.maxDistance,
      minReward: State.filters.minReward,
      sortByReward: State.filters.sortByReward,
      duration: State.filters.duration,
      keyword: State.filters.keyword,
      userLat: State.currentUser ? State.currentUser.latitude : 15.5057,
      userLng: State.currentUser ? State.currentUser.longitude : 80.0499
    });

    if (!tasks || tasks.length === 0) {
      grid.innerHTML = `
        <div class="empty-state" style="grid-column: 1 / -1;">
          <div class="empty-icon"><i class="fa-solid fa-route"></i></div>
          <h3 class="empty-title">No tasks match your criteria</h3>
          <p class="empty-desc">Try expanding your search distance, changing category, or resetting filters.</p>
          <button class="btn btn-outline" onclick="resetFilters()">Reset All Filters</button>
        </div>
      `;
      return;
    }

    grid.innerHTML = tasks.map(task => renderTaskCard(task, { showDetailsBtn: true })).join('');
  } catch (error) {
    grid.innerHTML = `<div class="empty-state" style="grid-column: 1 / -1;">Error searching tasks.</div>`;
  }
}

function debounceFindSearch() {
  clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => {
    applyFilters();
  }, 350);
}

function applyFilters() {
  const keyword = document.getElementById('find-search-input')?.value.trim() || '';
  const category = document.getElementById('filter-category-select')?.value || 'All';
  const maxDistance = document.getElementById('filter-distance-select')?.value || null;
  const duration = document.getElementById('filter-duration-select')?.value || 'All';
  const sortByReward = document.getElementById('filter-sort-select')?.value || null;

  State.setFilters({
    keyword,
    category,
    maxDistance: maxDistance ? parseFloat(maxDistance) : null,
    duration,
    sortByReward
  });

  loadFindWorkData();
}

function resetFilters() {
  const keywordInput = document.getElementById('find-search-input');
  const catSelect = document.getElementById('filter-category-select');
  const distSelect = document.getElementById('filter-distance-select');
  const durSelect = document.getElementById('filter-duration-select');
  const sortSelect = document.getElementById('filter-sort-select');

  if (keywordInput) keywordInput.value = '';
  if (catSelect) catSelect.value = 'All';
  if (distSelect) distSelect.value = '';
  if (durSelect) durSelect.value = 'All';
  if (sortSelect) sortSelect.value = '';

  State.setFilters({
    category: 'All',
    maxDistance: null,
    minReward: null,
    sortByReward: null,
    duration: 'All',
    keyword: ''
  });

  renderCategoriesBar();
  loadFindWorkData();
}

// ============================================================================
// 3. POST TASK SCREEN
// ============================================================================
async function handlePostTaskSubmit(e) {
  e.preventDefault();
  if (!State.currentUser) {
    openAuthModal('login');
    showToast('Please sign in to post tasks', 'info');
    return;
  }

  if (!isUserVerified(State.currentUser)) {
    openModal('modal-verification-required');
    return;
  }

  const title = document.getElementById('post-title').value.trim();
  const category = document.getElementById('post-category').value;
  const reward = parseFloat(document.getElementById('post-reward').value);
  const duration = document.getElementById('post-duration').value;
  const date = document.getElementById('post-date').value;
  const startTime = document.getElementById('post-start-time').value.trim() || 'Flexible';
  const endTime = document.getElementById('post-end-time').value.trim() || 'Flexible';
  const location = document.getElementById('post-location').value.trim();
  const skillsRaw = document.getElementById('post-skills').value.trim();
  const description = document.getElementById('post-desc').value.trim();

  const requiredSkills = skillsRaw ? skillsRaw.split(',').map(s => s.trim()).filter(Boolean) : [];

  const payload = {
    posterId: State.currentUser.id,
    title,
    category,
    reward,
    duration,
    date,
    startTime,
    endTime,
    location,
    latitude: State.currentUser.latitude || 15.5057,
    longitude: State.currentUser.longitude || 80.0499,
    requiredSkills,
    description
  };

  try {
    const res = await API.createTask(payload);
    if (res.id) {
      showToast(`🎉 Task "${title}" published successfully!`, 'success');
      document.getElementById('post-task-form').reset();
      await State.refreshCurrentUser();
      navigateTo('mytasks');
      switchMyTasksTab('posted');
    } else {
      showToast(res.error || 'Failed to post task', 'error');
    }
  } catch (error) {
    showToast(error.message || 'Error creating task', 'error');
  }
}

// ============================================================================
// 4. MY TASKS SCREEN & LIFECYCLE
// ============================================================================
function switchMyTasksTab(tabName) {
  document.querySelectorAll('.tabs-navigation .tab-btn').forEach(btn => {
    btn.classList.toggle('active', btn.id === `tab-${tabName}`);
  });
  State.setMyTasksTab(tabName);
  loadMyTasksData();
}

async function loadMyTasksData() {
  if (!State.currentUser) return;
  const grid = document.getElementById('mytasks-grid');
  if (!grid) return;

  grid.innerHTML = `<div style="grid-column:1/-1; text-align:center; padding:2rem; color:var(--text-muted);">
    <i class="fa-solid fa-spinner fa-spin fa-2x"></i><br>Loading your tasks...
  </div>`;

  try {
    const [posted, accepted] = await Promise.all([
      API.getMyPostedTasks(State.currentUser.id),
      API.getMyAcceptedTasks(State.currentUser.id)
    ]);

    const postedTasks = posted || [];
    const acceptedTasks = accepted || [];

    // Calculate tab counts
    const countPosted = postedTasks.filter(t => t.status === 'OPEN' || t.status === 'ACCEPTED' || t.status === 'IN_PROGRESS').length;
    const countAccepted = acceptedTasks.filter(t => t.status === 'ACCEPTED' || t.status === 'IN_PROGRESS').length;
    const countCompleted = [...postedTasks, ...acceptedTasks].filter(t => t.status === 'COMPLETED').length;
    const countHistory = [...postedTasks, ...acceptedTasks].filter(t => t.status === 'PAYMENT_RELEASED').length;

    document.getElementById('count-posted').textContent = countPosted;
    document.getElementById('count-accepted').textContent = countAccepted;
    document.getElementById('count-completed').textContent = countCompleted;
    document.getElementById('count-history').textContent = countHistory;

    let displayTasks = [];
    let emptyTitle = '';
    let emptyDesc = '';

    if (State.activeMyTasksTab === 'posted') {
      displayTasks = postedTasks.filter(t => t.status !== 'PAYMENT_RELEASED');
      emptyTitle = "You haven't posted any active tasks yet.";
      emptyDesc = "Need something done? Post a micro-task and offer a fixed reward.";
    } else if (State.activeMyTasksTab === 'accepted') {
      displayTasks = acceptedTasks.filter(t => t.status === 'ACCEPTED' || t.status === 'IN_PROGRESS');
      emptyTitle = "You haven't accepted any tasks yet.";
      emptyDesc = "Find a task nearby, accept it, and start earning.";
    } else if (State.activeMyTasksTab === 'completed') {
      displayTasks = [...postedTasks, ...acceptedTasks].filter(t => t.status === 'COMPLETED');
      emptyTitle = "No tasks currently awaiting review.";
      emptyDesc = "When a worker marks a task completed, confirm to release payment.";
    } else if (State.activeMyTasksTab === 'history') {
      displayTasks = [...postedTasks, ...acceptedTasks].filter(t => t.status === 'PAYMENT_RELEASED');
      emptyTitle = "No payment history yet.";
      emptyDesc = "Tasks with released rewards and ratings will appear here.";
    }

    if (displayTasks.length === 0) {
      grid.innerHTML = `
        <div class="empty-state" style="grid-column: 1 / -1;">
          <div class="empty-icon"><i class="fa-solid fa-folder-open"></i></div>
          <h3 class="empty-title">${emptyTitle}</h3>
          <p class="empty-desc">${emptyDesc}</p>
          <div style="display:flex; justify-content:center; gap:0.75rem;">
            <button class="btn btn-outline" onclick="navigateTo('find')"><i class="fa-solid fa-compass"></i> Find Work</button>
            <button class="btn btn-primary" onclick="navigateTo('post')"><i class="fa-solid fa-plus"></i> Post Work</button>
          </div>
        </div>
      `;
      return;
    }

    grid.innerHTML = displayTasks.map(task => renderTaskCard(task, { showLifecycleActions: true })).join('');
  } catch (error) {
    grid.innerHTML = `<div class="empty-state" style="grid-column: 1 / -1;">Failed to load tasks.</div>`;
  }
}

// ============================================================================
// TASK CARD RENDERER
// ============================================================================
function renderTaskCard(task, options = {}) {
  const isPoster = State.currentUser && task.posterId === State.currentUser.id;
  const isWorker = State.currentUser && task.workerId === State.currentUser.id;

  const statusClass = `status-${task.status.toLowerCase()}`;
  const statusLabel = task.status.replace('_', ' ');

  // Determine Lifecycle Action Buttons
  let actionButtonsHtml = '';

  if (options.showLifecycleActions) {
    if (task.status === 'ACCEPTED') {
      if (isWorker) {
        actionButtonsHtml = `
          <button class="btn btn-primary btn-sm btn-block" onclick="handleStartTask(${task.id})">
            <i class="fa-solid fa-play"></i> Start Task
          </button>`;
      } else if (isPoster) {
        actionButtonsHtml = `
          <span style="font-size:0.8rem; color:var(--info); font-weight:600;">
            <i class="fa-solid fa-circle-notch fa-spin"></i> Assigned to ${task.workerName}. Awaiting start.
          </span>`;
      }
    } else if (task.status === 'IN_PROGRESS') {
      if (isWorker) {
        actionButtonsHtml = `
          <button class="btn btn-primary btn-sm btn-block" style="background:#7e22ce;" onclick="handleCompleteTask(${task.id})">
            <i class="fa-solid fa-check"></i> Mark Completed
          </button>`;
      } else if (isPoster) {
        actionButtonsHtml = `
          <span style="font-size:0.8rem; color:#b45309; font-weight:600;">
            <i class="fa-solid fa-person-digging"></i> ${task.workerName} is currently working on this.
          </span>`;
      }
    } else if (task.status === 'COMPLETED') {
      if (isPoster) {
        actionButtonsHtml = `
          <button class="btn btn-primary btn-sm btn-block" style="background:#059669;" onclick="handleReleasePayment(${task.id})">
            <i class="fa-solid fa-money-bill-wave"></i> Confirm & Release ₹${task.reward}
          </button>`;
      } else if (isWorker) {
        actionButtonsHtml = `
          <span style="font-size:0.8rem; color:#7e22ce; font-weight:600;">
            <i class="fa-solid fa-hourglass-half"></i> Completed! Awaiting poster payment release.
          </span>`;
      }
    } else if (task.status === 'PAYMENT_RELEASED') {
      const targetUserId = isPoster ? task.workerId : task.posterId;
      const targetName = isPoster ? task.workerName : task.posterName;
      const role = isPoster ? 'POSTER_RATING_WORKER' : 'WORKER_RATING_POSTER';
      actionButtonsHtml = `
        <button class="btn btn-outline btn-sm btn-block" onclick="openRatingModal(${task.id}, ${targetUserId}, '${targetName}', '${role}')">
          <i class="fa-solid fa-star text-accent"></i> Rate ${targetName}
        </button>`;
    }
  } else {
    // Discovery Feed (Home & Find Work)
    if (task.status === 'OPEN') {
      if (isPoster) {
        actionButtonsHtml = `
          <button class="btn btn-outline btn-sm" onclick="openTaskDetails(${task.id})">
            <i class="fa-solid fa-eye"></i> View Your Post
          </button>`;
      } else {
        actionButtonsHtml = `
          <div style="display:flex; gap:0.5rem; width:100%;">
            <button class="btn btn-outline btn-sm" style="flex:1;" onclick="openTaskDetails(${task.id})">Details</button>
            <button class="btn btn-primary btn-sm" style="flex:1;" onclick="handleDirectAccept(${task.id}, ${task.reward})">
              <i class="fa-solid fa-handshake"></i> Accept
            </button>
          </div>`;
      }
    }
  }

  return `
    <div class="task-card">
      <div>
        <div class="task-card-header">
          <span class="task-category-tag"><i class="fa-solid fa-tag"></i> ${task.category}</span>
          <span class="task-reward-badge">₹${task.reward}</span>
        </div>

        <h3 class="task-title">${task.title}</h3>
        <p class="task-desc-clamp">${task.description || 'No additional description provided.'}</p>

        <div class="task-meta-list">
          <div class="task-meta-item">
            <i class="fa-solid fa-clock"></i> <span>${task.duration}</span>
            <span style="margin:0 4px; color:var(--border-color);">•</span>
            <i class="fa-solid fa-calendar"></i> <span>${task.date} (${task.startTime || 'Flexible'})</span>
          </div>
          <div class="task-meta-item">
            <i class="fa-solid fa-location-dot"></i>
            <span>${task.location}</span>
            ${task.distanceKm !== null && task.distanceKm !== undefined ? `<span class="task-distance-highlight">• ${task.distanceKm} km away</span>` : ''}
          </div>
        </div>

        <div class="task-poster-row">
          <div class="poster-mini-info">
            <img class="poster-mini-avatar" src="https://api.dicebear.com/7.x/avataaars/svg?seed=${task.posterName.replaceAll(' ', '')}" alt="${task.posterName}">
            <div>
              <div class="poster-mini-name">${task.posterName} <i class="fa-solid fa-circle-check verified-icon" title="Verified Poster"></i></div>
              <div class="poster-mini-rating">★ ${task.posterRating || '5.0'} (${task.posterCompletedTasks || '0'} jobs)</div>
            </div>
          </div>
          <span class="task-status-badge ${statusClass}">${statusLabel}</span>
        </div>
      </div>

      <div style="margin-top: 0.75rem;">
        ${actionButtonsHtml}
      </div>
    </div>
  `;
}

// ============================================================================
// TASK LIFECYCLE CONTROLLERS
// ============================================================================
async function handleDirectAccept(taskId, reward) {
  if (!State.currentUser) {
    openAuthModal('login');
    showToast('Please sign in or create an account to accept tasks', 'info');
    return;
  }
  
  if (!isUserVerified(State.currentUser)) {
    openModal('modal-verification-required');
    return;
  }

  if (confirm(`Do you want to accept this task and commit to earning ₹${reward}?`)) {
    try {
      const res = await API.acceptTask(taskId, State.currentUser.id);
      if (res.id) {
        showToast(`🎉 Task accepted! Moved to "My Tasks".`, 'success');
        await State.refreshCurrentUser();
        navigateTo('mytasks');
        switchMyTasksTab('accepted');
      } else {
        showToast(res.error || 'Failed to accept task', 'error');
      }
    } catch (e) {
      showToast(e.message || 'Error accepting task', 'error');
    }
  }
}

async function handleStartTask(taskId) {
  try {
    const res = await API.startTask(taskId, State.currentUser.id);
    if (res.id) {
      showToast('🚀 Task started! Status is now IN PROGRESS.', 'success');
      loadMyTasksData();
      await State.refreshCurrentUser();
    } else {
      showToast(res.error || 'Could not start task', 'error');
    }
  } catch (e) {
    showToast('Error starting task', 'error');
  }
}

async function handleCompleteTask(taskId) {
  try {
    const res = await API.completeTask(taskId, State.currentUser.id);
    if (res.id) {
      showToast('✅ Task marked COMPLETED! Poster has been notified to confirm and release reward.', 'success');
      loadMyTasksData();
      await State.refreshCurrentUser();
    } else {
      showToast(res.error || 'Could not complete task', 'error');
    }
  } catch (e) {
    showToast('Error completing task', 'error');
  }
}

async function handleReleasePayment(taskId) {
  if (confirm('Confirm work completion and release reward from simulated escrow?')) {
    try {
      const res = await API.releasePayment(taskId, State.currentUser.id);
      if (res.id) {
        showToast(`💰 Payment of ₹${res.reward} successfully released to ${res.workerName}'s wallet!`, 'success');
        await State.refreshCurrentUser();
        loadMyTasksData();

        // Automatically prompt to leave a 5-star rating for the worker
        openRatingModal(res.id, res.workerId, res.workerName, 'POSTER_RATING_WORKER');
      } else {
        showToast(res.error || 'Could not release payment', 'error');
      }
    } catch (e) {
      showToast('Error releasing payment', 'error');
    }
  }
}

// ============================================================================
// 5. EARNINGS & WALLET SCREEN
// ============================================================================
async function loadEarningsData() {
  if (!State.currentUser) return;
  const listContainer = document.getElementById('wallet-transactions-list');
  if (!listContainer) return;

  try {
    const summary = await API.getWalletSummary(State.currentUser.id);

    document.getElementById('wallet-balance').textContent = `₹${(summary.availableBalance || 0).toLocaleString('en-IN')}`;
    document.getElementById('wallet-total-earned').textContent = `₹${(summary.totalEarned || 0).toLocaleString('en-IN')}`;
    document.getElementById('wallet-completed-count').textContent = summary.completedTasks || 0;

    const txs = summary.transactions || [];
    if (txs.length === 0) {
      listContainer.innerHTML = `
        <div style="text-align:center; padding:2rem; color:var(--text-muted);">
          <i class="fa-solid fa-receipt fa-2x" style="margin-bottom:0.5rem; color:var(--text-light);"></i>
          <p>No wallet transactions yet. Complete tasks to start earning!</p>
        </div>
      `;
      return;
    }

    listContainer.innerHTML = txs.map(tx => {
      const isCredit = tx.type === 'CREDIT';
      const icon = isCredit ? 'fa-arrow-down-left' : 'fa-arrow-up-right';
      const badgeClass = isCredit ? 'tx-credit' : 'tx-debit';
      const amountClass = isCredit ? 'credit' : 'debit';
      const prefix = isCredit ? '+₹' : '-₹';

      return `
        <div class="tx-item">
          <div class="tx-left">
            <div class="tx-icon-badge ${badgeClass}"><i class="fa-solid ${icon}"></i></div>
            <div class="tx-details">
              <span class="tx-title">${tx.description || tx.taskTitle || 'Wallet Transaction'}</span>
              <span class="tx-date">${formatDate(tx.timestamp)}</span>
            </div>
          </div>
          <div class="tx-amount ${amountClass}">${prefix}${tx.amount}</div>
        </div>
      `;
    }).join('');

  } catch (error) {
    listContainer.innerHTML = `<div style="padding:1rem; color:var(--danger);">Error loading wallet details.</div>`;
  }
}

// ============================================================================
// 6. PROFILE SCREEN
// ============================================================================
async function loadProfileData() {
  if (!State.currentUser) return;
  const user = State.currentUser;

  document.getElementById('profile-avatar').src = user.avatarUrl || `https://api.dicebear.com/7.x/avataaars/svg?seed=${user.name}`;
  document.getElementById('profile-name').innerHTML = `${user.name} <i class="fa-solid fa-circle-check verified-icon" title="Verified Profile"></i>`;
  document.getElementById('profile-rating').textContent = user.rating ? user.rating.toFixed(1) : '5.0';
  document.getElementById('profile-rating-count').textContent = user.ratingCount || 0;
  document.getElementById('profile-tasks-count').textContent = user.completedTasks || 0;
  document.getElementById('profile-location').textContent = user.location || 'Ongole';
  document.getElementById('profile-bio').textContent = user.bio || 'Local task enthusiast and worker.';

  // Render skills tags
  const skillsContainer = document.getElementById('profile-skills-list');
  if (skillsContainer) {
    if (user.skills && user.skills.length > 0) {
      skillsContainer.innerHTML = user.skills.map(s => `<span class="skill-tag">${s}</span>`).join('');
    } else {
      skillsContainer.innerHTML = `<span style="font-size:0.8rem; color:var(--text-muted);">No specific skills added.</span>`;
    }
  }

  // Load reviews received
  const reviewsContainer = document.getElementById('profile-reviews-list');
  if (reviewsContainer) {
    try {
      const reviews = await API.getReviewsForUser(user.id);
      if (!reviews || reviews.length === 0) {
        reviewsContainer.innerHTML = `<p style="color:var(--text-muted); font-size:0.88rem;">No reviews received yet. Complete tasks to build your reputation!</p>`;
      } else {
        reviewsContainer.innerHTML = reviews.map(r => `
          <div class="review-item">
            <div class="review-header">
              <span class="review-author">${r.fromUserName} <span style="font-size:0.75rem; color:var(--text-light); font-weight:normal;">• ${r.taskTitle}</span></span>
              <span class="review-stars">${'★'.repeat(Math.round(r.rating))}</span>
            </div>
            <p class="review-text">"${r.reviewText || 'Excellent experience!'}"</p>
          </div>
        `).join('');
      }
    } catch (e) {
      reviewsContainer.innerHTML = `<p style="color:var(--danger);">Error loading reviews.</p>`;
    }
  }
}

// ============================================================================
// MODAL DIALOGS
// ============================================================================
async function openTaskDetails(taskId) {
  currentModalTaskId = taskId;
  try {
    const task = await API.getTaskById(taskId);
    if (!task) return;

    document.getElementById('modal-task-category').textContent = task.category;
    document.getElementById('modal-task-title').textContent = task.title;
    document.getElementById('modal-task-reward').textContent = `₹${task.reward}`;
    document.getElementById('modal-task-status').textContent = task.status;
    document.getElementById('modal-task-duration').textContent = task.duration;
    document.getElementById('modal-task-schedule').textContent = `${task.date} • ${task.startTime || 'Flexible'} – ${task.endTime || 'Flexible'}`;
    document.getElementById('modal-task-location').textContent = task.location;
    document.getElementById('modal-task-distance').textContent = `${task.distanceKm || '0.8'} km away`;
    document.getElementById('modal-task-desc').textContent = task.description;

    document.getElementById('modal-poster-avatar').src = `https://api.dicebear.com/7.x/avataaars/svg?seed=${task.posterName.replaceAll(' ', '')}`;
    document.getElementById('modal-poster-name').innerHTML = `${task.posterName} <i class="fa-solid fa-circle-check verified-icon"></i>`;
    document.getElementById('modal-poster-rating').textContent = task.posterRating || '5.0';
    document.getElementById('modal-poster-tasks').textContent = task.posterCompletedTasks || '0';

    const skillsContainer = document.getElementById('modal-task-skills');
    if (task.requiredSkills && task.requiredSkills.length > 0) {
      skillsContainer.innerHTML = task.requiredSkills.map(s => `<span class="skill-tag">${s}</span>`).join('');
    } else {
      skillsContainer.innerHTML = `<span style="font-size:0.8rem; color:var(--text-muted);">General work (no specialized skills required)</span>`;
    }

    const acceptBtn = document.getElementById('btn-accept-task');
    const acceptReward = document.getElementById('btn-accept-reward');
    if (acceptReward) acceptReward.textContent = task.reward;

    currentOpenedTaskId = taskId;
    currentOpenedPosterId = task.posterId;
    const blockBtn = document.getElementById('btn-block-poster');
    if (blockBtn) {
      if (State.currentUser && State.currentUser.id === task.posterId) {
        blockBtn.style.display = 'none';
      } else {
        blockBtn.style.display = 'inline-flex';
      }
    }

    openModal('modal-task-details');
  } catch (error) {
    showToast('Failed to load task details', 'error');
  }
}

function handleAcceptTaskFromModal() {
  closeModal('modal-task-details');
  if (currentModalTaskId) {
    handleDirectAccept(currentModalTaskId, 400);
  }
}

// Persona Switcher Modal
function openPersonaModal() {
  const container = document.getElementById('persona-users-list');
  if (!container) return;

  container.innerHTML = State.allUsers.map(user => {
    const isCurrent = State.currentUser && State.currentUser.id === user.id;
    return `
      <div style="display:flex; align-items:center; justify-content:space-between; padding:0.75rem; border:1px solid ${isCurrent ? 'var(--primary)' : 'var(--border-color)'}; background:${isCurrent ? 'var(--primary-light)' : 'var(--bg-page)'}; border-radius:var(--radius-md);">
        <div style="display:flex; align-items:center; gap:0.75rem;">
          <img src="${user.avatarUrl || 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + user.name}" style="width:38px; height:38px; border-radius:50%;" alt="${user.name}">
          <div>
            <div style="font-size:0.92rem; font-weight:700; color:var(--secondary);">${user.name} ${isCurrent ? '<span style="color:var(--primary); font-size:0.75rem;">(Active)</span>' : ''}</div>
            <div style="font-size:0.78rem; color:var(--text-muted);">★ ${user.rating || 5.0} • Balance: ₹${user.walletBalance || 0} • ${user.location}</div>
          </div>
        </div>
        <button class="btn btn-sm ${isCurrent ? 'btn-primary' : 'btn-outline'}" onclick="switchPersona(${user.id})">
          ${isCurrent ? 'Selected' : 'Switch'}
        </button>
      </div>
    `;
  }).join('');

  openModal('modal-persona');
}

async function switchPersona(userId) {
  const target = State.allUsers.find(u => u.id === userId);
  if (target) {
    await State.setCurrentUser(target);
    closeModal('modal-persona');
    showToast(`Switched active persona to ${target.name}`, 'info');
  }
}

async function handleQuickRegister(e) {
  e.preventDefault();
  const name = document.getElementById('register-name').value.trim();
  const email = document.getElementById('register-email').value.trim();

  if (!name || !email) return;

  try {
    const newUser = await API.registerUser({
      name,
      email,
      phone: '+91 99999 88888',
      location: 'Ongole',
      latitude: 15.5057,
      longitude: 80.0499,
      skills: ['General Support'],
      bio: 'New user excited to find and post micro-tasks.'
    });

    const refreshedUsers = await API.getUsers();
    State.allUsers = refreshedUsers;
    await State.setCurrentUser(newUser);
    closeModal('modal-persona');
    showToast(`Welcome ${name}! Account registered successfully.`, 'success');
  } catch (error) {
    showToast('Failed to register user', 'error');
  }
}

// Rating & Review Modal
function openRatingModal(taskId, toUserId, toUserName, role) {
  currentRatingContext = { taskId, toUserId, role };
  document.getElementById('rating-modal-title').textContent = `Rate & Review ${toUserName}`;
  document.getElementById('rating-modal-subtitle').textContent = `How was your experience working with ${toUserName}?`;
  setRatingValue(5);
  document.getElementById('rating-review-text').value = '';
  openModal('modal-rating');
}

function setRatingValue(val) {
  selectedRating = val;
  const stars = document.querySelectorAll('#rating-stars-container .star');
  stars.forEach(star => {
    const starVal = parseInt(star.dataset.value);
    star.classList.toggle('active', starVal <= val);
  });

  const textLabel = document.getElementById('rating-text-value');
  const labels = {
    1: '1.0 — Poor Experience',
    2: '2.0 — Below Average',
    3: '3.0 — Satisfactory',
    4: '4.0 — Good & Reliable',
    5: '5.0 — Excellent & Highly Recommended!'
  };
  if (textLabel) textLabel.textContent = labels[val] || `${val}.0`;
}

async function submitRatingReview() {
  if (!currentRatingContext || !State.currentUser) return;
  const text = document.getElementById('rating-review-text').value.trim();

  const payload = {
    taskId: currentRatingContext.taskId,
    fromUserId: State.currentUser.id,
    toUserId: currentRatingContext.toUserId,
    rating: selectedRating,
    reviewText: text || 'Great experience working together on LocalJobs!',
    role: currentRatingContext.role
  };

  try {
    await API.submitRating(payload);
    closeModal('modal-rating');
    showToast(`⭐ Review submitted! Thank you for building trust.`, 'success');
    await State.refreshCurrentUser();
    loadMyTasksData();
  } catch (error) {
    showToast('Failed to submit rating', 'error');
  }
}

// Notifications Modal
async function openNotificationsModal() {
  const container = document.getElementById('notifications-list-container');
  if (!container || !State.currentUser) return;

  container.innerHTML = `<div style="text-align:center; padding:1.5rem; color:var(--text-muted);"><i class="fa-solid fa-spinner fa-spin"></i> Loading...</div>`;
  openModal('modal-notifications');

  try {
    const notifs = await API.getNotifications(State.currentUser.id);
    if (!notifs || notifs.length === 0) {
      container.innerHTML = `<p style="color:var(--text-muted); text-align:center; padding:1rem;">No notifications yet.</p>`;
      return;
    }

    container.innerHTML = notifs.map(n => `
      <div style="padding:0.75rem; border-radius:var(--radius-md); background:${n.isRead ? 'var(--bg-page)' : 'var(--primary-light)'}; border:1px solid var(--border-subtle);">
        <div style="font-size:0.88rem; font-weight:700; color:var(--secondary);">${n.title}</div>
        <div style="font-size:0.82rem; color:var(--text-muted); margin-top:2px;">${n.message}</div>
        <div style="font-size:0.7rem; color:var(--text-light); margin-top:4px;">${formatDate(n.createdAt)}</div>
      </div>
    `).join('');
  } catch (error) {
    container.innerHTML = `<p style="color:var(--danger); text-align:center;">Failed to load notifications.</p>`;
  }
}

async function markAllNotificationsRead() {
  if (!State.currentUser) return;
  try {
    await API.markNotificationsRead(State.currentUser.id);
    await State.refreshNotifications();
    openNotificationsModal();
    showToast('All notifications marked read', 'info');
  } catch (e) {
    console.error(e);
  }
}

// Modal generic open/close
function openModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.add('open');
}

function closeModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.remove('open');
}

// ============================================================================
// UTILITIES & TOASTS
// ============================================================================
function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  
  let icon = 'fa-circle-info';
  if (type === 'success') icon = 'fa-circle-check';
  if (type === 'error') icon = 'fa-triangle-exclamation';

  toast.innerHTML = `<i class="fa-solid ${icon}"></i> <span>${message}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

function formatDate(dateStr) {
  if (!dateStr) return 'Just now';
  try {
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-IN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (e) {
    return dateStr;
  }
}

// ============================================================================
// 7. ADMIN CONTROL PANEL
// ============================================================================
async function loadAdminData() {
  try {
    const [stats, users, tasks] = await Promise.all([
      API.getAdminStats(),
      API.getUsers(),
      API.getTasks({ status: '' }) // all statuses
    ]);

    // Update KPI cards
    document.getElementById('admin-total-users').textContent = stats.totalUsers || 0;
    document.getElementById('admin-total-tasks').textContent = stats.totalTasks || 0;
    document.getElementById('admin-open-tasks').textContent = stats.openTasks || 0;
    document.getElementById('admin-active-tasks').textContent = stats.inProgressTasks || 0;
    document.getElementById('admin-total-value').textContent = `₹${(stats.totalTaskValue || 0).toLocaleString('en-IN')}`;
    document.getElementById('admin-completed-tasks').textContent = stats.completedTasks || 0;

    // Render Users Table
    const usersBody = document.getElementById('admin-users-table-body');
// ============================================================================
// 7. IDENTITY VERIFICATION SCREEN (MOCK KYC)
// ============================================================================
let selectedKycDocType = 'AADHAAR';
let currentOpenedTaskId = null;
let currentOpenedPosterId = null;

function selectDocType(type) {
  selectedKycDocType = type;
  const choiceAadhaar = document.getElementById('doc-choice-aadhaar');
  const choicePan = document.getElementById('doc-choice-pan');
  const numLabel = document.getElementById('kyc-number-label');
  const docInput = document.getElementById('kyc-doc-number');
  const maskPreview = document.getElementById('kyc-mask-preview');

  if (type === 'AADHAAR') {
    if (choiceAadhaar) { choiceAadhaar.style.borderColor = 'var(--primary)'; choiceAadhaar.style.background = '#f5f3ff'; }
    if (choicePan) { choicePan.style.borderColor = 'var(--border-color)'; choicePan.style.background = 'white'; }
    if (numLabel) numLabel.innerHTML = 'Aadhaar Card Number <span>*</span>';
    if (docInput) { docInput.placeholder = 'e.g. 5432 8765 1234'; docInput.maxLength = 16; }
    if (maskPreview) maskPreview.textContent = 'XXXX-XXXX-1234';
  } else {
    if (choiceAadhaar) { choiceAadhaar.style.borderColor = 'var(--border-color)'; choiceAadhaar.style.background = 'white'; }
    if (choicePan) { choicePan.style.borderColor = 'var(--primary)'; choicePan.style.background = '#f5f3ff'; }
    if (numLabel) numLabel.innerHTML = 'PAN Card Number <span>*</span>';
    if (docInput) { docInput.placeholder = 'e.g. ABCDE1234F'; docInput.maxLength = 10; }
    if (maskPreview) maskPreview.textContent = 'XXXXX1234X';
  }
}

function handleKycFileSelect(event) {
  const file = event.target.files[0];
  const nameDisplay = document.getElementById('kyc-file-name');
  if (file && nameDisplay) {
    nameDisplay.textContent = `✓ Selected: ${file.name} (${Math.round(file.size / 1024)} KB)`;
  }
}

async function loadVerificationScreenData() {
  if (!State.currentUser) {
    openAuthModal('login');
    showToast('Please sign in to view identity verification', 'info');
    return;
  }

  const user = State.currentUser;
  const statusBanner = document.getElementById('verify-status-banner');
  const statusIconBox = document.getElementById('verify-status-icon-box');
  const statusTitle = document.getElementById('verify-status-title');
  const statusBadge = document.getElementById('verify-status-badge');
  const summaryBox = document.getElementById('verify-details-summary');
  const summaryDoc = document.getElementById('verify-summary-doc');
  const summaryMasked = document.getElementById('verify-summary-masked');
  const summaryName = document.getElementById('verify-summary-name');
  const remarksBox = document.getElementById('verify-remarks-box');
  const formContainer = document.getElementById('verify-form-container');

  const status = user.verificationStatus || (user.verified ? 'VERIFIED' : 'NOT_VERIFIED');

  if (summaryDoc) summaryDoc.textContent = user.verificationDocType || 'AADHAAR';
  if (summaryMasked) summaryMasked.textContent = user.maskedDocNumber || 'XXXX-XXXX-XXXX';
  if (summaryName) summaryName.textContent = user.nameOnDoc || user.name;

  if (status === 'VERIFIED') {
    if (statusIconBox) { statusIconBox.style.background = '#d1fae5'; statusIconBox.style.color = '#059669'; statusIconBox.innerHTML = '<i class="fa-solid fa-circle-check"></i>'; }
    if (statusTitle) statusTitle.textContent = 'Identity Verified (✓ Verified User)';
    if (statusBadge) { statusBadge.className = 'task-status-badge status-payment_released'; statusBadge.innerHTML = '<i class="fa-solid fa-circle-check"></i> VERIFIED'; }
    if (summaryBox) summaryBox.style.display = 'block';
    if (remarksBox) remarksBox.innerHTML = `<span style="color:#059669; font-weight:700;"><i class="fa-solid fa-shield-check"></i> You have full access to post tasks, accept tasks, and receive wallet payouts.</span>`;
    if (formContainer) formContainer.style.display = 'none';
  } else if (status === 'VERIFICATION_PENDING') {
    if (statusIconBox) { statusIconBox.style.background = '#fef3c7'; statusIconBox.style.color = '#b45309'; statusIconBox.innerHTML = '<i class="fa-solid fa-hourglass-half"></i>'; }
    if (statusTitle) statusTitle.textContent = 'Verification Under Review';
    if (statusBadge) { statusBadge.className = 'task-status-badge status-in_progress'; statusBadge.style.background = '#fef3c7'; statusBadge.style.color = '#b45309'; statusBadge.textContent = 'PENDING REVIEW'; }
    if (summaryBox) summaryBox.style.display = 'block';
    if (remarksBox) remarksBox.innerHTML = `<span style="color:#b45309;"><i class="fa-solid fa-clock"></i> Submitted ${user.verificationSubmittedAt ? formatDate(user.verificationSubmittedAt) : 'recently'}. Administrator review is in progress.</span>`;
    if (formContainer) formContainer.style.display = 'none';
  } else if (status === 'REJECTED') {
    if (statusIconBox) { statusIconBox.style.background = '#fee2e2'; statusIconBox.style.color = '#b91c1c'; statusIconBox.innerHTML = '<i class="fa-solid fa-triangle-exclamation"></i>'; }
    if (statusTitle) statusTitle.textContent = 'Verification Rejected';
    if (statusBadge) { statusBadge.className = 'task-status-badge status-in_progress'; statusBadge.style.background = '#fee2e2'; statusBadge.style.color = '#b91c1c'; statusBadge.textContent = 'REJECTED'; }
    if (summaryBox) summaryBox.style.display = 'block';
    if (remarksBox) remarksBox.innerHTML = `<span style="color:#b91c1c; font-weight:700;"><i class="fa-solid fa-circle-exclamation"></i> Reason: ${user.verificationRemarks || 'Document details did not match.'} Please resubmit below.</span>`;
    if (formContainer) formContainer.style.display = 'block';
  } else {
    if (statusIconBox) { statusIconBox.style.background = '#fee2e2'; statusIconBox.style.color = '#ef4444'; statusIconBox.innerHTML = '<i class="fa-solid fa-id-card"></i>'; }
    if (statusTitle) statusTitle.textContent = 'Identity Verification Required';
    if (statusBadge) { statusBadge.className = 'task-status-badge status-in_progress'; statusBadge.style.background = '#fee2e2'; statusBadge.style.color = '#b91c1c'; statusBadge.textContent = 'NOT VERIFIED'; }
    if (summaryBox) summaryBox.style.display = 'none';
    if (formContainer) formContainer.style.display = 'block';
  }
}

async function handleVerificationSubmit(event) {
  event.preventDefault();
  if (!State.currentUser) return;

  const nameOnDoc = document.getElementById('kyc-name-on-doc').value.trim();
  const docNumber = document.getElementById('kyc-doc-number').value.trim();

  if (!nameOnDoc || !docNumber) {
    showToast('Please fill in all document verification fields.', 'error');
    return;
  }

  try {
    const res = await API.submitVerification({
      userId: State.currentUser.id,
      docType: selectedKycDocType,
      docNumber,
      nameOnDoc
    });

    showToast('Identity verification submitted! Review is pending.', 'success');
    State.currentUser.verificationStatus = 'VERIFICATION_PENDING';
    State.currentUser.verificationDocType = selectedKycDocType;
    State.currentUser.maskedDocNumber = res.maskedDocNumber;
    State.currentUser.nameOnDoc = nameOnDoc;
    loadVerificationScreenData();
    renderHeaderProfile();
  } catch (error) {
    showToast(error.message || 'Failed to submit verification.', 'error');
  }
}

// ============================================================================
// 8. REGISTRATION VALIDATION & AUTHENTICATION
// ============================================================================
let usernameCheckTimeout = null;

function handleUsernameInput(event) {
  const username = event.target.value.trim().toLowerCase();
  const feedback = document.getElementById('username-feedback');
  if (!feedback) return;

  clearTimeout(usernameCheckTimeout);

  if (username.length < 3) {
    feedback.textContent = 'Username must be at least 3 characters.';
    feedback.style.color = 'var(--text-muted)';
    return;
  }

  usernameCheckTimeout = setTimeout(async () => {
    try {
      const res = await API.checkUsername(username);
      if (res.available) {
        feedback.innerHTML = `<i class="fa-solid fa-circle-check text-success"></i> <span style="color:#059669;">Username @${username} is available</span>`;
      } else {
        feedback.innerHTML = `<i class="fa-solid fa-circle-xmark" style="color:#ef4444;"></i> <span style="color:#ef4444;">Username already exists. Please choose another username.</span>`;
      }
    } catch (e) {
      feedback.textContent = '';
    }
  }, 350);
}

function checkPasswordStrength(event) {
  const pass = event.target.value;
  const matchFeedback = document.getElementById('password-match-feedback');
  if (!matchFeedback) return;

  if (pass.length < 6) {
    matchFeedback.textContent = 'Password must be at least 6 characters.';
    matchFeedback.style.color = '#ef4444';
  } else {
    matchFeedback.textContent = 'Password strength: Good ✓';
    matchFeedback.style.color = '#059669';
  }
  checkPasswordMatch();
}

function checkPasswordMatch() {
  const pass = document.getElementById('reg-password')?.value;
  const confirm = document.getElementById('reg-confirm-password')?.value;
  const feedback = document.getElementById('password-match-feedback');
  if (!feedback || !confirm) return;

  if (pass !== confirm) {
    feedback.textContent = 'Passwords do not match.';
    feedback.style.color = '#ef4444';
  } else {
    feedback.textContent = 'Passwords match ✓';
    feedback.style.color = '#059669';
  }
}

async function handleRegisterSubmit(event) {
  event.preventDefault();
  const fullName = document.getElementById('reg-name').value.trim();
  const username = document.getElementById('reg-username').value.trim().toLowerCase();
  const email = document.getElementById('reg-email').value.trim().toLowerCase();
  const phone = document.getElementById('reg-phone').value.trim();
  const password = document.getElementById('reg-password').value;
  const confirmPassword = document.getElementById('reg-confirm-password').value;
  const location = document.getElementById('reg-location').value.trim();
  const skillsInput = document.getElementById('reg-skills').value.trim();

  if (password !== confirmPassword) {
    showToast('Passwords do not match. Please confirm your password.', 'error');
    return;
  }

  const skills = skillsInput ? skillsInput.split(',').map(s => s.trim()).filter(Boolean) : ['General Work'];

  const payload = {
    fullName,
    username,
    email,
    phone,
    password,
    confirmPassword,
    location,
    latitude: 15.5057 + (Math.random() - 0.5) * 0.02,
    longitude: 80.0499 + (Math.random() - 0.5) * 0.02,
    skills,
    bio: 'New verified member on LocalJobs ready for local micro-tasks.'
  };

  try {
    const user = await API.signupUser(payload);
    if (user && user.id) {
      const refreshedUsers = await API.getUsers();
      State.allUsers = refreshedUsers || [];
      await State.setCurrentUser(user);
      closeModal('modal-auth');
      showToast(`Account @${username} created! Please complete identity verification. 🎉`, 'success');
      navigateTo('verify');
    }
  } catch (error) {
    showToast(error.message || 'Registration failed.', 'error');
  }
}

async function handleLoginSubmit(event) {
  event.preventDefault();
  const identifier = document.getElementById('login-identifier').value.trim();
  const password = document.getElementById('login-password').value;

  try {
    const user = await API.loginUser(identifier, password);
    if (user && user.id) {
      await State.setCurrentUser(user);
      closeModal('modal-auth');
      showToast(`Welcome back, ${user.name}! 👋`, 'success');
      navigateTo('home');
    }
  } catch (error) {
    showToast(error.message || 'Login failed. Check credentials.', 'error');
  }
}

async function quickDemoLogin(email) {
  try {
    let user = State.allUsers.find(u => u.email.toLowerCase() === email.toLowerCase());
    if (!user) {
      user = await API.loginUser(email, 'password123');
    }
    if (user) {
      await State.setCurrentUser(user);
      closeModal('modal-auth');
      showToast(`Logged in as ${user.name} ⚡`, 'success');
      navigateTo('home');
    }
  } catch (error) {
    showToast('Failed to log in with demo persona', 'error');
  }
}

function handleLogout() {
  State.logout();
  showToast('You have been signed out.', 'info');
  navigateTo('landing');
}

// ============================================================================
// 9. INCIDENT REPORTING & USER BLOCKING
// ============================================================================
function openReportModal(type, targetId) {
  const typeInput = document.getElementById('report-type-input');
  const targetIdInput = document.getElementById('report-target-id-input');
  const desc = document.getElementById('report-target-desc');

  if (typeInput) typeInput.value = type === 'TASK' ? 'TASK_REPORT' : 'USER_REPORT';
  if (targetIdInput) targetIdInput.value = targetId || '';
  if (desc) {
    desc.textContent = type === 'TASK'
      ? `Reporting Task #${targetId}. Our moderation team investigates all complaints.`
      : `Reporting User ID #${targetId}. Reports remain strictly confidential.`;
  }
  openModal('modal-report');
}

async function handleReportSubmit(event) {
  event.preventDefault();
  if (!State.currentUser) {
    showToast('Please sign in to file a report.', 'error');
    return;
  }

  const reportType = document.getElementById('report-type-input').value;
  const targetId = document.getElementById('report-target-id-input').value;
  const reason = document.getElementById('report-reason-select').value;
  const description = document.getElementById('report-description-input').value.trim();

  const payload = {
    reporterUserId: State.currentUser.id,
    reportedUserId: reportType === 'USER_REPORT' ? targetId : null,
    reportedTaskId: reportType === 'TASK_REPORT' ? targetId : null,
    reportType,
    reason,
    description
  };

  try {
    await API.fileReport(payload);
    closeModal('modal-report');
    showToast('Incident report filed. Platform moderators have been notified. 🛡️', 'success');
    document.getElementById('report-submission-form').reset();
  } catch (error) {
    showToast('Failed to submit report', 'error');
  }
}

async function handleBlockPosterFromModal() {
  if (!State.currentUser) return;
  if (!currentOpenedTaskId) return;

  try {
    const task = await API.getTaskById(currentOpenedTaskId);
    if (!task) return;

    if (confirm(`Are you sure you want to block ${task.posterName}? You will not see their tasks or receive interactions from them.`)) {
      await API.blockUser(State.currentUser.id, task.posterId);
      closeModal('modal-task-details');
      showToast(`User ${task.posterName} has been blocked.`, 'info');
      await State.refreshCurrentUser();
      loadFindWorkData();
    }
  } catch (e) {
    showToast('Error blocking user', 'error');
  }
}

// Landing tasks preview
async function loadLandingPageData() {
  const container = document.getElementById('landing-tasks-preview-grid');
  if (!container) return;

  try {
    const tasks = await API.getTasks({ status: 'OPEN' });
    const previewTasks = (tasks || []).slice(0, 4);

    if (previewTasks.length === 0) {
      container.innerHTML = `
        <div class="empty-state" style="grid-column: 1 / -1;">
          <i class="fa-solid fa-briefcase empty-icon"></i>
          <div class="empty-title">Fresh Tasks Coming Soon</div>
          <div class="empty-desc">New micro-tasks are posted regularly by nearby businesses and neighbors.</div>
        </div>
      `;
      return;
    }

    container.innerHTML = previewTasks.map(t => `
      <div class="task-card">
        <div>
          <div class="task-card-header">
            <span class="task-category-tag">${t.category}</span>
            <span class="task-reward-badge">₹${t.reward}</span>
          </div>

          <h3 class="task-title">${t.title}</h3>
          <p class="task-desc-clamp">${t.description || 'Flexible local micro-task in your neighborhood.'}</p>

          <div class="task-meta-list">
            <div class="task-meta-item"><i class="fa-solid fa-clock"></i> <span>${t.duration}</span></div>
            <div class="task-meta-item"><i class="fa-solid fa-calendar-day"></i> <span>${t.scheduleText || 'Today'}</span></div>
            <div class="task-meta-item"><i class="fa-solid fa-location-dot"></i> <span>${t.location}</span></div>
          </div>
        </div>

        <div style="display:flex; justify-content:space-between; align-items:center; margin-top:1rem; padding-top:0.75rem; border-top:1px solid var(--border-subtle);">
          <div class="poster-mini-info">
            <img src="${t.posterAvatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + t.posterName}" class="poster-mini-avatar" alt="${t.posterName}">
            <div>
              <div class="poster-mini-name">${t.posterName} <span class="task-status-badge status-payment_released" style="font-size:0.6rem; padding:1px 4px;"><i class="fa-solid fa-circle-check"></i> Verified</span></div>
              <div class="poster-mini-rating"><i class="fa-solid fa-star"></i> ${t.posterRating || '5.0'}</div>
            </div>
          </div>
          <button class="btn btn-primary btn-sm" onclick="handleLandingTaskClick(${t.id})">
            Accept & Earn
          </button>
        </div>
      </div>
    `).join('');
  } catch (error) {
    console.error('Failed to load landing tasks preview:', error);
  }
}

function handleLandingTaskClick(taskId) {
  if (!State.currentUser) {
    openAuthModal('login');
    showToast('Please sign in or create an account to accept tasks', 'info');
  } else {
    openTaskDetails(taskId);
  }
}

function openAuthModal(tab = 'login') {
  switchAuthTab(tab);
  openModal('modal-auth');
}

function switchAuthTab(tab) {
  const isLogin = tab === 'login';
  const tabLogin = document.getElementById('tab-auth-login');
  const tabReg = document.getElementById('tab-auth-register');
  const viewLogin = document.getElementById('auth-login-view');
  const viewReg = document.getElementById('auth-register-view');
  const title = document.getElementById('auth-modal-title');

  if (tabLogin) tabLogin.classList.toggle('active', isLogin);
  if (tabReg) tabReg.classList.toggle('active', !isLogin);
  if (viewLogin) viewLogin.style.display = isLogin ? 'block' : 'none';
  if (viewReg) viewReg.style.display = isLogin ? 'none' : 'block';
  if (title) title.textContent = isLogin ? 'Sign In to LocalJobs' : 'Create Dual-Role Account';
}



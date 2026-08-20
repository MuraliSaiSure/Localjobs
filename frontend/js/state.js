/**
 * LocalJobs — Central Reactive State
 */

const State = {
  currentUser: null,
  allUsers: [],
  activeScreen: 'home',
  activeMyTasksTab: 'posted',
  filters: {
    category: 'All',
    maxDistance: null,
    minReward: null,
    sortByReward: null,
    duration: 'All',
    keyword: ''
  },
  notifications: [],
  unreadNotifsCount: 0,
  
  listeners: [],

  subscribe(listener) {
    this.listeners.push(listener);
  },

  notify(changeType, payload) {
    this.listeners.forEach(fn => fn(changeType, payload));
  },

  async setCurrentUser(user) {
    this.currentUser = user;
    localStorage.setItem('localjobs_user_id', user.id);
    this.notify('USER_CHANGED', user);
    await this.refreshNotifications();
  },

  async refreshCurrentUser() {
    if (this.currentUser) {
      const refreshed = await API.getUserById(this.currentUser.id);
      this.currentUser = refreshed;
      this.notify('USER_UPDATED', refreshed);
    }
  },

  setScreen(screenName) {
    this.activeScreen = screenName;
    this.notify('SCREEN_CHANGED', screenName);
  },

  setMyTasksTab(tabName) {
    this.activeMyTasksTab = tabName;
    this.notify('MY_TASKS_TAB_CHANGED', tabName);
  },

  setFilters(newFilters) {
    this.filters = { ...this.filters, ...newFilters };
    this.notify('FILTERS_CHANGED', this.filters);
  },

  async refreshNotifications() {
    if (!this.currentUser) return;
    try {
      const notifs = await API.getNotifications(this.currentUser.id);
      this.notifications = notifs || [];
      this.unreadNotifsCount = this.notifications.filter(n => !n.isRead).length;
      this.notify('NOTIFICATIONS_UPDATED', {
        notifications: this.notifications,
        unreadCount: this.unreadNotifsCount
      });
    } catch (e) {
      console.warn('Could not fetch notifications:', e);
    }
  }
};

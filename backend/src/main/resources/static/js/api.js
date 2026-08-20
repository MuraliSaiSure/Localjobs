/**
 * LocalJobs — API Client
 * Connects frontend views to Spring Boot REST endpoints.
 */

// Dynamic API Base URL detection (supports standalone Live Server, file://, Spring Boot 8080, and Vercel deployments)
const getApiBase = () => {
  if (window.LOCALJOBS_API_URL) return window.LOCALJOBS_API_URL;
  const stored = localStorage.getItem('localjobs_api_url');
  if (stored) return stored;
  // If running on embedded Spring Boot port 8080
  if (window.location.port === '8080') return '';
  // If running on a standalone dev server or local file
  if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1' || window.location.protocol === 'file:') {
    return 'http://localhost:8080';
  }
  return '';
};

const API_BASE = getApiBase();

const API = {
  // Users
  async getUsers() {
    const res = await fetch(`${API_BASE}/api/users`);
    return res.json();
  },

  async getUserById(id) {
    const res = await fetch(`${API_BASE}/api/users/${id}`);
    return res.json();
  },

  async registerUser(userData) {
    const res = await fetch(`${API_BASE}/api/users/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(userData)
    });
    return res.json();
  },

  async loginUser(emailOrPhone, password) {
    const res = await fetch(`${API_BASE}/api/users/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ emailOrPhone, password })
    });
    if (!res.ok) {
      throw new Error('Invalid email or password');
    }
    return res.json();
  },

  async updateUser(id, userData) {
    const res = await fetch(`${API_BASE}/api/users/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(userData)
    });
    return res.json();
  },

  // Tasks
  async getTasks(params = {}) {
    const query = new URLSearchParams();
    if (params.category && params.category !== 'All') query.append('category', params.category);
    if (params.maxDistance) query.append('maxDistance', params.maxDistance);
    if (params.minReward) query.append('minReward', params.minReward);
    if (params.sortByReward) query.append('sortByReward', params.sortByReward);
    if (params.duration && params.duration !== 'All') query.append('duration', params.duration);
    if (params.status) query.append('status', params.status);
    if (params.keyword) query.append('keyword', params.keyword);
    if (params.userLat) query.append('userLat', params.userLat);
    if (params.userLng) query.append('userLng', params.userLng);

    const url = `${API_BASE}/api/tasks${query.toString() ? '?' + query.toString() : ''}`;
    const res = await fetch(url);
    return res.json();
  },

  async getTaskById(id) {
    const res = await fetch(`${API_BASE}/api/tasks/${id}`);
    return res.json();
  },

  async createTask(taskData) {
    const res = await fetch(`${API_BASE}/api/tasks`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(taskData)
    });
    return res.json();
  },

  async acceptTask(taskId, workerId) {
    const res = await fetch(`${API_BASE}/api/tasks/${taskId}/accept?workerId=${workerId}`, {
      method: 'PUT'
    });
    return res.json();
  },

  async startTask(taskId, workerId) {
    const res = await fetch(`${API_BASE}/api/tasks/${taskId}/start?workerId=${workerId}`, {
      method: 'PUT'
    });
    return res.json();
  },

  async completeTask(taskId, workerId) {
    const res = await fetch(`${API_BASE}/api/tasks/${taskId}/complete?workerId=${workerId}`, {
      method: 'PUT'
    });
    return res.json();
  },

  async releasePayment(taskId, posterId) {
    const res = await fetch(`${API_BASE}/api/tasks/${taskId}/release-payment?posterId=${posterId}`, {
      method: 'PUT'
    });
    return res.json();
  },

  async getMyPostedTasks(userId, status) {
    let url = `${API_BASE}/api/tasks/my-posted?userId=${userId}`;
    if (status) url += `&status=${status}`;
    const res = await fetch(url);
    return res.json();
  },

  async getMyAcceptedTasks(workerId, status) {
    let url = `${API_BASE}/api/tasks/my-accepted?workerId=${workerId}`;
    if (status) url += `&status=${status}`;
    const res = await fetch(url);
    return res.json();
  },

  // Wallet
  async getWalletSummary(userId) {
    const res = await fetch(`${API_BASE}/api/wallet/${userId}`);
    return res.json();
  },

  async getTransactions(userId) {
    const res = await fetch(`${API_BASE}/api/wallet/${userId}/transactions`);
    return res.json();
  },

  // Ratings
  async submitRating(ratingData) {
    const res = await fetch(`${API_BASE}/api/ratings`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(ratingData)
    });
    return res.json();
  },

  async getReviewsForUser(userId) {
    const res = await fetch(`${API_BASE}/api/ratings/user/${userId}`);
    return res.json();
  },

  // Notifications
  async getNotifications(userId) {
    const res = await fetch(`${API_BASE}/api/notifications?userId=${userId}`);
    return res.json();
  },

  async markNotificationsRead(userId) {
    const res = await fetch(`${API_BASE}/api/notifications/mark-read?userId=${userId}`, {
      method: 'PUT'
    });
    return res.json();
  },

  // Admin
  async getAdminStats() {
    const res = await fetch(`${API_BASE}/api/admin/stats`);
    return res.json();
  },

  async deleteAdminTask(taskId) {
    const res = await fetch(`${API_BASE}/api/admin/tasks/${taskId}`, {
      method: 'DELETE'
    });
    return res.json();
  },

  async toggleUserVerify(userId) {
    const res = await fetch(`${API_BASE}/api/admin/users/${userId}/toggle-verify`, {
      method: 'PUT'
    });
    return res.json();
  }
};

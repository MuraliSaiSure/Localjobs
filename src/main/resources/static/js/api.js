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

const getAdminHeaders = () => {
  const token = localStorage.getItem('localjobs_admin_token') || 'ADMIN_SESSION_ACTIVE';
  return {
    'Content-Type': 'application/json',
    'X-Admin-Role': 'ADMIN',
    'Authorization': `Bearer ${token}`
  };
};

const API = {
  // ==========================================================================
  // AUTHENTICATION & USERS
  // ==========================================================================
  async checkUsername(username) {
    const res = await fetch(`${API_BASE}/api/auth/check-username?username=${encodeURIComponent(username)}`);
    return res.json();
  },

  async signupUser(signupData) {
    const res = await fetch(`${API_BASE}/api/auth/signup`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(signupData)
    });
    const data = await res.json();
    if (!res.ok) {
      throw new Error(data.error || 'Signup failed');
    }
    return data;
  },

  async loginUser(usernameOrEmail, password) {
    const res = await fetch(`${API_BASE}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ usernameOrEmail, password })
    });
    const data = await res.json();
    if (!res.ok) {
      throw new Error(data.error || 'Invalid credentials');
    }
    return data;
  },

  async adminLogin(username, password) {
    const res = await fetch(`${API_BASE}/api/auth/admin/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    const data = await res.json();
    if (!res.ok) {
      throw new Error(data.error || 'Admin authentication failed');
    }
    return data;
  },

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

  async updateUser(id, userData) {
    const res = await fetch(`${API_BASE}/api/users/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(userData)
    });
    return res.json();
  },

  // ==========================================================================
  // IDENTITY VERIFICATION (MOCK KYC)
  // ==========================================================================
  async submitVerification(payload) {
    const res = await fetch(`${API_BASE}/api/verification/submit`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (!res.ok) {
      throw new Error(data.error || 'Verification submission failed');
    }
    return data;
  },

  // ==========================================================================
  // SECURITY, REPORTS & BLOCKING
  // ==========================================================================
  async fileReport(reportData) {
    const res = await fetch(`${API_BASE}/api/security/report`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(reportData)
    });
    const data = await res.json();
    if (!res.ok) {
      throw new Error(data.error || 'Failed to file report');
    }
    return data;
  },

  async blockUser(userId, targetUserId) {
    const res = await fetch(`${API_BASE}/api/security/block`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId, targetUserId })
    });
    return res.json();
  },

  async unblockUser(userId, targetUserId) {
    const res = await fetch(`${API_BASE}/api/security/unblock`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId, targetUserId })
    });
    return res.json();
  },

  // ==========================================================================
  // TASKS & REPUTATION
  // ==========================================================================
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
    const data = await res.json();
    if (!res.ok) {
      throw new Error(data.message || data.error || 'Failed to publish task');
    }
    return data;
  },

  async acceptTask(taskId, workerId) {
    const res = await fetch(`${API_BASE}/api/tasks/${taskId}/accept?workerId=${workerId}`, {
      method: 'PUT'
    });
    const data = await res.json();
    if (!res.ok) {
      throw new Error(data.message || data.error || 'Failed to accept task');
    }
    return data;
  },

  async startTask(taskId) {
    const res = await fetch(`${API_BASE}/api/tasks/${taskId}/start`, { method: 'PUT' });
    return res.json();
  },

  async completeTask(taskId) {
    const res = await fetch(`${API_BASE}/api/tasks/${taskId}/complete`, { method: 'PUT' });
    return res.json();
  },

  async releasePayment(taskId) {
    const res = await fetch(`${API_BASE}/api/tasks/${taskId}/release-payment`, { method: 'PUT' });
    return res.json();
  },

  async getMyTasks(userId, role = 'all') {
    const res = await fetch(`${API_BASE}/api/tasks/user/${userId}?role=${role}`);
    return res.json();
  },

  // Wallet
  async getWallet(userId) {
    const res = await fetch(`${API_BASE}/api/wallet/${userId}`);
    return res.json();
  },

  async getTransactions(userId) {
    const res = await fetch(`${API_BASE}/api/wallet/${userId}/transactions`);
    return res.json();
  },

  async addFunds(userId, amount) {
    const res = await fetch(`${API_BASE}/api/wallet/${userId}/deposit?amount=${amount}`, { method: 'POST' });
    return res.json();
  },

  // Reviews
  async submitReview(reviewData) {
    const res = await fetch(`${API_BASE}/api/reviews`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(reviewData)
    });
    return res.json();
  },

  async getUserReviews(userId) {
    const res = await fetch(`${API_BASE}/api/reviews/user/${userId}`);
    return res.json();
  },

  // Notifications
  async getNotifications(userId) {
    const res = await fetch(`${API_BASE}/api/notifications/user/${userId}`);
    return res.json();
  },

  async markNotificationRead(id) {
    const res = await fetch(`${API_BASE}/api/notifications/${id}/read`, { method: 'PUT' });
    return res.json();
  },

  async markAllNotificationsRead(userId) {
    const res = await fetch(`${API_BASE}/api/notifications/user/${userId}/read-all`, { method: 'PUT' });
    return res.json();
  },

  // ==========================================================================
  // ADMIN CONTROL PANEL (PROTECTED WITH RBAC)
  // ==========================================================================
  async getAdminStats() {
    const res = await fetch(`${API_BASE}/api/admin/stats`, {
      headers: getAdminHeaders()
    });
    if (!res.ok) throw new Error('Unauthorized admin access');
    return res.json();
  },

  async getPendingVerifications() {
    const res = await fetch(`${API_BASE}/api/admin/verifications/pending`, {
      headers: getAdminHeaders()
    });
    if (!res.ok) throw new Error('Unauthorized admin access');
    return res.json();
  },

  async reviewVerification(userId, decision, remarks) {
    const adminUsername = localStorage.getItem('localjobs_admin_username') || 'admin';
    const res = await fetch(`${API_BASE}/api/admin/verifications/review`, {
      method: 'POST',
      headers: getAdminHeaders(),
      body: JSON.stringify({ adminUsername, userId, decision, remarks })
    });
    return res.json();
  },

  async getAdminReports() {
    const res = await fetch(`${API_BASE}/api/admin/reports`, {
      headers: getAdminHeaders()
    });
    if (!res.ok) throw new Error('Unauthorized admin access');
    return res.json();
  },

  async resolveReport(reportId, decision, notes) {
    const adminUsername = localStorage.getItem('localjobs_admin_username') || 'admin';
    const res = await fetch(`${API_BASE}/api/admin/reports/${reportId}/resolve`, {
      method: 'POST',
      headers: getAdminHeaders(),
      body: JSON.stringify({ adminUsername, decision, notes })
    });
    return res.json();
  },

  async updateAccountStatus(userId, newStatus, reason) {
    const adminUsername = localStorage.getItem('localjobs_admin_username') || 'admin';
    const res = await fetch(`${API_BASE}/api/admin/users/${userId}/account-status`, {
      method: 'PUT',
      headers: getAdminHeaders(),
      body: JSON.stringify({ adminUsername, newStatus, reason })
    });
    return res.json();
  },

  async getAdminAuditLogs() {
    const res = await fetch(`${API_BASE}/api/admin/audit-logs`, {
      headers: getAdminHeaders()
    });
    if (!res.ok) throw new Error('Unauthorized admin access');
    return res.json();
  },

  async deleteAdminTask(taskId) {
    const res = await fetch(`${API_BASE}/api/admin/tasks/${taskId}`, {
      method: 'DELETE',
      headers: getAdminHeaders()
    });
    return res.json();
  }
};

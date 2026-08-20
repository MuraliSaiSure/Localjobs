/**
 * LocalJobs — Standalone Admin Portal Controller
 */

let currentAdminSession = null;

document.addEventListener('DOMContentLoaded', () => {
  initAdminPortal();
});

function initAdminPortal() {
  const token = localStorage.getItem('localjobs_admin_token');
  const adminName = localStorage.getItem('localjobs_admin_name');
  const adminUsername = localStorage.getItem('localjobs_admin_username');

  if (token) {
    currentAdminSession = { token, name: adminName || 'Admin', username: adminUsername || 'admin' };
    showAdminDashboard();
  } else {
    showAdminLogin();
  }
}

function showAdminLogin() {
  document.getElementById('admin-login-view').style.display = 'flex';
  document.getElementById('admin-app-view').style.display = 'none';
}

function showAdminDashboard() {
  document.getElementById('admin-login-view').style.display = 'none';
  document.getElementById('admin-app-view').style.display = 'flex';
  document.getElementById('admin-user-display').textContent = `@${currentAdminSession.username}`;
  loadAdminOverview();
}

async function handleAdminLogin(event) {
  event.preventDefault();
  const username = document.getElementById('admin-username-input').value.trim();
  const password = document.getElementById('admin-password-input').value;

  try {
    const res = await API.adminLogin(username, password);
    if (res && res.token) {
      localStorage.setItem('localjobs_admin_token', res.token);
      localStorage.setItem('localjobs_admin_name', res.name);
      localStorage.setItem('localjobs_admin_username', res.username);
      currentAdminSession = res;
      showToast(`Authenticated as ${res.name} (Admin)`, 'success');
      showAdminDashboard();
    } else {
      showToast('Admin authentication failed', 'error');
    }
  } catch (error) {
    showToast(error.message || 'Invalid admin credentials', 'error');
  }
}

function handleAdminLogout() {
  localStorage.removeItem('localjobs_admin_token');
  localStorage.removeItem('localjobs_admin_name');
  localStorage.removeItem('localjobs_admin_username');
  currentAdminSession = null;
  showToast('Logged out of Admin portal', 'info');
  showAdminLogin();
}

function switchAdminTab(tabName) {
  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.classList.toggle('active', btn.id === `tab-admin-${tabName}`);
  });

  document.querySelectorAll('.admin-tab-view').forEach(view => {
    view.style.display = 'none';
  });

  const activeView = document.getElementById(`admin-view-${tabName}`);
  if (activeView) {
    activeView.style.display = 'block';
  }

  switch (tabName) {
    case 'overview':
      loadAdminOverview();
      break;
    case 'verifications':
      loadAdminVerifications();
      break;
    case 'users':
      loadAdminUsers();
      break;
    case 'reports':
      loadAdminReports();
      break;
    case 'tasks':
      loadAdminTasksMod();
      break;
    case 'audit':
      loadAdminAuditLogs();
      break;
  }
}

// 1. Overview Loader
async function loadAdminOverview() {
  try {
    const stats = await API.getAdminStats();
    document.getElementById('stat-total-users').textContent = stats.totalUsers || 0;
    document.getElementById('stat-total-tasks').textContent = stats.totalTasks || 0;
    document.getElementById('stat-open-tasks').textContent = stats.openTasks || 0;
    document.getElementById('stat-active-tasks').textContent = stats.inProgressTasks || 0;
    document.getElementById('stat-pending-kyc').textContent = stats.pendingVerifications || 0;
    document.getElementById('stat-pending-reports').textContent = stats.pendingReports || 0;

    // Badges in tabs
    const verifBadge = document.getElementById('admin-pending-verif-badge');
    if (verifBadge) verifBadge.textContent = stats.pendingVerifications || 0;

    const reportsBadge = document.getElementById('admin-pending-reports-badge');
    if (reportsBadge) reportsBadge.textContent = stats.pendingReports || 0;
  } catch (e) {
    showToast('Failed to load platform stats', 'error');
  }
}

// 2. Verification Requests Loader
async function loadAdminVerifications() {
  try {
    const pendingUsers = await API.getPendingVerifications();
    const tbody = document.getElementById('admin-verifications-table-body');
    if (!tbody) return;

    if (!pendingUsers || pendingUsers.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="7" style="text-align:center; padding:2.5rem; color:var(--text-muted);">
            <i class="fa-solid fa-circle-check text-success" style="font-size:2rem; margin-bottom:0.5rem; display:block;"></i>
            All identity verification requests have been processed. Zero pending queues!
          </td>
        </tr>
      `;
      return;
    }

    tbody.innerHTML = pendingUsers.map(u => `
      <tr style="border-bottom: 1px solid var(--border-subtle);">
        <td style="padding:0.85rem 0.5rem; display:flex; align-items:center; gap:0.6rem;">
          <img src="${u.avatarUrl || 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + u.name}" style="width:34px; height:34px; border-radius:50%;" alt="${u.name}">
          <div>
            <strong style="display:block; color:var(--secondary);">${u.name}</strong>
            <span style="font-size:0.75rem; color:var(--text-muted);">@${u.username || 'user'}</span>
          </div>
        </td>
        <td style="padding:0.85rem 0.5rem;">
          <span class="task-category-tag" style="background:#eef2ff; color:#4f46e5; font-weight:700;">
            <i class="fa-solid fa-id-card"></i> ${u.verificationDocType || 'AADHAAR'}
          </span>
        </td>
        <td style="padding:0.85rem 0.5rem; font-family:monospace; font-weight:700; color:var(--secondary); font-size:0.92rem;">
          ${u.maskedDocNumber || 'XXXX-XXXX-XXXX'}
        </td>
        <td style="padding:0.85rem 0.5rem; font-weight:600;">${u.nameOnDoc || u.name}</td>
        <td style="padding:0.85rem 0.5rem; font-size:0.8rem; color:var(--text-muted);">${formatDate(u.verificationSubmittedAt)}</td>
        <td style="padding:0.85rem 0.5rem;">
          <span class="task-status-badge status-in_progress" style="background:#fee2e2; color:#b91c1c; font-size:0.72rem;">
            PENDING REVIEW
          </span>
        </td>
        <td style="padding:0.85rem 0.5rem; text-align:right;">
          <div style="display:flex; justify-content:flex-end; gap:0.4rem;">
            <button class="btn btn-sm btn-primary" style="background:#10b981; border-color:#059669; padding:0.35rem 0.75rem;" onclick="handleReviewVerification(${u.id}, 'APPROVED')">
              <i class="fa-solid fa-check"></i> Approve
            </button>
            <button class="btn btn-sm btn-outline" style="color:#ef4444; border-color:#fca5a5; padding:0.35rem 0.75rem;" onclick="handleReviewVerification(${u.id}, 'REJECTED')">
              <i class="fa-solid fa-xmark"></i> Reject
            </button>
            <button class="btn btn-sm btn-outline" style="color:#b45309; border-color:#fcd34d; padding:0.35rem 0.75rem;" onclick="handleReviewVerification(${u.id}, 'RE_VERIFY')">
              <i class="fa-solid fa-rotate-right"></i> Re-Verify
            </button>
          </div>
        </td>
      </tr>
    `).join('');
  } catch (e) {
    showToast('Error loading verification queue', 'error');
  }
}

async function handleReviewVerification(userId, decision) {
  let remarks = 'Approved by administrator';
  if (decision === 'REJECTED') {
    remarks = prompt('Enter rejection reason for the user:', 'Document image or details could not be matched.');
    if (!remarks) return;
  } else if (decision === 'RE_VERIFY') {
    remarks = prompt('Enter note requesting re-verification:', 'Please provide clearer document photo.');
    if (!remarks) return;
  }

  try {
    await API.reviewVerification(userId, decision, remarks);
    showToast(`Verification ${decision.toLowerCase()} successfully!`, 'success');
    loadAdminVerifications();
    loadAdminOverview();
  } catch (e) {
    showToast('Failed to submit verification review', 'error');
  }
}

// 3. User Management Loader
async function loadAdminUsers() {
  try {
    const users = await API.getUsers();
    const tbody = document.getElementById('admin-users-full-table-body');
    if (!tbody) return;

    tbody.innerHTML = (users || []).map(u => `
      <tr style="border-bottom: 1px solid var(--border-subtle);">
        <td style="padding:0.85rem 0.5rem; display:flex; align-items:center; gap:0.6rem;">
          <img src="${u.avatarUrl || 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + u.name}" style="width:32px; height:32px; border-radius:50%;" alt="${u.name}">
          <div>
            <strong style="display:block;">${u.name}</strong>
            <span style="font-size:0.75rem; color:var(--text-muted);">${u.location || 'Ongole'}</span>
          </div>
        </td>
        <td style="padding:0.85rem 0.5rem; font-weight:600; color:var(--primary);">@${u.username || 'user' + u.id}</td>
        <td style="padding:0.85rem 0.5rem; font-size:0.82rem; color:var(--text-muted);">${u.email}<br>${u.phone || ''}</td>
        <td style="padding:0.85rem 0.5rem;">
          ${u.verified || u.verificationStatus === 'VERIFIED'
            ? '<span class="task-status-badge status-payment_released"><i class="fa-solid fa-circle-check"></i> VERIFIED</span>'
            : '<span class="task-status-badge status-in_progress" style="background:#fee2e2; color:#b91c1c;">UNVERIFIED</span>'
          }
        </td>
        <td style="padding:0.85rem 0.5rem;">
          <span class="task-status-badge ${u.accountStatus === 'SUSPENDED' ? 'status-in_progress' : 'status-open'}" style="${u.accountStatus === 'SUSPENDED' ? 'background:#881337; color:#fda4af;' : ''}">
            ${u.accountStatus || 'ACTIVE'}
          </span>
        </td>
        <td style="padding:0.85rem 0.5rem; font-weight:700; color:var(--success);">₹${u.walletBalance || 0}</td>
        <td style="padding:0.85rem 0.5rem; text-align:right;">
          ${u.accountStatus === 'SUSPENDED'
            ? `<button class="btn btn-sm btn-primary" style="background:#10b981; border-color:#059669; padding:0.3rem 0.65rem;" onclick="handleToggleAccountStatus(${u.id}, 'ACTIVE')">
                <i class="fa-solid fa-unlock"></i> Reactivate
               </button>`
            : `<button class="btn btn-sm btn-outline" style="color:#ef4444; border-color:#fca5a5; padding:0.3rem 0.65rem;" onclick="handleToggleAccountStatus(${u.id}, 'SUSPENDED')">
                <i class="fa-solid fa-ban"></i> Suspend
               </button>`
          }
        </td>
      </tr>
    `).join('');
  } catch (e) {
    showToast('Failed to load user accounts', 'error');
  }
}

async function handleToggleAccountStatus(userId, newStatus) {
  let reason = 'Administrator moderation';
  if (newStatus === 'SUSPENDED') {
    reason = prompt('Enter suspension reason:', 'Violation of marketplace policy / suspicious behavior.');
    if (!reason) return;
  }

  try {
    await API.updateAccountStatus(userId, newStatus, reason);
    showToast(`User account status updated to ${newStatus}`, 'success');
    loadAdminUsers();
    loadAdminOverview();
  } catch (e) {
    showToast('Failed to update account status', 'error');
  }
}

// 4. Reports & Complaints Loader
async function loadAdminReports() {
  try {
    const reports = await API.getAdminReports();
    const tbody = document.getElementById('admin-reports-table-body');
    if (!tbody) return;

    if (!reports || reports.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="7" style="text-align:center; padding:2.5rem; color:var(--text-muted);">
            <i class="fa-solid fa-shield-check text-success" style="font-size:2rem; margin-bottom:0.5rem; display:block;"></i>
            No active incident reports. Marketplace is operating clean and safe!
          </td>
        </tr>
      `;
      return;
    }

    tbody.innerHTML = reports.map(r => `
      <tr style="border-bottom: 1px solid var(--border-subtle);">
        <td style="padding:0.85rem 0.5rem; font-weight:700; color:var(--text-muted);">#${r.id}</td>
        <td style="padding:0.85rem 0.5rem;"><span class="task-category-tag">${r.reportType}</span></td>
        <td style="padding:0.85rem 0.5rem; font-weight:600;">${r.reporterName} (ID #${r.reporterUserId})</td>
        <td style="padding:0.85rem 0.5rem;">
          ${r.reportedUserName ? `<strong>User:</strong> ${r.reportedUserName}<br>` : ''}
          ${r.reportedTaskTitle ? `<strong>Task:</strong> ${r.reportedTaskTitle}` : ''}
        </td>
        <td style="padding:0.85rem 0.5rem; font-size:0.84rem;">
          <strong style="color:#ef4444;">${r.reason}</strong>: ${r.description || 'No description provided.'}
        </td>
        <td style="padding:0.85rem 0.5rem;">
          <span class="task-status-badge ${r.status === 'PENDING' ? 'status-in_progress' : 'status-payment_released'}">
            ${r.status}
          </span>
        </td>
        <td style="padding:0.85rem 0.5rem; text-align:right;">
          ${r.status === 'PENDING'
            ? `<div style="display:flex; justify-content:flex-end; gap:0.35rem;">
                <button class="btn btn-sm btn-primary" style="background:#10b981; border-color:#059669; padding:0.3rem 0.6rem;" onclick="handleResolveReport(${r.id}, 'RESOLVED')">
                  <i class="fa-solid fa-check"></i> Resolve
                </button>
                <button class="btn btn-sm btn-outline" style="padding:0.3rem 0.6rem;" onclick="handleResolveReport(${r.id}, 'DISMISSED')">
                  Dismiss
                </button>
               </div>`
            : `<span style="font-size:0.75rem; color:var(--text-muted);">${r.resolutionNotes || 'Resolved'}</span>`
          }
        </td>
      </tr>
    `).join('');
  } catch (e) {
    showToast('Failed to load incident reports', 'error');
  }
}

async function handleResolveReport(reportId, decision) {
  const notes = prompt(`Enter resolution notes for report #${reportId}:`, 'Investigated and resolved by platform moderation.');
  if (!notes) return;

  try {
    await API.resolveReport(reportId, decision, notes);
    showToast(`Report #${reportId} marked as ${decision}`, 'success');
    loadAdminReports();
    loadAdminOverview();
  } catch (e) {
    showToast('Failed to resolve report', 'error');
  }
}

// 5. Tasks Moderation Loader
async function loadAdminTasksMod() {
  try {
    const tasks = await API.getTasks({ status: '' });
    const tbody = document.getElementById('admin-tasks-mod-table-body');
    if (!tbody) return;

    tbody.innerHTML = (tasks || []).map(t => `
      <tr style="border-bottom: 1px solid var(--border-subtle);">
        <td style="padding:0.85rem 0.5rem; font-weight:700; color:var(--text-muted);">#${t.id}</td>
        <td style="padding:0.85rem 0.5rem; font-weight:700; color:var(--secondary);">${t.title}</td>
        <td style="padding:0.85rem 0.5rem;"><span class="task-category-tag">${t.category}</span></td>
        <td style="padding:0.85rem 0.5rem; font-weight:800; color:var(--success);">₹${t.reward}</td>
        <td style="padding:0.85rem 0.5rem;">${t.posterName}</td>
        <td style="padding:0.85rem 0.5rem;"><span class="task-status-badge status-${t.status.toLowerCase()}">${t.status}</span></td>
        <td style="padding:0.85rem 0.5rem; text-align:right;">
          <button class="btn btn-outline btn-sm" style="color:var(--danger); border-color:#fca5a5; padding:0.28rem 0.65rem;" onclick="handleAdminDeleteTask(${t.id})">
            <i class="fa-solid fa-trash"></i> Delete
          </button>
        </td>
      </tr>
    `).join('');
  } catch (e) {
    showToast('Failed to load tasks for moderation', 'error');
  }
}

async function handleAdminDeleteTask(taskId) {
  if (confirm(`Are you sure you want to moderate and permanently delete Task #${taskId}?`)) {
    try {
      await API.deleteAdminTask(taskId);
      showToast(`Task #${taskId} deleted by Administrator`, 'info');
      loadAdminTasksMod();
      loadAdminOverview();
    } catch (e) {
      showToast('Error deleting task', 'error');
    }
  }
}

// 6. Security Audit Logs Loader
async function loadAdminAuditLogs() {
  try {
    const logs = await API.getAdminAuditLogs();
    const tbody = document.getElementById('admin-audit-table-body');
    if (!tbody) return;

    if (!logs || logs.length === 0) {
      tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; padding:2rem; color:var(--text-muted);">No audit logs recorded yet.</td></tr>`;
      return;
    }

    tbody.innerHTML = logs.map(l => `
      <tr style="border-bottom: 1px solid var(--border-subtle);">
        <td style="padding:0.85rem 0.5rem; font-size:0.8rem; color:var(--text-muted);">${formatDate(l.timestamp)}</td>
        <td style="padding:0.85rem 0.5rem; font-weight:700; color:var(--primary);">@${l.adminUsername}</td>
        <td style="padding:0.85rem 0.5rem;"><span class="task-category-tag" style="background:#f1f5f9; color:var(--secondary); font-weight:700;">${l.action}</span></td>
        <td style="padding:0.85rem 0.5rem; font-weight:600;">${l.targetType}: ${l.targetName || ('#' + l.targetId)}</td>
        <td style="padding:0.85rem 0.5rem; font-size:0.84rem; color:var(--text-muted);">${l.reason || 'N/A'}</td>
      </tr>
    `).join('');
  } catch (e) {
    showToast('Failed to load audit trail', 'error');
  }
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

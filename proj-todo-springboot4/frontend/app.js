'use strict';

const API = 'http://localhost:8002';

// ── State ──────────────────────────────────────────────────
let columns = [];       // [{id, name, color, position}]
let cards = [];         // [{id, title, description, column_id, priority, position, ...}]
let columnSortable = null;
let cardSortables = {};

// Modal state
let currentEditCardId = null;
let currentRenameColumnId = null;
let activeMenuColumnId = null;

// ── API helpers ─────────────────────────────────────────────
async function api(method, path, body) {
  const opts = {
    method,
    headers: { 'Content-Type': 'application/json' },
  };
  if (body !== undefined) opts.body = JSON.stringify(body);
  try {
    const res = await fetch(API + path, opts);
    if (!res.ok) {
      let msg = `HTTP ${res.status}`;
      try { const e = await res.json(); msg = e.detail || e.message || msg; } catch {}
      throw new Error(msg);
    }
    if (res.status === 204) return null;
    return res.json();
  } catch (err) {
    throw err;
  }
}

// ── Toast ───────────────────────────────────────────────────
function showToast(message, type = 'success') {
  const container = document.getElementById('toast-container');
  const el = document.createElement('div');
  el.className = `toast toast-${type}`;

  const iconSvg = type === 'success'
    ? `<svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#10b981" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="8" cy="8" r="6"/><polyline points="5,8 7,10.5 11,6"/></svg>`
    : `<svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#ef4444" stroke-width="2.5" stroke-linecap="round"><circle cx="8" cy="8" r="6"/><line x1="8" y1="5" x2="8" y2="8.5"/><circle cx="8" cy="11" r="0.5" fill="#ef4444"/></svg>`;

  el.innerHTML = `${iconSvg}<span>${message}</span>`;
  container.appendChild(el);

  setTimeout(() => {
    el.classList.add('removing');
    el.addEventListener('animationend', () => el.remove());
  }, 3000);
}

// ── Loading overlay ──────────────────────────────────────────
function hideLoading() {
  const el = document.getElementById('loading-overlay');
  el.classList.add('hidden');
  setTimeout(() => el.style.display = 'none', 400);
}

// ── Render ──────────────────────────────────────────────────
function getCardsForColumn(columnId) {
  return cards
    .filter(c => c.column_id === columnId)
    .sort((a, b) => a.position - b.position);
}

function formatDate(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

function priorityBadgeHTML(priority) {
  const p = priority || 'low';
  const labels = { low: 'Low', medium: 'Medium', high: 'High', critical: 'Critical' };
  return `<span class="priority-badge priority-${p}"><span class="priority-dot"></span>${labels[p] || p}</span>`;
}

function cardHTML(card) {
  const desc = card.description
    ? `<div class="card-description">${escapeHtml(card.description)}</div>`
    : '';
  const date = card.created_at ? `<span class="card-date">${formatDate(card.created_at)}</span>` : '';
  return `
    <div class="card" data-card-id="${card.id}" onclick="openEditModal(${card.id})">
      <div class="card-title">${escapeHtml(card.title)}</div>
      ${desc}
      <div class="card-meta">
        ${priorityBadgeHTML(card.priority)}
        ${date}
      </div>
    </div>
  `;
}

function columnHTML(col) {
  const colCards = getCardsForColumn(col.id);
  const count = colCards.length;
  const cardsHtml = colCards.length > 0
    ? colCards.map(cardHTML).join('')
    : `<div class="empty-column">No tasks yet</div>`;

  const color = col.color || '#38bdf8';

  return `
    <div class="column" data-column-id="${col.id}">
      <div class="column-header" style="border-left-color: ${color}">
        <div class="column-header-top">
          <div class="column-header-left">
            <span class="column-name">${escapeHtml(col.name)}</span>
            <span class="column-count">${count}</span>
          </div>
          <div class="column-header-actions">
            <div class="column-menu-wrapper">
              <button class="btn-icon" onclick="toggleColumnMenu(event, ${col.id})" title="Column options">
                <svg width="15" height="15" viewBox="0 0 16 16" fill="currentColor">
                  <circle cx="8" cy="3" r="1.2"/><circle cx="8" cy="8" r="1.2"/><circle cx="8" cy="13" r="1.2"/>
                </svg>
              </button>
              <div class="column-menu" id="col-menu-${col.id}">
                <button class="column-menu-item" onclick="openRenameModal(${col.id})">
                  <svg width="13" height="13" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                    <path d="M11 2l3 3-9 9H2v-3L11 2z"/>
                  </svg>
                  Rename
                </button>
                <div class="column-menu-divider"></div>
                <button class="column-menu-item danger" onclick="deleteColumn(${col.id})">
                  <svg width="13" height="13" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                    <polyline points="2,4 4,4 14,4"/><path d="M5 4V2h6v2"/><path d="M6 7v5m4-5v5"/><rect x="3" y="4" width="10" height="10" rx="1"/>
                  </svg>
                  Delete Column
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="column-cards" id="cards-${col.id}">
        ${cardsHtml}
      </div>

      <div class="column-footer">
        <div class="add-task-form" id="add-form-${col.id}">
          <div class="form-group">
            <input class="form-input" id="atf-title-${col.id}" placeholder="Task title…" onkeydown="handleAddTaskKey(event, ${col.id})" />
          </div>
          <div class="form-group">
            <textarea class="form-textarea form-textarea-short" id="atf-desc-${col.id}" placeholder="Description (optional)…" onkeydown="handleAddTaskDescKey(event, ${col.id})"></textarea>
          </div>
          <div class="form-group">
            <select class="form-select" id="atf-priority-${col.id}">
              <option value="low">Low priority</option>
              <option value="medium" selected>Medium priority</option>
              <option value="high">High priority</option>
              <option value="critical">Critical</option>
            </select>
          </div>
          <div class="form-row">
            <button type="button" class="btn btn-primary btn-flex" onclick="submitAddTask(${col.id})">Add Task</button>
            <button type="button" class="btn btn-ghost" onclick="hideAddTaskForm(${col.id})">Cancel</button>
          </div>
        </div>
        <button class="add-task-btn" id="add-btn-${col.id}" onclick="showAddTaskForm(${col.id})">
          <svg width="13" height="13" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
            <line x1="8" y1="2" x2="8" y2="14"/><line x1="2" y1="8" x2="14" y2="8"/>
          </svg>
          Add Task
        </button>
      </div>
    </div>
  `;
}

function renderBoard() {
  const container = document.getElementById('columns-container');
  const addForm = document.getElementById('add-column-form');

  // Remove all columns (keep add form and btn)
  const existing = container.querySelectorAll('.column');
  existing.forEach(el => el.remove());

  // Destroy old sortables
  Object.values(cardSortables).forEach(s => s.destroy());
  cardSortables = {};
  if (columnSortable) { columnSortable.destroy(); columnSortable = null; }

  // Sort columns
  const sorted = [...columns].sort((a, b) => a.position - b.position);

  // Insert columns before add form
  sorted.forEach(col => {
    const div = document.createElement('div');
    div.innerHTML = columnHTML(col);
    container.insertBefore(div.firstElementChild, addForm);
  });

  // Setup column drag
  columnSortable = new Sortable(container, {
    animation: 200,
    handle: '.column-header',
    draggable: '.column',
    ghostClass: 'sortable-ghost',
    dragClass: 'sortable-drag',
    onEnd: onColumnDragEnd,
  });

  // Setup card drags per column
  sorted.forEach(col => {
    const el = document.getElementById(`cards-${col.id}`);
    if (!el) return;
    cardSortables[col.id] = new Sortable(el, {
      animation: 200,
      group: 'cards',
      draggable: '.card',
      ghostClass: 'sortable-ghost',
      dragClass: 'sortable-drag',
      onEnd: onCardDragEnd,
    });
  });
}

// ── Drag Handlers ────────────────────────────────────────────
async function onColumnDragEnd(evt) {
  if (evt.oldIndex === evt.newIndex) return;

  // Build new order from DOM
  const container = document.getElementById('columns-container');
  const colEls = container.querySelectorAll('.column');
  const orderedIds = Array.from(colEls).map(el => parseInt(el.dataset.columnId));

  // Optimistic update
  orderedIds.forEach((id, idx) => {
    const col = columns.find(c => c.id === id);
    if (col) col.position = idx;
  });

  try {
    await api('PATCH', '/columns/reorder', { column_ids: orderedIds });
    showToast('Column order updated');
  } catch (err) {
    showToast('Failed to reorder columns: ' + err.message, 'error');
    await reloadData();
  }
}

async function onCardDragEnd(evt) {
  const cardId = parseInt(evt.item.dataset.cardId);
  const fromColumnId = evt.from.id.replace('cards-', '');
  const toColumnEl = evt.to;
  const toColumnId = parseInt(toColumnEl.id.replace('cards-', ''));
  const newIndex = evt.newIndex;

  if (parseInt(fromColumnId) === toColumnId && evt.oldIndex === newIndex) return;

  // If moved to different column, use /move endpoint
  if (parseInt(fromColumnId) !== toColumnId) {
    try {
      await api('PATCH', `/cards/${cardId}/move`, {
        column_id: toColumnId,
        position: newIndex,
      });
      // Update local state
      const card = cards.find(c => c.id === cardId);
      if (card) { card.column_id = toColumnId; card.position = newIndex; }
      await reloadCards();
      renderBoard();
      showToast('Card moved');
    } catch (err) {
      showToast('Failed to move card: ' + err.message, 'error');
      await reloadData();
    }
    return;
  }

  // Same column reorder
  const cardEls = toColumnEl.querySelectorAll('.card');
  const orderedCardIds = Array.from(cardEls).map(el => parseInt(el.dataset.cardId));

  try {
    await api('PATCH', `/columns/${toColumnId}/cards/reorder`, { card_ids: orderedCardIds });
    // Update local positions
    orderedCardIds.forEach((id, idx) => {
      const c = cards.find(x => x.id === id);
      if (c) c.position = idx;
    });
    showToast('Card order updated');
  } catch (err) {
    showToast('Failed to reorder cards: ' + err.message, 'error');
    await reloadData();
  }
}

// ── Column Actions ───────────────────────────────────────────
function toggleColumnMenu(event, colId) {
  event.stopPropagation();
  const menu = document.getElementById(`col-menu-${colId}`);
  const isOpen = menu.classList.contains('open');

  // Close all menus
  document.querySelectorAll('.column-menu.open').forEach(m => m.classList.remove('open'));

  if (!isOpen) {
    menu.classList.add('open');
    activeMenuColumnId = colId;
  } else {
    activeMenuColumnId = null;
  }
}

function closeAllMenus() {
  document.querySelectorAll('.column-menu.open').forEach(m => m.classList.remove('open'));
  activeMenuColumnId = null;
}

function openRenameModal(colId) {
  closeAllMenus();
  const col = columns.find(c => c.id === colId);
  if (!col) return;
  currentRenameColumnId = colId;
  document.getElementById('rename-col-name').value = col.name;
  document.getElementById('rename-col-color').value = col.color || '#38bdf8';
  document.getElementById('rename-modal-overlay').classList.add('open');
  setTimeout(() => document.getElementById('rename-col-name').focus(), 100);
}

function closeRenameModal() {
  document.getElementById('rename-modal-overlay').classList.remove('open');
  currentRenameColumnId = null;
}

async function submitRenameColumn() {
  const colId = currentRenameColumnId;
  if (!colId) return;
  const name = document.getElementById('rename-col-name').value.trim();
  const color = document.getElementById('rename-col-color').value;

  if (!name) {
    showToast('Column name is required', 'error');
    return;
  }

  try {
    const updated = await api('PATCH', `/columns/${colId}`, { name, color });
    const idx = columns.findIndex(c => c.id === colId);
    if (idx !== -1) columns[idx] = { ...columns[idx], ...updated };
    closeRenameModal();
    renderBoard();
    showToast('Column renamed');
  } catch (err) {
    showToast('Failed to rename column: ' + err.message, 'error');
  }
}

async function deleteColumn(colId) {
  closeAllMenus();
  const colCards = getCardsForColumn(colId);
  if (colCards.length > 0) {
    showToast(`Cannot delete column — it has ${colCards.length} task${colCards.length > 1 ? 's' : ''}`, 'error');
    return;
  }
  const col = columns.find(c => c.id === colId);
  const colName = col ? col.name : 'this column';
  if (!confirm(`Delete "${colName}"? This cannot be undone.`)) return;

  try {
    await api('DELETE', `/columns/${colId}`);
    columns = columns.filter(c => c.id !== colId);
    renderBoard();
    showToast('Column deleted');
  } catch (err) {
    showToast('Failed to delete column: ' + err.message, 'error');
  }
}

// ── Add Column ───────────────────────────────────────────────
function showAddColumnForm() {
  const form = document.getElementById('add-column-form');
  const btn = document.getElementById('add-column-btn');
  form.classList.add('visible');
  btn.style.display = 'none';
  document.getElementById('new-col-name').value = '';
  document.getElementById('new-col-color').value = randomColumnColor();
  setTimeout(() => document.getElementById('new-col-name').focus(), 50);
}

function hideAddColumnForm() {
  document.getElementById('add-column-form').classList.remove('visible');
  document.getElementById('add-column-btn').style.display = '';
}

function randomColumnColor() {
  const colors = ['#38bdf8', '#818cf8', '#34d399', '#f472b6', '#fb923c', '#a78bfa', '#4ade80'];
  return colors[Math.floor(Math.random() * colors.length)];
}

async function submitAddColumn() {
  const name = document.getElementById('new-col-name').value.trim();
  const color = document.getElementById('new-col-color').value;

  if (!name) {
    showToast('Column name is required', 'error');
    document.getElementById('new-col-name').focus();
    return;
  }

  try {
    const col = await api('POST', '/columns', { name, color });
    columns.push(col);
    hideAddColumnForm();
    renderBoard();
    showToast(`"${col.name}" column created`);
  } catch (err) {
    showToast('Failed to create column: ' + err.message, 'error');
  }
}

// ── Add Task Form ─────────────────────────────────────────────
function showAddTaskForm(colId) {
  const form = document.getElementById(`add-form-${colId}`);
  const btn = document.getElementById(`add-btn-${colId}`);
  if (!form || !btn) return;
  form.classList.add('visible');
  btn.style.display = 'none';
  document.getElementById(`atf-title-${colId}`).value = '';
  document.getElementById(`atf-desc-${colId}`).value = '';
  document.getElementById(`atf-priority-${colId}`).value = 'medium';
  setTimeout(() => document.getElementById(`atf-title-${colId}`).focus(), 50);
}

function hideAddTaskForm(colId) {
  const form = document.getElementById(`add-form-${colId}`);
  const btn = document.getElementById(`add-btn-${colId}`);
  if (form) form.classList.remove('visible');
  if (btn) btn.style.display = '';
}

function handleAddTaskKey(event, colId) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault();
    submitAddTask(colId);
  } else if (event.key === 'Escape') {
    hideAddTaskForm(colId);
  }
}

function handleAddTaskDescKey(event, colId) {
  if (event.key === 'Escape') {
    hideAddTaskForm(colId);
  }
}

async function submitAddTask(colId) {
  const title = document.getElementById(`atf-title-${colId}`).value.trim();
  const description = document.getElementById(`atf-desc-${colId}`).value.trim();
  const priority = document.getElementById(`atf-priority-${colId}`).value;

  if (!title) {
    showToast('Task title is required', 'error');
    document.getElementById(`atf-title-${colId}`).focus();
    return;
  }

  try {
    const card = await api('POST', '/cards', {
      title,
      description: description || null,
      column_id: colId,
      priority,
    });
    cards.push(card);
    hideAddTaskForm(colId);
    renderBoard();
    showToast('Task created');
  } catch (err) {
    showToast('Failed to create task: ' + err.message, 'error');
  }
}

// ── Edit Task Modal ───────────────────────────────────────────
function openEditModal(cardId) {
  const card = cards.find(c => c.id === cardId);
  if (!card) return;
  currentEditCardId = cardId;
  document.getElementById('modal-task-title').value = card.title;
  document.getElementById('modal-task-desc').value = card.description || '';
  document.getElementById('modal-task-priority').value = card.priority || 'low';
  document.getElementById('modal-title').textContent = 'Edit Task';
  document.getElementById('modal-overlay').classList.add('open');
  setTimeout(() => document.getElementById('modal-task-title').focus(), 100);
}

function closeModal() {
  document.getElementById('modal-overlay').classList.remove('open');
  currentEditCardId = null;
}

function handleModalOverlayClick(e) {
  if (e.target === document.getElementById('modal-overlay')) closeModal();
}

async function saveModalTask() {
  const cardId = currentEditCardId;
  if (!cardId) return;
  const title = document.getElementById('modal-task-title').value.trim();
  const description = document.getElementById('modal-task-desc').value.trim();
  const priority = document.getElementById('modal-task-priority').value;

  if (!title) {
    showToast('Task title is required', 'error');
    document.getElementById('modal-task-title').focus();
    return;
  }

  try {
    const updated = await api('PATCH', `/cards/${cardId}`, {
      title,
      description: description || null,
      priority,
    });
    const idx = cards.findIndex(c => c.id === cardId);
    if (idx !== -1) cards[idx] = { ...cards[idx], ...updated };
    closeModal();
    renderBoard();
    showToast('Task updated');
  } catch (err) {
    showToast('Failed to update task: ' + err.message, 'error');
  }
}

async function deleteCurrentTask() {
  const cardId = currentEditCardId;
  if (!cardId) return;
  const card = cards.find(c => c.id === cardId);
  const title = card ? card.title : 'this task';
  if (!confirm(`Delete "${title}"? This cannot be undone.`)) return;

  try {
    await api('DELETE', `/cards/${cardId}`);
    cards = cards.filter(c => c.id !== cardId);
    closeModal();
    renderBoard();
    showToast('Task deleted');
  } catch (err) {
    showToast('Failed to delete task: ' + err.message, 'error');
  }
}

// ── Rename overlay ───────────────────────────────────────────
function handleRenameOverlayClick(e) {
  if (e.target === document.getElementById('rename-modal-overlay')) closeRenameModal();
}

// ── Load data ────────────────────────────────────────────────
async function reloadCards() {
  cards = await api('GET', '/cards');
}

async function reloadData() {
  [columns, cards] = await Promise.all([
    api('GET', '/columns'),
    api('GET', '/cards'),
  ]);
}

async function init() {
  try {
    await reloadData();
    renderBoard();
    hideLoading();
  } catch (err) {
    document.querySelector('.loading-text').textContent = 'Failed to connect to API';
    document.querySelector('.spinner').style.borderTopColor = '#ef4444';
    showToast('Cannot connect to API at http://localhost:8000', 'error');
    console.error('Init error:', err);
  }
}

// ── Keyboard shortcuts ───────────────────────────────────────
document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') {
    if (document.getElementById('modal-overlay').classList.contains('open')) {
      closeModal();
    } else if (document.getElementById('rename-modal-overlay').classList.contains('open')) {
      closeRenameModal();
    } else if (activeMenuColumnId) {
      closeAllMenus();
    } else {
      // Close any open add task forms
      document.querySelectorAll('.add-task-form.visible').forEach(f => {
        const colId = f.id.replace('add-form-', '');
        hideAddTaskForm(parseInt(colId));
      });
      // Close add column form
      hideAddColumnForm();
    }
  }
});

// Close menus on outside click
document.addEventListener('click', (e) => {
  if (!e.target.closest('.column-menu-wrapper')) {
    closeAllMenus();
  }
});

// Enter on rename input
document.getElementById('rename-col-name').addEventListener('keydown', (e) => {
  if (e.key === 'Enter') submitRenameColumn();
  else if (e.key === 'Escape') closeRenameModal();
});

// Enter on new column name
document.getElementById('new-col-name').addEventListener('keydown', (e) => {
  if (e.key === 'Enter') submitAddColumn();
  else if (e.key === 'Escape') hideAddColumnForm();
});

// Enter on modal title
document.getElementById('modal-task-title').addEventListener('keydown', (e) => {
  if (e.key === 'Escape') closeModal();
});

// ── Utility ──────────────────────────────────────────────────
function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

// ── Boot ─────────────────────────────────────────────────────
init();

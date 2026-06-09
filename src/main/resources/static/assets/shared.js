const API_BASE = '/api/v1';

async function apiFetch(path, options = {}) {
  const { headers = {}, ...restOptions } = options;

  const response = await fetch(`${API_BASE}${path}`, {
    ...restOptions,
    headers: {
      'Content-Type': 'application/json',
      ...headers
    }
  });

  const text = await response.text();
  let data = null;
  if (text) {
    try { data = JSON.parse(text); } catch { data = text; }
  }

  if (!response.ok) {
    const details = Array.isArray(data?.details) && data.details.length ? ` ${data.details.join(' | ')}` : '';
    const message = data?.message || data?.error || data || `HTTP ${response.status}`;
    throw new Error(`${message}${details}`);
  }
  return data;
}

function q(selector, scope = document) { return scope.querySelector(selector); }
function qa(selector, scope = document) { return [...scope.querySelectorAll(selector)]; }

function money(value) {
  const number = Number(value || 0);
  return new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' }).format(number);
}

function dateText(value) {
  if (!value) return '-';
  return new Intl.DateTimeFormat('es-PE', { dateStyle: 'medium' }).format(new Date(value));
}

function dateTimeText(value) {
  if (!value) return '-';
  return new Intl.DateTimeFormat('es-PE', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value));
}

function toast(message, type = 'info') {
  const el = q('#toast');
  if (!el) return;
  el.textContent = message;
  el.className = `toast show ${type}`;
  clearTimeout(window.__toastTimer);
  window.__toastTimer = setTimeout(() => el.className = 'toast', 4200);
}

function renderJson(target, data) {
  const el = typeof target === 'string' ? q(target) : target;
  if (el) el.textContent = JSON.stringify(data, null, 2);
}

function getSession(scope = 'extranet') {
  const raw = localStorage.getItem(`turism:${scope}:session`);
  return raw ? JSON.parse(raw) : null;
}

function setSession(user, scope = 'extranet') {
  localStorage.setItem(`turism:${scope}:session`, JSON.stringify(user));
}

function clearSession(scope = 'extranet') {
  localStorage.removeItem(`turism:${scope}:session`);
}

function badgeStatus(status) {
  const value = String(status || '').toUpperCase();
  const cls = ['ACTIVA', 'APROBADO', 'PUBLICADA', 'CONFIRMADA'].includes(value) ? 'ok'
    : ['RECHAZADA', 'RECHAZADO'].includes(value) ? 'danger'
    : 'warn';
  return `<span class="badge ${cls}">${value || 'SIN ESTADO'}</span>`;
}

function parseLines(text, mapper) {
  return String(text || '')
    .split('\n')
    .map(line => line.trim())
    .filter(Boolean)
    .map(mapper);
}

function sessionHeaders(scope = 'extranet') {
  const user = getSession(scope);
  if (!user || !user.sessionToken) return {};
  return {
    'X-Session-User': String(user.idUsuario),
    'X-Session-Token': user.sessionToken
  };
}

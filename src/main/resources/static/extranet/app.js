let categorias = [];
let intereses = [];
let experienciasCache = [];

function currentUser() { return getSession('extranet'); }

function requireSession() {
  const user = currentUser();
  if (!user || !user.sessionToken) throw new Error('Primero inicia sesión.');
  return user;
}

function puedeReservar() {
  const user = currentUser();
  return Boolean(
    user
    && user.sessionToken
    && user.rol === 'VIAJERO'
    && user.correoVerificado
    && user.estadoCuenta === 'ACTIVA'
  );
}


function syncSessionUi() {
  const logged = Boolean(currentUser() && currentUser().sessionToken);
  qa('.session-guest').forEach(el => el.classList.toggle('hidden', logged));
  qa('.session-auth').forEach(el => el.classList.toggle('hidden', !logged));

  const active = q('[data-section].active');
  if (active && active.classList.contains('hidden')) {
    showSection('explorar');
  }
}

function cerrarSesion() {
  clearSession('extranet');
  q('#tablaMisReservas').innerHTML = '';
  q('#notificacionesList').innerHTML = '';
  limpiarFormularioReserva();
  renderSession();
  toast('Sesión cerrada.');
  showSection('explorar');
}

function actualizarEstadoFormularioReserva() {
  const submit = q('#btnEnviarReserva');
  const hint = q('#reservaLoginHint');
  if (!submit || !hint) return;

  const user = currentUser();
  const permitido = puedeReservar();
  submit.disabled = !permitido;

  if (!user || !user.sessionToken) {
    hint.textContent = 'Primero inicia sesión para enviar una reserva.';
  } else if (user.rol !== 'VIAJERO') {
    hint.textContent = 'Solo una cuenta de viajero puede registrar reservas.';
  } else if (!user.correoVerificado || user.estadoCuenta !== 'ACTIVA') {
    hint.textContent = 'Activa tu cuenta antes de reservar.';
  } else {
    hint.textContent = 'Listo. Puedes enviar tu solicitud.';
  }
}

function showSection(name, syncHash = true) {
  qa('.section').forEach(section => section.classList.toggle('active', section.id === name));
  qa('[data-section]').forEach(btn => btn.classList.toggle('active', btn.dataset.section === name));
  if (syncHash) history.replaceState(null, '', `#${name}`);
}

function initSectionFromHash() {
  const hash = window.location.hash.replace('#', '');
  showSection(hash && q(`#${hash}.section`) ? hash : 'explorar', false);
}

function tomorrowDateString() {
  const date = new Date();
  date.setDate(date.getDate() + 1);
  return date.toISOString().slice(0, 10);
}

function isTomorrowOrLater(value) {
  return Boolean(value) && value >= tomorrowDateString();
}

function setReservaMinDate() {
  const input = q('#reservaFechaInput');
  if (input) input.min = tomorrowDateString();
}

function primeraFechaReservable(exp) {
  return (exp?.disponibilidades || [])
    .map(d => d.fechaDisponible)
    .filter(isTomorrowOrLater)
    .sort()[0] || '';
}

function experienciaPorId(idExperiencia) {
  return experienciasCache.find(exp => String(exp.idExperiencia) === String(idExperiencia));
}

function renderSession() {
  const user = currentUser();
  const el = q('#sessionInfo');
  if (!user) {
    el.innerHTML = `
      <div class="summary-top">
        <div>
          <span class="route-title">Estado actual</span>
          <h3>Sin sesión</h3>
          <p>Ingresa o crea una cuenta para reservar experiencias.</p>
        </div>
        <span class="badge invert">Público</span>
      </div>
      <div class="hero-stats">
        <div class="stat-box"><strong>Portal de viajes</strong><span class="small">Solo clientes viajeros.</span></div>
        <div class="stat-box"><strong>Reservas</strong><span class="small">Solicitud, revisión y confirmación.</span></div>
      </div>`;
    actualizarEstadoFormularioReserva();
    syncSessionUi();
    return;
  }
  el.innerHTML = `
    <div class="summary-top">
      <div>
        <span class="route-title">Cuenta activa</span>
        <h3>${user.nombres} ${user.apellidos}</h3>
        <p>${user.email}</p>
      </div>
      <span class="badge invert">${user.rol}</span>
    </div>
    <div class="actions">
      ${badgeStatus(user.estadoCuenta)}
      <span class="badge ${user.correoVerificado ? 'ok' : 'warn'}">Cuenta ${user.correoVerificado ? 'activada' : 'pendiente'}</span>
      <span class="badge">Usuario #${user.idUsuario}</span>
    </div>`;
  actualizarEstadoFormularioReserva();
  syncSessionUi();
}

async function cargarCatalogos() {
  categorias = await apiFetch('/catalogos/categorias');
  intereses = await apiFetch('/catalogos/intereses');
  q('#searchCategoria').innerHTML = '<option value="">Todas</option>' + categorias.map(c => `<option value="${c.nombre}">${c.nombre}</option>`).join('');
  q('#interesesBox').innerHTML = intereses.map(i => `
    <label class="pill-check"><input type="checkbox" name="interesesIds" value="${i.id}" /> ${i.nombre}</label>
  `).join('');
}

function payloadRegistro(form) {
  const fd = new FormData(form);
  const interesesIds = qa('input[name="interesesIds"]:checked', form).map(x => Number(x.value));
  return {
    nombres: fd.get('nombres'),
    apellidos: fd.get('apellidos'),
    email: fd.get('email'),
    password: fd.get('password'),
    telefono: fd.get('telefono'),
    tipoCuenta: 'VIAJERO',
    interesesIds
  };
}

async function login(event) {
  event.preventDefault();
  const fd = new FormData(event.target);
  const user = await apiFetch('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email: fd.get('email'), password: fd.get('password') })
  });
  if (user.rol !== 'VIAJERO') {
    clearSession('extranet');
    throw new Error('Este portal es solo para viajeros. Usa el panel administrativo para otros roles.');
  }
  setSession(user, 'extranet');
  renderSession();
  renderJson('#authOutput', user);
  toast('Sesión iniciada.');
  showSection('explorar');
}

async function registrar(event) {
  event.preventDefault();
  const user = await apiFetch('/auth/registro', {
    method: 'POST',
    body: JSON.stringify(payloadRegistro(event.target))
  });
  clearSession('extranet');
  renderSession();
  renderJson('#authOutput', user);
  toast('Cuenta creada. Inicia sesión y activa tu cuenta para reservar.');
  showSection('acceso');
}

async function verificarCorreo() {
  const user = requireSession();
  const updated = await apiFetch(`/auth/usuarios/${user.idUsuario}/verificar-correo`, { method: 'PATCH' });
  const updatedWithSession = { ...updated, sessionToken: user.sessionToken };
  setSession(updatedWithSession, 'extranet');
  renderSession();
  renderJson('#authOutput', updatedWithSession);
  toast('Cuenta activada.');
}

function aplicarFiltros() {
  const text = q('#searchText').value.trim().toLowerCase();
  const categoria = q('#searchCategoria').value;
  const precioMax = Number(q('#searchPrecio').value || 0);
  const filtradas = experienciasCache.filter(exp => {
    const blob = `${exp.titulo} ${exp.descripcion} ${exp.ubicacion} ${exp.categoria}`.toLowerCase();
    const matchText = !text || blob.includes(text);
    const matchCategoria = !categoria || exp.categoria === categoria;
    const matchPrecio = !precioMax || Number(exp.precio) <= precioMax;
    return matchText && matchCategoria && matchPrecio;
  });
  renderExperiencias(filtradas);
}

async function cargarExperiencias() {
  experienciasCache = await apiFetch('/experiencias/publicadas');
  actualizarSelectorExperiencias();
  aplicarFiltros();
}

function actualizarSelectorExperiencias(selectedId = '') {
  const select = q('#reservaExperienciaSelect');
  if (!select) return;

  select.innerHTML = '<option value="">Selecciona una experiencia</option>' + experienciasCache.map(exp => {
    const fecha = primeraFechaReservable(exp);
    const disabled = fecha ? '' : 'disabled';
    const label = `#${exp.idExperiencia} · ${exp.titulo} · ${exp.ubicacion} · ${money(exp.precio)}`;
    return `<option value="${exp.idExperiencia}" ${disabled}>${label}</option>`;
  }).join('');

  if (selectedId) select.value = String(selectedId);
  actualizarFechaSegunExperiencia();
}

function actualizarFechaSegunExperiencia() {
  setReservaMinDate();
  const select = q('#reservaExperienciaSelect');
  const fechaInput = q('#reservaFechaInput');
  if (!select || !fechaInput) return;

  const exp = experienciaPorId(select.value);
  const fecha = primeraFechaReservable(exp);
  fechaInput.value = fecha;
  fechaInput.disabled = !select.value;
}

function limpiarFormularioReserva() {
  const form = q('#formReserva');
  form.reset();
  q('#reservaExperienciaSelect').value = '';
  q('#reservaFechaInput').value = '';
  q('#reservaFechaInput').disabled = true;
  q('#formReserva [name="cantidadPersonas"]').value = 1;
  setReservaMinDate();
  actualizarEstadoFormularioReserva();
}

function renderExperiencias(experiencias) {
  const target = q('#experienciasList');
  q('#experienciasCount').textContent = `${experiencias.length} resultado${experiencias.length === 1 ? '' : 's'}`;
  if (!experiencias.length) {
    target.innerHTML = `<div class="card"><h3>No hay experiencias para ese filtro</h3><p>Prueba buscando por otro destino, categoría o precio.</p></div>`;
    return;
  }
  target.innerHTML = experiencias.map(exp => {
    const img = exp.fotosUrls?.[0];
    const fechas = (exp.disponibilidades || []).map(d => `${d.fechaDisponible} (${d.cuposDisponibles} cupos)`).join(', ');
    const fechaReservable = primeraFechaReservable(exp);
    return `
      <article class="card experience-card">
        <div class="experience-img">${img ? `<img src="${img}" alt="${exp.titulo}" onerror="this.remove()" />` : ''}</div>
        <div>
          <div class="actions mb">
            ${badgeStatus(exp.estadoPublicacion)}
            <span class="badge">#${exp.idExperiencia}</span>
            <span class="badge">${exp.categoria}</span>
          </div>
          <h3>${exp.titulo}</h3>
          <p>${exp.descripcion}</p>
          <div class="metric-row">
            <div class="metric"><span>Destino</span><strong>${exp.ubicacion}</strong></div>
            <div class="metric"><span>Precio</span><strong>${money(exp.precio)}</strong></div>
            <div class="metric"><span>Duración</span><strong>${exp.duracionHoras} h</strong></div>
          </div>
          <p class="small mt"><strong>Fechas:</strong> ${fechas || 'Sin fechas registradas'}</p>
          <div class="actions mt">
            <button class="btn primary" ${fechaReservable ? '' : 'disabled'} onclick="prepararReserva(${exp.idExperiencia})">Reservar</button>
          </div>
        </div>
      </article>
    `;
  }).join('');
}

function prepararReserva(idExperiencia) {
  const exp = experienciaPorId(idExperiencia);
  const fecha = primeraFechaReservable(exp);
  if (!fecha) {
    toast('Esta experiencia no tiene fechas disponibles desde mañana.', 'error');
    return;
  }
  showSection('explorar');
  actualizarSelectorExperiencias(idExperiencia);
  q('#reservaFechaInput').value = fecha;
  q('#formReserva').scrollIntoView({ behavior: 'smooth', block: 'center' });
}
window.prepararReserva = prepararReserva;

async function crearReserva(event) {
  event.preventDefault();
  const user = requireSession();
  if (!puedeReservar()) throw new Error('Debes iniciar sesión como viajero y activar tu cuenta para reservar.');
  const fd = new FormData(event.target);
  const idExperiencia = Number(fd.get('idExperiencia'));
  const fechaExperiencia = fd.get('fechaExperiencia');

  if (!idExperiencia) throw new Error('Selecciona una experiencia desde el listado.');
  if (!isTomorrowOrLater(fechaExperiencia)) throw new Error('La fecha debe ser desde mañana en adelante.');
  const expSeleccionada = experienciaPorId(idExperiencia);
  const fechasDisponibles = (expSeleccionada?.disponibilidades || []).map(d => d.fechaDisponible);
  if (!fechasDisponibles.includes(fechaExperiencia)) throw new Error('Selecciona una fecha disponible para la experiencia elegida.');

  const reserva = await apiFetch('/reservas', {
    method: 'POST',
    headers: sessionHeaders('extranet'),
    body: JSON.stringify({
      idUsuario: user.idUsuario,
      idExperiencia,
      fechaExperiencia,
      cantidadPersonas: Number(fd.get('cantidadPersonas')),
      observaciones: fd.get('observaciones')
    })
  });
  toast('Solicitud enviada.');
  renderJson('#authOutput', reserva);
  limpiarFormularioReserva();
  await misReservas().catch(() => null);
}

async function misReservas() {
  const user = requireSession();
  if (user.rol !== 'VIAJERO') throw new Error('Solo una cuenta de viajero puede consultar reservas.');
  const data = await apiFetch(`/reservas/usuario/${user.idUsuario}`, { headers: sessionHeaders('extranet') });
  q('#tablaMisReservas').innerHTML = `
    <thead><tr><th>ID</th><th>Experiencia</th><th>Fecha</th><th>Personas</th><th>Estado</th><th>Registro</th></tr></thead>
    <tbody>${data.map(r => `<tr><td>#${r.idReserva}</td><td>${r.experiencia}</td><td>${dateText(r.fechaExperiencia)}</td><td>${r.cantidadPersonas}</td><td>${badgeStatus(r.estado)}</td><td>${dateTimeText(r.fechaReserva)}</td></tr>`).join('')}</tbody>
  `;
}

async function listarNotificaciones() {
  const user = requireSession();
  const data = await apiFetch(`/notificaciones/usuario/${user.idUsuario}`);
  q('#notificacionesList').innerHTML = data.length ? data.map(n => `
    <article class="card">
      <div class="actions mb"><span class="badge ${n.leida ? 'ok' : 'warn'}">${n.leida ? 'Leída' : 'Nueva'}</span><span class="small">${dateTimeText(n.fechaEnvio)}</span></div>
      <h3>${n.titulo}</h3>
      <p>${n.mensaje}</p>
    </article>
  `).join('') : '<div class="card"><p>No hay notificaciones.</p></div>';
}

function bind() {
  qa('[data-section]').forEach(btn => btn.addEventListener('click', () => showSection(btn.dataset.section)));
  qa('[data-go-section]').forEach(btn => btn.addEventListener('click', () => showSection(btn.dataset.goSection)));
  q('#formBuscar').addEventListener('submit', event => { event.preventDefault(); aplicarFiltros(); });
  q('#searchText').addEventListener('input', aplicarFiltros);
  q('#searchCategoria').addEventListener('change', aplicarFiltros);
  q('#searchPrecio').addEventListener('input', aplicarFiltros);
  q('#reservaExperienciaSelect').addEventListener('change', actualizarFechaSegunExperiencia);
  q('#formLogin').addEventListener('submit', wrap(login));
  q('#formRegistro').addEventListener('submit', wrap(registrar));
  q('#btnLogout').addEventListener('click', cerrarSesion);
  q('#btnTopLogout').addEventListener('click', cerrarSesion);
  q('#btnVerifyEmail').addEventListener('click', wrap(verificarCorreo));
  q('#btnLoadExperiencias').addEventListener('click', wrap(cargarExperiencias));
  q('#formReserva').addEventListener('submit', wrap(crearReserva));
  q('#btnMisReservas').addEventListener('click', wrap(misReservas));
  q('#btnNotificaciones').addEventListener('click', wrap(listarNotificaciones));
}

function wrap(fn) {
  return async (event) => {
    try { await fn(event); } catch (error) { toast(error.message || String(error), 'error'); }
  };
}

document.addEventListener('DOMContentLoaded', async () => {
  bind();
  initSectionFromHash();
  setReservaMinDate();
  limpiarFormularioReserva();
  renderSession();
  try {
    await cargarCatalogos();
    await cargarExperiencias();
  } catch (error) {
    toast(error.message || String(error), 'error');
  }
});

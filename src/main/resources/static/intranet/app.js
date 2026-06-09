
let categoriasAdmin = [];
let ultimoAnfitrionReservas = null;

function adminUser() { return getSession('intranet'); }

function showSection(name, syncHash = true) {
  qa('.section').forEach(section => section.classList.toggle('active', section.id === name));
  qa('[data-section]').forEach(btn => btn.classList.toggle('active', btn.dataset.section === name));
  if (syncHash) history.replaceState(null, '', `#${name}`);
}

function initSectionFromHash() {
  const hash = window.location.hash.replace('#', '');
  showSection(hash && q(`#${hash}.section`) ? hash : 'dashboard', false);
}

function requireAdmin() {
  const user = adminUser();
  if (!user) throw new Error('Primero inicia sesión como administrador.');
  if (user.rol !== 'ADMINISTRADOR') throw new Error('Esta sección requiere rol ADMINISTRADOR.');
  return user;
}

function renderAdminSession() {
  const user = adminUser();
  q('#adminSession').innerHTML = user
    ? `<strong>${user.nombres} ${user.apellidos}</strong><br>${user.email}<br>${badgeStatus(user.estadoCuenta)} · Usuario #${user.idUsuario}`
    : 'Sin sesión administrativa activa.';
}

async function cargarCatalogosAdmin() {
  categoriasAdmin = await apiFetch('/catalogos/categorias');
  q('#categoriaAdminSelect').innerHTML = categoriasAdmin.map(c => `<option value="${c.id}">${c.nombre}</option>`).join('');
}

async function adminLogin(event) {
  event.preventDefault();
  const fd = new FormData(event.target);
  const user = await apiFetch('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email: fd.get('email'), password: fd.get('password') })
  });
  if (user.rol !== 'ADMINISTRADOR') {
    clearSession('intranet');
    throw new Error('El usuario no tiene rol ADMINISTRADOR.');
  }
  setSession(user, 'intranet');
  renderAdminSession();
  toast('Sesión administrativa iniciada.');
  await cargarDashboard();
}

async function cargarDashboard() {
  requireAdmin();
  const [anfitriones, pendientes, publicadas] = await Promise.all([
    apiFetch('/admin/anfitriones?estado=PENDIENTE'),
    apiFetch('/admin/experiencias?estado=PENDIENTE'),
    apiFetch('/experiencias/publicadas')
  ]);
  q('#countAnfitriones').textContent = anfitriones.length;
  q('#countExperiencias').textContent = pendientes.length;
  q('#countPublicadas').textContent = publicadas.length;
}

function payloadAnfitrion(form) {
  const fd = new FormData(form);
  return {
    nombres: fd.get('nombres'),
    apellidos: fd.get('apellidos'),
    email: fd.get('email'),
    password: fd.get('password'),
    telefono: fd.get('telefono'),
    tipoCuenta: 'ANFITRION',
    documentoIdentidad: fd.get('documentoIdentidad'),
    descripcionAnfitrion: fd.get('descripcionAnfitrion'),
    interesesIds: []
  };
}

async function crearAnfitrion(event) {
  event.preventDefault();
  requireAdmin();
  const data = await apiFetch('/admin/anfitriones/registrar', {
    method: 'POST',
    body: JSON.stringify(payloadAnfitrion(event.target))
  });
  toast('Anfitrión registrado desde administración.');
  event.target.reset();
  q('#formCrearAnfitrion [name="telefono"]').value = '999999999';
  q('#formCrearAnfitrion [name="password"]').value = 'Host12345';
  await cargarAnfitriones();
  await cargarDashboard();
  console.log(data);
}

async function cargarAnfitriones() {
  requireAdmin();
  const estado = q('#filterAnfitriones').value;
  const data = await apiFetch(`/admin/anfitriones${estado ? `?estado=${estado}` : ''}`);
  q('#tablaAnfitriones').innerHTML = `
    <thead><tr><th>ID</th><th>Operador</th><th>Email</th><th>Documento</th><th>Descripción</th><th>Validación</th><th>Cuenta</th><th>Acción</th></tr></thead>
    <tbody>${data.map(a => `
      <tr>
        <td>#${a.idAnfitrion}</td>
        <td>${a.nombres} ${a.apellidos}<br><span class="small">Usuario #${a.idUsuario}</span></td>
        <td>${a.email}</td>
        <td>${a.documentoIdentidad}</td>
        <td>${a.descripcion}</td>
        <td>${badgeStatus(a.estadoValidacion)}</td>
        <td>${badgeStatus(a.estadoCuenta)}</td>
        <td class="actions">
          <button class="btn primary" onclick="aprobarAnfitrion(${a.idAnfitrion})">Aprobar</button>
          <button class="btn danger" onclick="rechazarAnfitrion(${a.idAnfitrion})">Rechazar</button>
          <button class="btn soft" onclick="usarAnfitrion(${a.idAnfitrion})">Usar ID</button>
        </td>
      </tr>
    `).join('')}</tbody>
  `;
}

function usarAnfitrion(id) {
  showSection('experiencias');
  q('#formCrearExperienciaAdmin [name="idAnfitrion"]').value = id;
  q('#formCrearExperienciaAdmin').scrollIntoView({ behavior: 'smooth', block: 'start' });
}
window.usarAnfitrion = usarAnfitrion;

async function aprobarAnfitrion(id) {
  requireAdmin();
  await apiFetch(`/admin/anfitriones/${id}/aprobar`, { method: 'PATCH' });
  toast('Anfitrión aprobado.');
  await cargarAnfitriones();
  await cargarDashboard();
}

async function rechazarAnfitrion(id) {
  requireAdmin();
  const motivo = prompt('Motivo del rechazo:', 'Información incompleta o no verificable.');
  if (!motivo) return;
  await apiFetch(`/admin/anfitriones/${id}/rechazar`, {
    method: 'PATCH',
    body: JSON.stringify({ motivo })
  });
  toast('Anfitrión rechazado.');
  await cargarAnfitriones();
  await cargarDashboard();
}
window.aprobarAnfitrion = aprobarAnfitrion;
window.rechazarAnfitrion = rechazarAnfitrion;

function payloadExperienciaAdmin(form) {
  const fd = new FormData(form);
  const fotosUrls = parseLines(fd.get('fotosUrls'), line => line);
  const disponibilidades = parseLines(fd.get('disponibilidades'), line => {
    const [fechaDisponible, cuposDisponibles] = line.split(',').map(x => x.trim());
    return { fechaDisponible, cuposDisponibles: Number(cuposDisponibles) };
  });
  return {
    idAnfitrion: Number(fd.get('idAnfitrion')),
    body: {
      idCategoria: Number(fd.get('idCategoria')),
      titulo: fd.get('titulo'),
      descripcion: fd.get('descripcion'),
      ubicacion: fd.get('ubicacion'),
      precio: Number(fd.get('precio')),
      capacidadMaxima: Number(fd.get('capacidadMaxima')),
      duracionHoras: Number(fd.get('duracionHoras')),
      fotosUrls,
      disponibilidades
    }
  };
}

async function crearExperienciaAdmin(event) {
  event.preventDefault();
  requireAdmin();
  const payload = payloadExperienciaAdmin(event.target);
  const data = await apiFetch(`/anfitriones/${payload.idAnfitrion}/experiencias`, {
    method: 'POST',
    body: JSON.stringify(payload.body)
  });
  toast('Experiencia guardada. Quedó pendiente de aprobación.');
  event.target.reset();
  await cargarExperienciasAdmin();
  await cargarDashboard();
  console.log(data);
}

async function cargarExperienciasAdmin() {
  requireAdmin();
  const estado = q('#filterExperiencias').value;
  const data = await apiFetch(`/admin/experiencias${estado ? `?estado=${estado}` : ''}`);
  q('#tablaExperienciasAdmin').innerHTML = `
    <thead><tr><th>ID</th><th>Experiencia</th><th>Anfitrión</th><th>Categoría</th><th>Precio</th><th>Disponibilidad</th><th>Estado</th><th>Acción</th></tr></thead>
    <tbody>${data.map(e => `
      <tr>
        <td>#${e.idExperiencia}</td>
        <td><strong>${e.titulo}</strong><br><span class="small">${e.ubicacion}</span><br>${e.descripcion}</td>
        <td>#${e.idAnfitrion}<br>${e.anfitrion}</td>
        <td>${e.categoria}</td>
        <td>${money(e.precio)}</td>
        <td>${(e.disponibilidades || []).map(d => `${d.fechaDisponible}: ${d.cuposDisponibles}`).join('<br>')}</td>
        <td>${badgeStatus(e.estadoPublicacion)}</td>
        <td class="actions">
          <button class="btn primary" onclick="aprobarExperiencia(${e.idExperiencia})">Aprobar</button>
          <button class="btn danger" onclick="rechazarExperiencia(${e.idExperiencia})">Rechazar</button>
        </td>
      </tr>
    `).join('')}</tbody>
  `;
}

async function aprobarExperiencia(id) {
  requireAdmin();
  await apiFetch(`/admin/experiencias/${id}/aprobar`, { method: 'PATCH' });
  toast('Experiencia publicada.');
  await cargarExperienciasAdmin();
  await cargarDashboard();
}

async function rechazarExperiencia(id) {
  requireAdmin();
  const motivo = prompt('Motivo del rechazo:', 'La publicación requiere correcciones antes de ser publicada.');
  if (!motivo) return;
  await apiFetch(`/admin/experiencias/${id}/rechazar`, {
    method: 'PATCH',
    body: JSON.stringify({ motivo })
  });
  toast('Experiencia rechazada.');
  await cargarExperienciasAdmin();
  await cargarDashboard();
}
window.aprobarExperiencia = aprobarExperiencia;
window.rechazarExperiencia = rechazarExperiencia;

async function buscarReservasAnfitrion(event) {
  event.preventDefault();
  requireAdmin();
  const fd = new FormData(event.target);
  ultimoAnfitrionReservas = Number(fd.get('idAnfitrion'));
  await listarReservasAnfitrion(ultimoAnfitrionReservas);
}

async function listarReservasAnfitrion(idAnfitrion) {
  const data = await apiFetch(`/anfitriones/${idAnfitrion}/reservas`);
  q('#tablaReservasAnfitrion').innerHTML = `
    <thead><tr><th>ID</th><th>Viajero</th><th>Experiencia</th><th>Fecha</th><th>Personas</th><th>Estado</th><th>Acción</th></tr></thead>
    <tbody>${data.map(r => `
      <tr>
        <td>#${r.idReserva}</td><td>${r.viajero}</td><td>${r.experiencia}</td><td>${dateText(r.fechaExperiencia)}</td><td>${r.cantidadPersonas}</td><td>${badgeStatus(r.estado)}</td>
        <td class="actions">
          <button class="btn primary" onclick="cambiarReserva(${idAnfitrion}, ${r.idReserva}, 'aceptar')">Aceptar</button>
          <button class="btn danger" onclick="cambiarReserva(${idAnfitrion}, ${r.idReserva}, 'rechazar')">Rechazar</button>
        </td>
      </tr>
    `).join('')}</tbody>
  `;
}

async function cambiarReserva(idAnfitrion, idReserva, accion) {
  requireAdmin();
  await apiFetch(`/anfitriones/${idAnfitrion}/reservas/${idReserva}/${accion}`, { method: 'PATCH' });
  toast(`Reserva ${accion === 'aceptar' ? 'aceptada' : 'rechazada'}.`);
  await listarReservasAnfitrion(idAnfitrion);
}
window.cambiarReserva = cambiarReserva;

async function cargarNotificacionesAdmin() {
  const user = requireAdmin();
  const data = await apiFetch(`/notificaciones/usuario/${user.idUsuario}`);
  q('#adminNotificaciones').innerHTML = data.length ? data.map(n => `
    <article class="card">
      <div class="actions mb"><span class="badge ${n.leida ? 'ok' : 'warn'}">${n.leida ? 'Leída' : 'Nueva'}</span><span class="small">${dateTimeText(n.fechaEnvio)}</span></div>
      <h3>${n.titulo}</h3>
      <p>${n.mensaje}</p>
    </article>
  `).join('') : '<div class="card"><p>No hay notificaciones.</p></div>';
}

function wrap(fn) {
  return async (event) => {
    try { await fn(event); } catch (error) { toast(error.message || String(error), 'error'); }
  };
}

function bind() {
  qa('[data-section]').forEach(btn => btn.addEventListener('click', () => showSection(btn.dataset.section)));
  qa('[data-go-section]').forEach(btn => btn.addEventListener('click', () => showSection(btn.dataset.goSection)));
  q('#formAdminLogin').addEventListener('submit', wrap(adminLogin));
  q('#btnAdminLogout').addEventListener('click', () => { clearSession('intranet'); renderAdminSession(); toast('Sesión cerrada.'); showSection('dashboard'); });
  q('#btnRefreshDashboard').addEventListener('click', wrap(cargarDashboard));
  q('#formCrearAnfitrion').addEventListener('submit', wrap(crearAnfitrion));
  q('#btnLoadAnfitriones').addEventListener('click', wrap(cargarAnfitriones));
  q('#formCrearExperienciaAdmin').addEventListener('submit', wrap(crearExperienciaAdmin));
  q('#btnLoadExperienciasAdmin').addEventListener('click', wrap(cargarExperienciasAdmin));
  q('#formBuscarReservasAnfitrion').addEventListener('submit', wrap(buscarReservasAnfitrion));
  q('#btnAdminNotificaciones').addEventListener('click', wrap(cargarNotificacionesAdmin));
}

document.addEventListener('DOMContentLoaded', async () => {
  bind();
  initSectionFromHash();
  renderAdminSession();
  try { await cargarCatalogosAdmin(); } catch (error) { toast(error.message || String(error), 'error'); }
  if (adminUser()) {
    try { await cargarDashboard(); } catch (error) { toast(error.message || String(error), 'error'); }
  }
});

import { api, decodeToken, getToken, setToken, setUnauthorizedHandler } from './api.js';
import { html, raw, $, $$, PERFIL, toast, closeModal } from './lib.js';
import { loginPage } from './pages/login.js';
import { dashboardPage } from './pages/dashboard.js';
import { ticketsPage } from './pages/tickets.js';
import { ticketNewPage } from './pages/ticket-new.js';
import { ticketDetailPage } from './pages/ticket-detail.js';
import { equipamentosPage } from './pages/equipamentos.js';
import { usuariosPage } from './pages/usuarios.js';

export const session = { user: null };
const app = $('#app');
let navToken = 0, expiryTimer;

const NAV = [
  { href: '#/', label: 'Painel', icon: 'M3 3h7v9H3zM14 3h7v5h-7zM14 12h7v9h-7zM3 16h7v5H3z', roles: ['ADMIN', 'TECNICO', 'CLIENTE'] },
  { href: '#/chamados', label: 'Chamados', icon: 'M4 5h16M4 12h16M4 19h10', roles: ['ADMIN', 'TECNICO', 'CLIENTE'] },
  { href: '#/equipamentos', label: 'Equipamentos', icon: 'M12 8a4 4 0 100 8 4 4 0 000-8zM12 2v3M12 19v3M2 12h3M19 12h3M5 5l2 2M17 17l2 2M5 19l2-2M17 7l2-2', roles: ['ADMIN', 'TECNICO', 'CLIENTE'] },
  { href: '#/usuarios', label: 'Usuários', icon: 'M16 11a4 4 0 10-8 0 4 4 0 008 0zM4 21c0-4 3.6-6 8-6s8 2 8 6', roles: ['ADMIN'] },
];
const icon = (d) => html`<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="${d}"/></svg>`;

export const isRole = (...r) => r.includes(session.user?.perfil);
export const go = (hash) => { if (location.hash === hash) route(); else location.hash = hash; };

export function logout(msg) {
  setToken(null); session.user = null; clearTimeout(expiryTimer); closeModal();
  if (location.hash !== '#/') history.replaceState(null, '', '#/');
  renderLogin(msg);
}
setUnauthorizedHandler(() => logout('Sua sessão expirou. Entre novamente.'));

function renderLogin(msg) {
  app.innerHTML = '';
  loginPage(app, { message: msg, onLogin: start });
}

async function start() {
  const info = decodeToken();
  if (!info || info.exp < Date.now()) return logout();
  const perfil = ['ADMIN', 'TECNICO', 'CLIENTE'].find((r) => info.roles.includes(r));
  session.user = { email: info.email, perfil, id: null, nome: null, exp: info.exp };
  try { const me = await api.me(); if (me) Object.assign(session.user, { id: me.id, nome: me.nome }); } catch {}
  clearTimeout(expiryTimer);
  expiryTimer = setTimeout(() => logout('Sua sessão expirou. Entre novamente.'), Math.max(1000, info.exp - Date.now()));
  renderShell();
  route();
}

function renderShell() {
  const u = session.user;
  const nav = NAV.filter((n) => n.roles.includes(u.perfil));
  app.innerHTML = html`
    <div class="shell">
      <aside class="side" id="side">
        <a class="brand" href="#/"><svg viewBox="0 0 32 32" width="30" height="30" aria-hidden="true"><path d="M16 4l10 5.8v11.6L16 27.2 6 21.4V9.8z" fill="none" stroke="var(--signal)" stroke-width="2.4" stroke-linejoin="round"/><circle cx="16" cy="15.6" r="3" fill="var(--signal)"/></svg><span><b>Astec</b><small>AGE Máquinas</small></span></a>
        <nav>${nav.map((n) => html`<a href="${n.href}" data-nav="${n.href}">${icon(n.icon)}<span>${n.label}</span></a>`)}</nav>
        <div class="who">
          <div class="who-name">${u.nome || u.email}</div>
          <div class="who-meta">${PERFIL[u.perfil]}${u.nome ? html` · ${u.email}` : ''}</div>
          <button class="btn ghost sm" id="logout">Sair</button>
        </div>
      </aside>
      <div class="content">
        <header class="topbar"><button class="icon-btn" id="menu" aria-label="Abrir menu">${icon('M4 6h16M4 12h16M4 18h16')}</button><b>Astec</b></header>
        <main id="main" tabindex="-1"></main>
      </div>
    </div>`.toString();
  $('#logout').onclick = () => logout();
  $('#menu').onclick = () => $('#side').classList.toggle('open');
  $('#side').addEventListener('click', (e) => { if (e.target.closest('a')) $('#side').classList.remove('open'); });
}

const ROUTES = [
  [/^\/?$/, dashboardPage, ['ADMIN', 'TECNICO', 'CLIENTE']],
  [/^\/chamados$/, ticketsPage, ['ADMIN', 'TECNICO', 'CLIENTE']],
  [/^\/chamados\/novo$/, ticketNewPage, ['CLIENTE']],
  [/^\/chamados\/(\d+)$/, ticketDetailPage, ['ADMIN', 'TECNICO', 'CLIENTE']],
  [/^\/equipamentos$/, equipamentosPage, ['ADMIN', 'TECNICO', 'CLIENTE']],
  [/^\/usuarios$/, usuariosPage, ['ADMIN']],
];

async function route() {
  if (!session.user) return;
  const main = $('#main'); if (!main) return;
  const path = (location.hash.replace(/^#/, '').split('?')[0]) || '/';
  const my = ++navToken;
  let found = null;
  for (const [re, page, roles] of ROUTES) { const m = path.match(re); if (m) { found = { page, roles, params: m.slice(1) }; break; } }
  $$('[data-nav]').forEach((a) => {
    const h = a.dataset.nav.slice(1);
    a.classList.toggle('active', h === '/' ? path === '/' : path.startsWith(h));
  });
  if (!found || !found.roles.includes(session.user.perfil)) {
    main.innerHTML = html`<div class="empty"><h1>Página não encontrada</h1><p>O endereço não existe ou seu perfil não tem acesso a ele.</p><a class="btn primary" href="#/">Voltar ao painel</a></div>`.toString();
    return;
  }
  main.innerHTML = '<div class="loading" role="status">Carregando…</div>';
  window.scrollTo(0, 0);
  try { await found.page(main, found.params, { alive: () => my === navToken }); }
  catch (e) { if (my === navToken) main.innerHTML = html`<div class="empty"><h1>Não foi possível carregar</h1><p>${e.message}</p><button class="btn primary" onclick="location.reload()">Tentar de novo</button></div>`.toString(); }
}

window.addEventListener('hashchange', route);
getToken() ? start() : renderLogin();

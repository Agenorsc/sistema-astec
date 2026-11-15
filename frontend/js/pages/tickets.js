import { api, todos } from '../api.js';
import { session, isRole } from '../app.js';
import { html, raw, $, $$, STATUS, URGENCIA, options, pager, statusBadge, urgBadge, fmtDateTime, relative } from '../lib.js';

export function slaCell(t) {
  if (t.status === 'FECHADO' || t.status === 'RESOLVIDO') return html`<span class="muted">Encerrado</span>`;
  const late = new Date(t.prazoResolucao) < new Date();
  return html`<span class="${late ? 'late' : ''}" title="${fmtDateTime(t.prazoResolucao)}">${late ? 'Atrasado ' : 'Vence '}${relative(t.prazoResolucao)}</span>`;
}

export function ticketTable(items, { showCliente = true } = {}) {
  if (!items.length) return html`<div class="empty small"><p>Nenhum chamado para mostrar.</p></div>`;
  return html`<div class="table-wrap"><table class="table">
    <thead><tr><th>Código</th><th>Chamado</th>${showCliente ? html`<th>Cliente</th>` : ''}<th>Status</th><th>Urgência</th><th>Prazo</th></tr></thead>
    <tbody>${items.map((t) => html`<tr class="click" data-href="#/chamados/${t.id}">
      <td><a class="code" href="#/chamados/${t.id}">${t.codigo}</a></td>
      <td><div class="t-title">${t.titulo}</div><div class="muted sm">${t.equipamentoNome}</div></td>
      ${showCliente ? html`<td>${t.clienteNome}</td>` : ''}
      <td>${statusBadge(t.status)}</td><td>${urgBadge(t.urgencia)}</td><td>${slaCell(t)}</td></tr>`)}</tbody></table></div>`;
}

document.addEventListener('click', (e) => {
  const row = e.target.closest('tr[data-href]');
  if (row && !e.target.closest('a,button')) location.hash = row.dataset.href;
});

export async function ticketsPage(main, _p, { alive }) {
  const staff = isRole('ADMIN', 'TECNICO');
  const admin = isRole('ADMIN');
  const initial = Object.fromEntries(new URLSearchParams(location.hash.split('?')[1] || ''));
  const [equips, tecnicos, clientes] = await Promise.all([
    todos(api.equipamentos),
    admin ? todos(api.usuarios, { perfil: 'TECNICO' }) : [],
    admin ? todos(api.usuarios, { perfil: 'CLIENTE' }) : [],
  ]);
  if (!alive()) return;
  const sel = (list, key, label, blank) => html`<label>${label}<select name="${key}"><option value="">${blank}</option>${list.map((x) => html`<option value="${x.id}" ${String(initial[key]) === String(x.id) ? raw('selected') : ''}>${x.nome}</option>`)}</select></label>`;

  main.innerHTML = html`
    <div class="page-head"><div><h1>Chamados</h1><p class="muted">${staff ? 'Todos os chamados de assistência técnica.' : 'Chamados abertos por você.'}</p></div>
      ${session.user.perfil === 'CLIENTE' ? html`<a class="btn primary" href="#/chamados/novo">Abrir chamado</a>` : ''}</div>
    <form id="filters" class="filters">
      <label>Status<select name="status">${options(STATUS, initial.status, 'Todos')}</select></label>
      <label>Urgência<select name="urgencia">${options(URGENCIA, initial.urgencia, 'Todas')}</select></label>
      <label>Equipamento<select name="equipamentoId"><option value="">Todos</option>${equips.map((x) => html`<option value="${x.id}" ${String(initial.equipamentoId) === String(x.id) ? raw('selected') : ''}>${x.nome}</option>`)}</select></label>
      ${admin ? sel(clientes, 'clienteId', 'Cliente', 'Todos') : ''}
      ${admin ? sel(tecnicos, 'tecnicoId', 'Técnico', 'Todos') : ''}
      <label>De<input type="date" name="dataInicio" value="${initial.dataInicio || ''}"></label>
      <label>Até<input type="date" name="dataFim" value="${initial.dataFim || ''}"></label>
      <button type="button" class="btn ghost" id="clear">Limpar</button>
    </form>
    <section class="panel flush"><div id="list"><div class="loading">Carregando…</div></div></section>`.toString();

  const form = $('#filters');
  const load = async (pagina = 0) => {
    const q = Object.fromEntries(new FormData(form).entries());
    const list = $('#list');
    try {
      if (q.dataInicio && q.dataFim && q.dataInicio > q.dataFim) throw new Error('A data inicial não pode ser posterior à final.');
      const page = await api.tickets({ ...q, pagina, tamanho: 15 });
      if (!alive()) return;
      list.innerHTML = html`${ticketTable(page.content, { showCliente: staff })}${pager(page)}`.toString();
      $$('[data-page]', list).forEach((b) => (b.onclick = () => load(Number(b.dataset.page))));
    } catch (e) { list.innerHTML = html`<div class="alert err">${e.message}</div>`.toString(); }
  };
  form.onchange = () => load(0);
  $('#clear').onclick = () => { form.reset(); $$('select,input', form).forEach((i) => (i.value = '')); load(0); };
  load(0);
}

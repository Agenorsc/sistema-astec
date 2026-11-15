// Utilitários: template seguro, formatadores, enums e helpers de UI.
class Safe { constructor(s) { this.s = s; } toString() { return this.s; } }
export const raw = (s) => new Safe(String(s ?? ''));
export const esc = (v) => String(v ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const part = (v) => (v instanceof Safe ? v.s : Array.isArray(v) ? v.map(part).join('') : v === false || v == null ? '' : esc(v));
// html`...` escapa tudo que for interpolado, exceto resultados de outro html`...`
export const html = (strings, ...vals) => new Safe(strings.reduce((a, s, i) => a + s + (i < vals.length ? part(vals[i]) : ''), ''));

export const $ = (sel, root = document) => root.querySelector(sel);
export const $$ = (sel, root = document) => [...root.querySelectorAll(sel)];

export const STATUS = {
  ABERTO: 'Aberto', EM_ANALISE: 'Em análise', AGUARDANDO_PECAS: 'Aguardando peças',
  EM_ATENDIMENTO: 'Em atendimento', RESOLVIDO: 'Resolvido', FECHADO: 'Fechado',
};
export const STATUS_ORDER = Object.keys(STATUS);
export const URGENCIA = { BAIXA: 'Baixa', MEDIA: 'Média', ALTA: 'Alta', CRITICA: 'Crítica' };
export const TIPO = { CORRETIVA: 'Corretiva', PREVENTIVA: 'Preventiva', DUVIDA_TECNICA: 'Dúvida técnica', PECA_DEFEITO: 'Peça com defeito' };
export const PERFIL = { CLIENTE: 'Cliente', TECNICO: 'Técnico', ADMIN: 'Administrador' };
// Espelha StatusTicket.validarTransicao do back end
export const TRANSICOES = {
  ABERTO: ['EM_ANALISE', 'EM_ATENDIMENTO'],
  EM_ANALISE: ['AGUARDANDO_PECAS', 'EM_ATENDIMENTO', 'RESOLVIDO'],
  AGUARDANDO_PECAS: ['EM_ATENDIMENTO', 'RESOLVIDO'],
  EM_ATENDIMENTO: ['AGUARDANDO_PECAS', 'RESOLVIDO'],
  RESOLVIDO: ['FECHADO', 'EM_ATENDIMENTO'],
  FECHADO: [],
};

// LocalDateTime do Java chega sem fuso: interpretar como horário local.
const parse = (s) => (s ? new Date(s) : null);
export const fmtDate = (s) => { const d = parse(s); return d ? d.toLocaleDateString('pt-BR') : '—'; };
export const fmtDateTime = (s) => { const d = parse(s); return d ? d.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' }) : '—'; };
export const money = (n) => Number(n ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
export const bytes = (n) => (n < 1024 ? `${n} B` : n < 1048576 ? `${(n / 1024).toFixed(1)} KB` : `${(n / 1048576).toFixed(1)} MB`);
export function relative(s) {
  const d = parse(s); if (!d) return '';
  const diff = d - Date.now(); const abs = Math.abs(diff);
  const m = Math.round(abs / 60000), h = Math.round(abs / 3600000), dd = Math.round(abs / 86400000);
  const t = m < 60 ? `${m} min` : h < 48 ? `${h} h` : `${dd} d`;
  return diff < 0 ? `há ${t}` : `em ${t}`;
}

export const badge = (kind, value, labels) => html`<span class="badge ${kind}-${value}">${labels[value] ?? value}</span>`;
export const statusBadge = (s) => badge('st', s, STATUS);
export const urgBadge = (u) => badge('urg', u, URGENCIA);

export function toast(msg, type = 'ok') {
  const el = document.createElement('div');
  el.className = `toast ${type}`; el.textContent = msg; el.setAttribute('role', type === 'err' ? 'alert' : 'status');
  $('#toasts').append(el);
  setTimeout(() => el.remove(), type === 'err' ? 6500 : 3500);
}

export function modal(content, { onOpen } = {}) {
  const dlg = $('#modal');
  dlg.innerHTML = content.toString();
  if (!dlg.open) dlg.showModal();
  dlg.onclick = (e) => { if (e.target === dlg || e.target.closest('[data-close]')) dlg.close(); };
  onOpen?.(dlg);
  return dlg;
}
export const closeModal = () => { const d = $('#modal'); if (d.open) d.close(); };

export function confirmDialog(title, message, action = 'Confirmar', danger = false) {
  return new Promise((resolve) => {
    const dlg = modal(html`
      <form method="dialog" class="dlg">
        <h2>${title}</h2><p>${message}</p>
        <div class="dlg-actions"><button type="button" class="btn ghost" data-close>Cancelar</button>
        <button class="btn ${danger ? 'danger' : 'primary'}" value="ok">${action}</button></div>
      </form>`);
    dlg.onclose = () => resolve(dlg.returnValue === 'ok');
    dlg.returnValue = '';
  });
}

export const formData = (form) => Object.fromEntries(new FormData(form).entries());
export const options = (map, selected, blank) =>
  html`${blank != null ? html`<option value="">${blank}</option>` : ''}${Object.entries(map).map(([k, v]) => html`<option value="${k}" ${String(selected) === k ? raw('selected') : ''}>${v}</option>`)}`;

export function pager(page, onGo) {
  if (!page || page.totalPages <= 1) return html`<div class="pager"><span>${page?.totalElements ?? 0} registro(s)</span></div>`;
  return html`<div class="pager"><span>${page.totalElements} registros · página ${page.page + 1} de ${page.totalPages}</span>
    <div><button class="btn ghost sm" data-page="${page.page - 1}" ${page.first ? raw('disabled') : ''}>Anterior</button>
    <button class="btn ghost sm" data-page="${page.page + 1}" ${page.last ? raw('disabled') : ''}>Próxima</button></div></div>`;
}

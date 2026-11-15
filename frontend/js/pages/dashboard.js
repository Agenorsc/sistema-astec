import { api, todos } from '../api.js';
import { session, isRole } from '../app.js';
import { html, STATUS, STATUS_ORDER } from '../lib.js';
import { ticketTable } from './tickets.js';

export async function dashboardPage(main, _p, { alive }) {
  const staff = isRole('ADMIN', 'TECNICO');
  const [recent, statusRows, sla, equip, own] = await Promise.all([
    api.tickets({ pagina: 0, tamanho: 6 }),
    staff ? api.statusStats() : null,
    staff ? api.slaStats() : null,
    staff ? api.equipStats() : null,
    staff ? null : todos(api.tickets),
  ]);
  if (!alive()) return;

  const counts = Object.fromEntries(STATUS_ORDER.map((s) => [s, 0]));
  if (staff) statusRows.forEach((r) => { counts[r.status] = r.quantidade; });
  else own.forEach((t) => { counts[t.status]++; });
  const total = Object.values(counts).reduce((a, b) => a + b, 0);
  const max = Math.max(1, ...Object.values(counts));
  const emAberto = total - counts.FECHADO - counts.RESOLVIDO;
  const equipRows = staff ? [...equip].sort((a, b) => b.quantidade - a.quantidade).slice(0, 6) : [];
  const equipMax = Math.max(1, ...equipRows.map((r) => r.quantidade));
  const atrasos = sla ? sla.respostasAtrasadas + sla.resolucoesAtrasadas : 0;
  const nome = session.user.nome || session.user.email;

  main.innerHTML = html`
    <div class="page-head">
      <div><h1>Painel</h1><p class="muted">${staff ? 'Situação da assistência técnica agora.' : `Olá, ${nome}. Acompanhe seus chamados.`}</p></div>
      ${session.user.perfil === 'CLIENTE' ? html`<a class="btn primary" href="#/chamados/novo">Abrir chamado</a>` : ''}
    </div>

    <section class="kpis">
      <div class="kpi"><span class="kpi-n">${emAberto}</span><span class="kpi-l">chamados em andamento</span></div>
      <div class="kpi"><span class="kpi-n">${counts.RESOLVIDO}</span><span class="kpi-l">aguardando fechamento</span></div>
      ${staff ? html`
        <div class="kpi ${sla.respostasAtrasadas ? 'warn' : ''}"><span class="kpi-n">${sla.respostasAtrasadas}</span><span class="kpi-l">primeiras respostas atrasadas</span></div>
        <div class="kpi ${sla.resolucoesAtrasadas ? 'warn' : ''}"><span class="kpi-n">${sla.resolucoesAtrasadas}</span><span class="kpi-l">resoluções atrasadas</span></div>` : html`
        <div class="kpi"><span class="kpi-n">${counts.FECHADO}</span><span class="kpi-l">chamados fechados</span></div>
        <div class="kpi"><span class="kpi-n">${total}</span><span class="kpi-l">chamados no total</span></div>`}
    </section>
    ${staff && atrasos ? html`<div class="alert warn">Há ${atrasos} prazo(s) de SLA vencido(s). <a href="#/chamados">Ver chamados</a></div>` : ''}

    <div class="grid-2">
      <section class="panel">
        <h2>Chamados por etapa</h2>
        <ol class="rail">
          ${STATUS_ORDER.map((s) => html`
            <li><a href="#/chamados?status=${s}" class="rail-row">
              <span class="rail-l">${STATUS[s]}</span>
              <span class="rail-bar"><i class="st-bg-${s}" style="width:${(counts[s] / max) * 100}%"></i></span>
              <span class="rail-n">${counts[s]}</span>
            </a></li>`)}
        </ol>
      </section>
      ${staff ? html`
      <section class="panel">
        <h2>Equipamentos com mais chamados</h2>
        ${equipRows.length ? html`<ol class="rail">${equipRows.map((r) => html`
          <li><div class="rail-row">
            <span class="rail-l" title="${r.equipamento?.nome}">${r.equipamento?.nome ?? '—'}</span>
            <span class="rail-bar"><i class="bar-signal" style="width:${(r.quantidade / equipMax) * 100}%"></i></span>
            <span class="rail-n">${r.quantidade}</span></div></li>`)}</ol>` : html`<p class="muted">Nenhum chamado registrado ainda.</p>`}
      </section>` : html`
      <section class="panel">
        <h2>Próximos passos</h2>
        <ul class="plain">
          <li>${counts.RESOLVIDO + counts.FECHADO ? 'Avalie os atendimentos resolvidos abrindo o chamado.' : 'Quando um atendimento for resolvido, você poderá avaliá-lo.'}</li>
          <li>Cadastre seus equipamentos antes de abrir um chamado.</li>
        </ul>
        <a class="btn ghost" href="#/equipamentos">Ver meus equipamentos</a>
      </section>`}
    </div>

    <section class="panel flush">
      <div class="panel-head"><h2>Chamados recentes</h2><a href="#/chamados">Ver todos</a></div>
      ${ticketTable(recent.content, { showCliente: staff })}
    </section>`.toString();
}

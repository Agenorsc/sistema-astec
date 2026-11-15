import { api, todos, ApiError } from '../api.js';
import { session, isRole, go } from '../app.js';
import { html, raw, $, $$, STATUS, STATUS_ORDER, TRANSICOES, TIPO, URGENCIA, PERFIL, statusBadge, urgBadge, fmtDateTime, relative, money, bytes, toast, confirmDialog, formData } from '../lib.js';

const notFound = (p) => p.catch((e) => { if (e.status === 404) return null; throw e; });

export async function ticketDetailPage(main, [idStr], { alive }) {
  const id = Number(idStr);
  const st = { tab: 'conversa' };
  const staff = isRole('ADMIN', 'TECNICO');
  const admin = isRole('ADMIN');
  const cliente = isRole('CLIENTE');

  const [ticket, historico, mensagens, anexos, laudo, avaliacao, tecnicos] = await Promise.all([
    api.ticket(id), api.historico(id), api.mensagens(id), api.anexos(id),
    notFound(api.laudo(id)), notFound(api.avaliacao(id)),
    admin ? todos(api.usuarios, { perfil: 'TECNICO' }) : [],
  ]);
  if (!alive()) return;
  const data = { ticket, historico, mensagens, anexos, laudo, avaliacao };

  async function reload(keys = ['ticket', 'historico']) {
    const loaders = {
      ticket: () => api.ticket(id), historico: () => api.historico(id), mensagens: () => api.mensagens(id),
      anexos: () => api.anexos(id), laudo: () => notFound(api.laudo(id)), avaliacao: () => notFound(api.avaliacao(id)),
    };
    const res = await Promise.all(keys.map((k) => loaders[k]()));
    keys.forEach((k, i) => (data[k] = res[i]));
    if (alive()) draw();
  }
  const act = async (fn, okMsg, keys) => {
    try { await fn(); if (okMsg) toast(okMsg); await reload(keys); }
    catch (e) { toast(e.message, 'err'); }
  };

  const isMine = () => data.ticket.tecnicoId != null && data.ticket.tecnicoId === session.user.id;
  const canLaudo = () => staff && data.ticket.status === 'EM_ATENDIMENTO' && !data.laudo && (admin || isMine());
  const canAvaliar = () => cliente && ['RESOLVIDO', 'FECHADO'].includes(data.ticket.status) && !data.avaliacao;

  function draw() {
    const t = data.ticket;
    const tabs = [
      ['conversa', `Conversa (${data.mensagens.length})`], ['historico', 'Histórico'], ['anexos', `Anexos (${data.anexos.length})`],
      ['laudo', data.laudo ? 'Laudo técnico' : 'Laudo'], ['avaliacao', data.avaliacao ? 'Avaliação' : 'Avaliação'],
    ];
    const idx = STATUS_ORDER.indexOf(t.status);
    main.innerHTML = html`
      <div class="page-head"><div><a class="back" href="#/chamados">Chamados</a>
        <h1><span class="code lg">${t.codigo}</span> ${t.titulo}</h1>
        <div class="badges">${statusBadge(t.status)} ${urgBadge(t.urgencia)} <span class="muted">${TIPO[t.tipoAtendimento]}</span></div></div></div>

      <ol class="stations" aria-label="Etapas do chamado">
        ${STATUS_ORDER.map((s, i) => html`<li class="${i < idx ? 'done' : i === idx ? 'now' : ''}"><i></i><span>${STATUS[s]}</span></li>`)}
      </ol>

      <div class="detail-grid">
        <section class="panel">
          <h2>Descrição</h2>
          <p class="prose">${t.descricao}</p>
          <dl class="facts">
            <div><dt>Cliente</dt><dd>${t.clienteNome}</dd></div>
            <div><dt>Equipamento</dt><dd>${t.equipamentoNome}</dd></div>
            <div><dt>Técnico</dt><dd>${t.tecnicoNome || html`<span class="muted">Não atribuído</span>`}</dd></div>
            <div><dt>Aberto em</dt><dd>${fmtDateTime(t.dataCriacao)}</dd></div>
          </dl>
        </section>
        <aside class="panel">
          <h2>Prazos</h2>
          <dl class="facts one">${sla('Primeira resposta', t.prazoPrimeiraResposta, t.dataPrimeiraResposta)}${sla('Resolução', t.prazoResolucao, t.dataResolucao)}
            ${t.dataFechamento ? html`<div><dt>Fechado em</dt><dd>${fmtDateTime(t.dataFechamento)}</dd></div>` : ''}</dl>
          ${staff ? actions(t) : ''}
        </aside>
      </div>

      <div class="tabs" role="tablist">${tabs.map(([k, l]) => html`<button role="tab" class="${st.tab === k ? 'on' : ''}" aria-selected="${st.tab === k}" data-tab="${k}">${l}</button>`)}</div>
      <section class="panel" id="tab">${tabBody(t)}</section>`.toString();
    bind();
  }

  function sla(label, prazo, feito) {
    const late = !feito && new Date(prazo) < new Date() && !['RESOLVIDO', 'FECHADO'].includes(data.ticket.status);
    return html`<div><dt>${label}</dt><dd>${fmtDateTime(prazo)}
      <div class="sm ${late ? 'late' : 'muted'}">${feito ? `Cumprido em ${fmtDateTime(feito)}` : late ? `Atrasado (${relative(prazo)})` : `Vence ${relative(prazo)}`}</div></dd></div>`;
  }

  function actions(t) {
    const next = TRANSICOES[t.status];
    const showAssign = t.status !== 'FECHADO';
    const canTake = !admin && session.user.id != null && !isMine() && showAssign;
    return html`
      <h2 class="mt">Ações</h2>
      ${next.length ? html`<form id="statusForm" class="form tight">
        <label>Mudar status para<select name="status" required>${next.map((s) => html`<option value="${s}">${STATUS[s]}</option>`)}</select></label>
        <label><span>Observação <span class="muted">(opcional)</span></span><input name="observacao" maxlength="1000"></label>
        <button class="btn primary">Atualizar status</button></form>` : html`<p class="muted">Chamado fechado: o status não pode mais mudar.</p>`}
      ${admin && showAssign ? html`<form id="assignForm" class="form tight mt">
        <label>Técnico responsável<select name="tecnicoId" required><option value="">Escolha…</option>${tecnicos.map((u) => html`<option value="${u.id}" ${u.id === t.tecnicoId ? raw('selected') : ''}>${u.nome}</option>`)}</select></label>
        <button class="btn ghost">Atribuir técnico</button></form>` : ''}
      ${canTake ? html`<button class="btn ghost mt" id="take">Assumir este chamado</button>` : ''}
      ${!admin && session.user.id == null && showAssign ? html`<p class="hint mt">Para assumir chamados, o back end precisa expor <code>GET /api/auth/me</code> (veja o README do front end).</p>` : ''}`;
  }

  function tabBody(t) {
    switch (st.tab) {
      case 'conversa': return html`
        <div class="chat" id="chat">${data.mensagens.length ? data.mensagens.map((m) => html`
          <div class="msg ${m.autorId === session.user.id || (m.autorNome === session.user.nome && session.user.nome) ? 'mine' : ''}">
            <div class="msg-h"><b>${m.autorNome}</b><span class="muted">${fmtDateTime(m.dataEnvio)}</span></div><p>${m.texto}</p></div>`) : html`<p class="muted">Ainda não há mensagens neste chamado.</p>`}</div>
        ${t.status === 'FECHADO' ? html`<p class="muted">Chamado fechado: não é possível enviar mensagens.</p>` : html`
        <form id="msgForm" class="compose"><textarea name="texto" rows="2" maxlength="10000" required placeholder="Escreva uma mensagem…"></textarea><button class="btn primary">Enviar</button></form>`}`;
      case 'historico': return html`<ol class="timeline">${data.historico.map((h) => html`
        <li><div class="tl-dot"></div><div><b>${h.statusAnterior && h.statusAnterior !== h.statusNovo ? html`${STATUS[h.statusAnterior]} → ${STATUS[h.statusNovo]}` : STATUS[h.statusNovo]}</b>
        <div class="muted sm">${fmtDateTime(h.dataAlteracao)} · ${h.usuarioNome}</div>${h.observacao ? html`<p>${h.observacao}</p>` : ''}</div></li>`)}</ol>`;
      case 'anexos': return html`
        <form id="upForm" class="upload"><input type="file" name="arquivo" accept=".jpg,.pdf,.mp4" required><button class="btn ghost">Enviar arquivo</button><span class="muted sm">JPG, PDF ou MP4, até 20 MB.</span></form>
        ${data.anexos.length ? html`<ul class="files">${data.anexos.map((a) => html`<li><div><b>${a.nomeArquivo}</b><div class="muted sm">${a.tipoArquivo} · ${bytes(a.tamanhoBytes)}</div></div>
          <div><button class="btn ghost sm" data-dl="${a.id}" data-name="${a.nomeArquivo}">Baixar</button>${staff ? html` <button class="btn ghost sm danger-t" data-rm="${a.id}" data-name="${a.nomeArquivo}">Remover</button>` : ''}</div></li>`)}</ul>` : html`<p class="muted">Nenhum anexo enviado.</p>`}`;
      case 'laudo': return data.laudo ? laudoView(data.laudo) : canLaudo() ? laudoForm() : html`<p class="muted">${t.status === 'EM_ATENDIMENTO' ? (staff ? 'Somente o técnico responsável (ou um administrador) registra o laudo.' : 'O laudo aparece aqui quando o técnico concluir o atendimento.') : 'O laudo técnico é registrado quando o chamado está em atendimento.'}</p>`;
      case 'avaliacao': return data.avaliacao ? html`<div class="stars big" aria-label="Nota ${data.avaliacao.nota} de 5">${stars(data.avaliacao.nota)}</div>
        <p>${data.avaliacao.comentario || html`<span class="muted">Sem comentário.</span>`}</p><p class="muted sm">Avaliado em ${fmtDateTime(data.avaliacao.dataAvaliacao)}</p>`
        : canAvaliar() ? avaliacaoForm() : html`<p class="muted">${cliente ? 'Você poderá avaliar o atendimento quando o chamado for resolvido.' : 'O cliente ainda não avaliou este atendimento.'}</p>`;
    }
  }
  const stars = (n) => [1, 2, 3, 4, 5].map((i) => html`<span class="${i <= n ? 'on' : ''}">★</span>`);

  const laudoView = (l) => html`
    <div class="laudo"><dl class="facts"><div><dt>Registrado em</dt><dd>${fmtDateTime(l.dataRegistro)}</dd></div><div><dt>Horas trabalhadas</dt><dd>${String(l.horasTrabalhadas).replace('.', ',')} h</dd></div></dl>
    <h3>Diagnóstico e solução</h3><p class="prose">${l.diagnosticoSolucao}</p>
    <h3>Peças substituídas</h3>
    ${l.pecasSubstituidas.length ? html`<div class="table-wrap"><table class="table"><thead><tr><th>Peça</th><th class="num">Qtd.</th><th class="num">Valor unit.</th><th class="num">Subtotal</th></tr></thead>
      <tbody>${l.pecasSubstituidas.map((p) => html`<tr><td>${p.nomePeca}</td><td class="num">${p.quantidade}</td><td class="num">${money(p.valorUnitario)}</td><td class="num">${money(p.subtotal)}</td></tr>`)}</tbody>
      <tfoot><tr><td colspan="3">Total em peças</td><td class="num"><b>${money(l.totalPecas)}</b></td></tr></tfoot></table></div>` : html`<p class="muted">Nenhuma peça foi trocada.</p>`}</div>`;

  const pecaRow = () => html`<div class="peca"><input name="nomePeca" placeholder="Nome da peça" maxlength="255" required><input name="quantidade" type="number" min="1" value="1" required aria-label="Quantidade"><input name="valorUnitario" type="number" min="0" step="0.01" placeholder="0,00" required aria-label="Valor unitário (R$)"><button type="button" class="icon-btn" data-delpeca aria-label="Remover peça">✕</button></div>`;
  const laudoForm = () => html`
    <form id="laudoForm" class="form">
      <div class="alert info">Ao registrar o laudo, o chamado passa automaticamente para <b>Resolvido</b> e o cliente é avisado.</div>
      <label>Diagnóstico e solução<textarea name="diagnosticoSolucao" rows="5" required></textarea></label>
      <label class="w-sm">Horas trabalhadas<input name="horasTrabalhadas" type="number" min="0" step="0.25" required></label>
      <fieldset><legend>Peças substituídas</legend><div id="pecas"></div>
        <div class="between"><button type="button" class="btn ghost sm" id="addPeca">Adicionar peça</button><b id="total">Total: ${money(0)}</b></div></fieldset>
      <div class="actions"><button class="btn primary">Registrar laudo</button></div></form>`;
  const avaliacaoForm = () => html`
    <form id="avForm" class="form narrow"><fieldset class="rate"><legend>Como foi o atendimento?</legend>
      ${[1, 2, 3, 4, 5].map((n) => html`<input type="radio" name="nota" id="n${n}" value="${n}" ${n === 5 ? raw('required') : ''}><label for="n${n}" title="${n} de 5">★<span class="sr">${n}</span></label>`)}</fieldset>
      <label><span>Comentário <span class="muted">(opcional)</span></span><textarea name="comentario" rows="3" maxlength="500"></textarea></label>
      <button class="btn primary">Enviar avaliação</button></form>`;

  function bind() {
    $$('[data-tab]').forEach((b) => (b.onclick = () => { st.tab = b.dataset.tab; draw(); }));
    const chat = $('#chat'); if (chat) chat.scrollTop = chat.scrollHeight;
    const on = (sel, ev, fn) => { const el = $(sel); if (el) el[ev] = fn; };

    on('#statusForm', 'onsubmit', (e) => { e.preventDefault(); const d = formData(e.target); act(() => api.status(id, d.status, d.observacao), 'Status atualizado.', ['ticket', 'historico']); });
    on('#assignForm', 'onsubmit', (e) => { e.preventDefault(); act(() => api.atribuir(id, Number(formData(e.target).tecnicoId)), 'Técnico atribuído.', ['ticket', 'historico']); });
    on('#take', 'onclick', () => act(() => api.atribuir(id, session.user.id), 'Você assumiu o chamado.', ['ticket', 'historico']));
    on('#msgForm', 'onsubmit', (e) => { e.preventDefault(); const texto = formData(e.target).texto; act(() => api.enviarMensagem(id, texto), null, ['mensagens', 'ticket']); });
    on('#upForm', 'onsubmit', (e) => {
      e.preventDefault(); const file = e.target.arquivo.files[0];
      if (!file) return;
      if (!/\.(jpg|pdf|mp4)$/i.test(file.name)) return toast('Envie apenas arquivos JPG, PDF ou MP4.', 'err');
      if (file.size > 20 * 1024 * 1024) return toast('O arquivo excede o limite de 20 MB.', 'err');
      act(() => api.enviarAnexo(id, file), 'Arquivo enviado.', ['anexos']);
    });
    $$('[data-dl]').forEach((b) => (b.onclick = async () => {
      try {
        const blob = await api.baixarAnexo(id, b.dataset.dl); const url = URL.createObjectURL(blob);
        const a = Object.assign(document.createElement('a'), { href: url, download: b.dataset.name }); a.click(); setTimeout(() => URL.revokeObjectURL(url), 5000);
      } catch (e) { toast(e.message, 'err'); }
    }));
    $$('[data-rm]').forEach((b) => (b.onclick = async () => {
      if (await confirmDialog('Remover anexo', `Remover “${b.dataset.name}” deste chamado? Isso não pode ser desfeito.`, 'Remover', true))
        act(() => api.removerAnexo(id, b.dataset.rm), 'Anexo removido.', ['anexos']);
    }));

    const pecas = $('#pecas');
    if (pecas) {
      const total = () => { const sum = $$('.peca', pecas).reduce((a, r) => a + (Number(r.children[1].value) || 0) * (Number(r.children[2].value) || 0), 0); $('#total').textContent = `Total: ${money(sum)}`; };
      const add = () => { pecas.insertAdjacentHTML('beforeend', pecaRow().toString()); };
      pecas.oninput = total;
      pecas.onclick = (e) => { if (e.target.closest('[data-delpeca]')) { e.target.closest('.peca').remove(); total(); } };
      $('#addPeca').onclick = add;
    }
    on('#laudoForm', 'onsubmit', (e) => {
      e.preventDefault(); const f = e.target;
      const body = { diagnosticoSolucao: f.diagnosticoSolucao.value, horasTrabalhadas: Number(f.horasTrabalhadas.value),
        pecasSubstituidas: $$('.peca', f).map((r) => ({ nomePeca: r.children[0].value, quantidade: Number(r.children[1].value), valorUnitario: Number(r.children[2].value) })) };
      act(() => api.criarLaudo(id, body), 'Laudo registrado. Chamado resolvido.', ['ticket', 'historico', 'laudo']);
    });
    on('#avForm', 'onsubmit', (e) => {
      e.preventDefault(); const d = formData(e.target);
      if (!d.nota) return toast('Escolha uma nota de 1 a 5.', 'err');
      act(() => api.criarAvaliacao(id, { nota: Number(d.nota), comentario: d.comentario || null }), 'Avaliação enviada. Obrigado!', ['avaliacao']);
    });
  }
  draw();
}

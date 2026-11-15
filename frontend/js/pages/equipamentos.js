import { api, todos } from '../api.js';
import { isRole, session } from '../app.js';
import { html, raw, $, $$, modal, closeModal, confirmDialog, formData, pager, toast } from '../lib.js';

export async function equipamentosPage(main, _p, { alive }) {
  const admin = isRole('ADMIN'), cliente = isRole('CLIENTE');
  const canEdit = admin || cliente;
  const clientes = admin ? await todos(api.usuarios, { perfil: 'CLIENTE' }) : [];
  if (!alive()) return;

  main.innerHTML = html`
    <div class="page-head"><div><h1>Equipamentos</h1><p class="muted">${cliente ? 'Máquinas cadastradas em seu nome.' : 'Máquinas atendidas pela assistência técnica.'}</p></div>
      ${canEdit ? html`<button class="btn primary" id="new">Cadastrar equipamento</button>` : ''}</div>
    <section class="panel flush"><div id="list"><div class="loading">Carregando…</div></div></section>`.toString();

  const form = (e = {}) => html`
    <form id="eqForm" class="dlg form">
      <h2>${e.id ? 'Editar equipamento' : 'Cadastrar equipamento'}</h2>
      <label>Nome<input name="nome" maxlength="255" required value="${e.nome || ''}"></label>
      <div class="row2"><label>Modelo<input name="modelo" maxlength="255" value="${e.modelo || ''}"></label>
      <label>Número de série<input name="numeroSerie" maxlength="100" value="${e.numeroSerie || ''}"></label></div>
      ${admin ? html`<label>Cliente<select name="clienteId" required><option value="">Escolha…</option>${clientes.map((c) => html`<option value="${c.id}" ${c.id === e.clienteId ? raw('selected') : ''}>${c.nome}</option>`)}</select></label>` : ''}
      <div id="err" class="alert err" hidden></div>
      <div class="dlg-actions"><button type="button" class="btn ghost" data-close>Cancelar</button><button class="btn primary">Salvar</button></div>
    </form>`;

  const openForm = (e) => {
    const dlg = modal(form(e));
    $('#eqForm', dlg).onsubmit = async (ev) => {
      ev.preventDefault();
      const d = formData(ev.target);
      const body = { nome: d.nome, modelo: d.modelo || null, numeroSerie: d.numeroSerie || null, clienteId: d.clienteId ? Number(d.clienteId) : null };
      try { e ? await api.atualizarEquipamento(e.id, body) : await api.criarEquipamento(body); closeModal(); toast('Equipamento salvo.'); load(0); }
      catch (ex) { const er = $('#err', dlg); er.textContent = ex.message; er.hidden = false; }
    };
  };

  let rows = [];
  async function load(pagina = 0) {
    const list = $('#list');
    try {
      const page = await api.equipamentos({ pagina, tamanho: 15 });
      if (!alive()) return; rows = page.content;
      list.innerHTML = (page.content.length ? html`<div class="table-wrap"><table class="table">
        <thead><tr><th>Equipamento</th><th>Modelo</th><th>Nº de série</th>${!cliente ? html`<th>Cliente</th>` : ''}${canEdit ? html`<th></th>` : ''}</tr></thead>
        <tbody>${page.content.map((e) => html`<tr><td><b>${e.nome}</b></td><td>${e.modelo || '—'}</td><td class="code">${e.numeroSerie || '—'}</td>${!cliente ? html`<td>${e.clienteNome || '—'}</td>` : ''}
          ${canEdit ? html`<td class="act"><button class="btn ghost sm" data-edit="${e.id}">Editar</button>${admin ? html` <button class="btn ghost sm danger-t" data-del="${e.id}">Remover</button>` : ''}</td>` : ''}</tr>`)}</tbody></table></div>`
        : html`<div class="empty small"><p>${cliente ? 'Você ainda não cadastrou nenhum equipamento.' : 'Nenhum equipamento cadastrado.'}</p></div>`).toString() + pager(page).toString();
      $$('[data-page]', list).forEach((b) => (b.onclick = () => load(Number(b.dataset.page))));
      $$('[data-edit]', list).forEach((b) => (b.onclick = () => openForm(rows.find((r) => r.id === Number(b.dataset.edit)))));
      $$('[data-del]', list).forEach((b) => (b.onclick = async () => {
        const e = rows.find((r) => r.id === Number(b.dataset.del));
        if (!(await confirmDialog('Remover equipamento', `Remover “${e.nome}”? Equipamentos com chamados vinculados podem não ser removidos.`, 'Remover', true))) return;
        try { await api.removerEquipamento(e.id); toast('Equipamento removido.'); load(0); } catch (ex) { toast(ex.status === 409 ? 'Este equipamento tem chamados vinculados e não pode ser removido.' : ex.message, 'err'); }
      }));
    } catch (e) { list.innerHTML = html`<div class="alert err">${e.message}</div>`.toString(); }
  }
  $('#new')?.addEventListener('click', () => openForm());
  load(0);
}

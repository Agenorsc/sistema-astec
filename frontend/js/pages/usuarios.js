import { api } from '../api.js';
import { session } from '../app.js';
import { html, $, $$, PERFIL, options, modal, closeModal, confirmDialog, formData, pager, toast } from '../lib.js';

export async function usuariosPage(main, _p, { alive }) {
  main.innerHTML = html`
    <div class="page-head"><div><h1>Usuários</h1><p class="muted">Clientes, técnicos e administradores do sistema.</p></div><button class="btn primary" id="new">Cadastrar usuário</button></div>
    <form id="filters" class="filters"><label>Perfil<select name="perfil">${options(PERFIL, '', 'Todos')}</select></label>
      <label>Nome<input name="nome" type="search" placeholder="Buscar por nome"></label></form>
    <section class="panel flush"><div id="list"><div class="loading">Carregando…</div></div></section>`.toString();

  let rows = [], timer;
  const form = $('#filters');
  async function load(pagina = 0) {
    const list = $('#list');
    try {
      const q = Object.fromEntries(new FormData(form).entries());
      const page = await api.usuarios({ ...q, pagina, tamanho: 15 });
      if (!alive()) return; rows = page.content;
      list.innerHTML = (page.content.length ? html`<div class="table-wrap"><table class="table"><thead><tr><th>Nome</th><th>E-mail</th><th>Perfil</th><th></th></tr></thead>
        <tbody>${page.content.map((u) => html`<tr><td><b>${u.nome}</b></td><td>${u.email}</td>
          <td><select class="inline" data-perfil="${u.id}" aria-label="Perfil de ${u.nome}">${options(PERFIL, u.perfil)}</select></td>
          <td class="act"><button class="btn ghost sm danger-t" data-del="${u.id}">Remover</button></td></tr>`)}</tbody></table></div>`
        : html`<div class="empty small"><p>Nenhum usuário encontrado.</p></div>`).toString() + pager(page).toString();
      $$('[data-page]', list).forEach((b) => (b.onclick = () => load(Number(b.dataset.page))));
      $$('[data-perfil]', list).forEach((s) => {
        const u = rows.find((r) => r.id === Number(s.dataset.perfil));
        s.onchange = async () => {
          if (!(await confirmDialog('Alterar perfil', `Mudar o perfil de ${u.nome} para ${PERFIL[s.value]}?`, 'Alterar'))) { s.value = u.perfil; return; }
          try { await api.perfilUsuario(u.id, s.value); toast('Perfil atualizado.'); load(page.page); } catch (e) { toast(e.message, 'err'); s.value = u.perfil; }
        };
      });
      $$('[data-del]', list).forEach((b) => (b.onclick = async () => {
        const u = rows.find((r) => r.id === Number(b.dataset.del));
        if (u.email === session.user.email) return toast('Você não pode remover o próprio usuário logado por aqui.', 'err');
        if (!(await confirmDialog('Remover usuário', `Remover ${u.nome} (${u.email})? Isso não pode ser desfeito.`, 'Remover', true))) return;
        try { await api.removerUsuario(u.id); toast('Usuário removido.'); load(0); }
        catch (e) { toast(e.status === 409 ? 'Este usuário tem registros vinculados (chamados, equipamentos) ou é o último administrador.' : e.message, 'err'); }
      }));
    } catch (e) { list.innerHTML = html`<div class="alert err">${e.message}</div>`.toString(); }
  }
  form.onsubmit = (e) => e.preventDefault();
  form.oninput = () => { clearTimeout(timer); timer = setTimeout(() => load(0), 300); };

  $('#new').onclick = () => {
    const dlg = modal(html`<form id="uForm" class="dlg form"><h2>Cadastrar usuário</h2>
      <label>Nome<input name="nome" maxlength="255" required></label>
      <label>E-mail<input name="email" type="email" maxlength="255" required autocomplete="off"></label>
      <label>Senha<input name="senha" type="password" minlength="12" maxlength="72" required autocomplete="new-password"><span class="hint">Mínimo de 12 caracteres.</span></label>
      <label>Perfil<select name="perfil" required>${options(PERFIL, 'CLIENTE')}</select></label>
      <div id="err" class="alert err" hidden></div>
      <div class="dlg-actions"><button type="button" class="btn ghost" data-close>Cancelar</button><button class="btn primary">Cadastrar</button></div></form>`);
    $('#uForm', dlg).onsubmit = async (ev) => {
      ev.preventDefault();
      try { await api.criarUsuario(formData(ev.target)); closeModal(); toast('Usuário cadastrado.'); load(0); }
      catch (e) { const er = $('#err', dlg); er.textContent = e.message; er.hidden = false; }
    };
  };
  load(0);
}

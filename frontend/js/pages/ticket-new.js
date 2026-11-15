import { api, todos } from '../api.js';
import { go } from '../app.js';
import { html, $, options, TIPO, URGENCIA, formData, toast } from '../lib.js';

export async function ticketNewPage(main, _p, { alive }) {
  const equips = await todos(api.equipamentos);
  if (!alive()) return;
  if (!equips.length) {
    main.innerHTML = html`<div class="empty"><h1>Cadastre um equipamento primeiro</h1><p>Todo chamado é aberto para uma máquina sua. Você ainda não tem nenhuma cadastrada.</p><a class="btn primary" href="#/equipamentos">Cadastrar equipamento</a></div>`.toString();
    return;
  }
  main.innerHTML = html`
    <div class="page-head"><div><a class="back" href="#/chamados">Chamados</a><h1>Abrir chamado</h1></div></div>
    <form id="f" class="panel form narrow">
      <label>Equipamento<select name="equipamentoId" required>${equips.map((e) => html`<option value="${e.id}">${e.nome}${e.numeroSerie ? ` (${e.numeroSerie})` : ''}</option>`)}</select></label>
      <div class="row2">
        <label>Tipo de atendimento<select name="tipoAtendimento" required>${options(TIPO, 'CORRETIVA')}</select></label>
        <label>Urgência<select name="urgencia" required>${options(URGENCIA, 'MEDIA')}</select></label>
      </div>
      <p class="hint" id="sla-hint"></p>
      <label>Título<input name="titulo" maxlength="255" required placeholder="Ex.: Vibração excessiva no fuso principal"></label>
      <label>Descrição<textarea name="descricao" rows="6" maxlength="10000" required placeholder="O que acontece, quando começou e o que já foi tentado."></textarea></label>
      <div id="err" class="alert err" hidden></div>
      <div class="actions"><a class="btn ghost" href="#/chamados">Cancelar</a><button class="btn primary" id="go">Abrir chamado</button></div>
    </form>`.toString();
  const HINT = { CRITICA: 'Crítica: primeira resposta em 1 h e resolução em 4 h.', ALTA: 'Alta: primeira resposta em 4 h e resolução em 24 h.', MEDIA: 'Média: primeira resposta em 8 h e resolução em 72 h.', BAIXA: 'Baixa: primeira resposta em 24 h e resolução em 120 h.' };
  const f = $('#f');
  const hint = () => ($('#sla-hint').textContent = 'Prazos padrão — ' + HINT[f.urgencia.value]);
  f.urgencia.onchange = hint; hint();
  f.onsubmit = async (e) => {
    e.preventDefault();
    const d = formData(f); const btn = $('#go'); btn.disabled = true; $('#err').hidden = true;
    try {
      const t = await api.criarTicket({ ...d, equipamentoId: Number(d.equipamentoId) });
      toast(`Chamado ${t.codigo} aberto.`); go(`#/chamados/${t.id}`);
    } catch (ex) { $('#err').textContent = ex.message; $('#err').hidden = false; btn.disabled = false; }
  };
}

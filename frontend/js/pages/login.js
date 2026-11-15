import { api, setToken } from '../api.js';
import { html, $, formData } from '../lib.js';

export function loginPage(root, { message, onLogin }) {
  root.innerHTML = html`
    <div class="login">
      <section class="login-art" aria-hidden="true">
        <svg viewBox="0 0 400 400" class="gear">
          <g fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="200" cy="200" r="150"/><circle cx="200" cy="200" r="110" stroke-dasharray="3 9"/>
            <circle cx="200" cy="200" r="46"/><path d="M200 30v50M200 320v50M30 200h50M320 200h50M80 80l35 35M285 285l35 35M80 320l35-35M285 115l35-35"/>
            <path d="M200 154l40 23v46l-40 23-40-23v-46z" stroke="var(--signal)" stroke-width="3"/>
          </g>
        </svg>
        <p>Chamados, laudos e peças de cada máquina, em um só lugar.</p>
      </section>
      <section class="login-card">
        <h1>Entrar no Astec</h1>
        <p class="muted">Use o e-mail e a senha cadastrados pela AGE Máquinas e Manutenção.</p>
        ${message ? html`<div class="alert err" role="alert">${message}</div>` : ''}
        <form id="f" class="form">
          <label>E-mail<input name="email" type="email" required autocomplete="username" autofocus></label>
          <label>Senha<input name="senha" type="password" required autocomplete="current-password"></label>
          <div id="err" class="alert err" role="alert" hidden></div>
          <button class="btn primary lg" id="go">Entrar</button>
        </form>
      </section>
    </div>`.toString();
  $('#f').onsubmit = async (e) => {
    e.preventDefault();
    const { email, senha } = formData(e.target);
    const btn = $('#go'), err = $('#err');
    btn.disabled = true; btn.textContent = 'Entrando…'; err.hidden = true;
    try { const t = await api.login(email, senha); setToken(t.accessToken); onLogin(); }
    catch (ex) { err.textContent = ex.status === 401 ? 'E-mail ou senha incorretos.' : ex.message; err.hidden = false; btn.disabled = false; btn.textContent = 'Entrar'; }
  };
}

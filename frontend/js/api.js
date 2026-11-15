// Cliente HTTP do back end Astec. O token fica em sessionStorage (some ao fechar a aba).
const KEY = 'astec.token';
let token = null;
try { token = sessionStorage.getItem(KEY); } catch {}
let onUnauthorized = () => {};

export const setUnauthorizedHandler = (fn) => { onUnauthorized = fn; };
export const getToken = () => token;
export function setToken(t) {
  token = t;
  try { t ? sessionStorage.setItem(KEY, t) : sessionStorage.removeItem(KEY); } catch {}
}

export function decodeToken(t = token) {
  try {
    const p = JSON.parse(decodeURIComponent(escape(atob(t.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))));
    return { email: p.sub, roles: p.roles || [], exp: p.exp * 1000 };
  } catch { return null; }
}

export class ApiError extends Error {
  constructor(status, message) { super(message); this.status = status; }
}

async function request(method, path, { body, query, form, raw } = {}) {
  const qs = query ? '?' + new URLSearchParams(Object.entries(query).filter(([, v]) => v !== '' && v != null)) : '';
  const headers = {};
  if (token) headers.Authorization = `Bearer ${token}`;
  let payload;
  if (form) payload = form;
  else if (body !== undefined) { headers['Content-Type'] = 'application/json'; payload = JSON.stringify(body); }
  let res;
  try { res = await fetch(`/api${path}${qs}`, { method, headers, body: payload }); }
  catch { throw new ApiError(0, 'Sem conexão com o servidor.'); }
  if (res.status === 401 && token && path !== '/auth/login') { onUnauthorized(); throw new ApiError(401, 'Sua sessão expirou. Entre novamente.'); }
  if (!res.ok) {
    let msg = ({ 403: 'Você não tem permissão para esta ação.', 404: 'Registro não encontrado.' })[res.status] || `Erro ${res.status}.`;
    try { const j = await res.json(); if (j.detail) msg = j.detail; else if (j.message) msg = j.message; } catch {}
    throw new ApiError(res.status, msg);
  }
  if (raw) return res;
  if (res.status === 204) return null;
  return res.json();
}

export const api = {
  login: (email, senha) => request('POST', '/auth/login', { body: { email, senha } }),
  me: () => request('GET', '/auth/me').catch((e) => { if (e.status === 404 || e.status === 403 || e.status === 405) return null; throw e; }),
  tickets: (query) => request('GET', '/tickets', { query }),
  ticket: (id) => request('GET', `/tickets/${id}`),
  criarTicket: (body) => request('POST', '/tickets', { body }),
  status: (id, status, observacao) => request('PATCH', `/tickets/${id}/status`, { body: { status, observacao: observacao || null } }),
  atribuir: (id, tecnicoId) => request('PATCH', `/tickets/${id}/atribuicao`, { body: { tecnicoId } }),
  historico: (id) => request('GET', `/tickets/${id}/historico`),
  mensagens: (id) => request('GET', `/tickets/${id}/mensagens`),
  enviarMensagem: (id, texto) => request('POST', `/tickets/${id}/mensagens`, { body: { texto } }),
  slaStats: () => request('GET', '/tickets/estatisticas/sla'),
  equipStats: () => request('GET', '/tickets/estatisticas/equipamentos'),
  statusStats: () => request('GET', '/tickets/estatisticas/status'),
  laudo: (id) => request('GET', `/tickets/${id}/laudo`),
  criarLaudo: (id, body) => request('POST', `/tickets/${id}/laudo`, { body }),
  avaliacao: (id) => request('GET', `/tickets/${id}/avaliacao`),
  criarAvaliacao: (id, body) => request('POST', `/tickets/${id}/avaliacao`, { body }),
  anexos: (id) => request('GET', `/tickets/${id}/anexos`),
  enviarAnexo: (id, file) => { const f = new FormData(); f.append('arquivo', file); return request('POST', `/tickets/${id}/anexos`, { form: f }); },
  removerAnexo: (id, aid) => request('DELETE', `/tickets/${id}/anexos/${aid}`),
  baixarAnexo: (id, aid) => request('GET', `/tickets/${id}/anexos/${aid}`, { raw: true }).then((r) => r.blob()),
  equipamentos: (query) => request('GET', '/equipamentos', { query }),
  criarEquipamento: (body) => request('POST', '/equipamentos', { body }),
  atualizarEquipamento: (id, body) => request('PUT', `/equipamentos/${id}`, { body }),
  removerEquipamento: (id) => request('DELETE', `/equipamentos/${id}`),
  usuarios: (query) => request('GET', '/usuarios', { query }),
  criarUsuario: (body) => request('POST', '/usuarios', { body }),
  perfilUsuario: (id, perfil) => request('PATCH', `/usuarios/${id}/perfil`, { body: { perfil } }),
  removerUsuario: (id) => request('DELETE', `/usuarios/${id}`),
};

// Carrega todas as páginas de uma listagem (tamanho máx. 100 no back end).
export async function todos(fn, query = {}) {
  const out = []; let pagina = 0, p;
  do { p = await fn({ ...query, pagina, tamanho: 100 }); out.push(...p.content); pagina++; } while (!p.last && pagina < 50);
  return out;
}

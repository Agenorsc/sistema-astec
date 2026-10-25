// Camada de acesso a API. Todas as telas importam daqui (`import { api } from '../api'`).
// Os endpoints correspondem aos controllers do Spring Boot.
import { mockApi } from './mock'

async function request(path, options = {}) {
  let res
  try {
    res = await fetch(`/api${path}`, {
      headers: { 'Content-Type': 'application/json' },
      ...options,
    })
  } catch {
    throw new Error('Não foi possível conectar ao backend (http://localhost:8080). Ele está rodando?')
  }
  if (!res.ok) throw new Error(`Erro ${res.status} em ${options.method || 'GET'} /api${path}`)
  return res.status === 204 ? null : res.json()
}

const qs = (params = {}) => {
  const s = new URLSearchParams(Object.entries(params).filter(([, v]) => v != null && v !== ''))
  return s.toString() ? `?${s}` : ''
}
const send = (method, body) => ({ method, body: JSON.stringify(body) })

const realApi = {
  listUsuarios: (perfil) => request(`/usuarios${qs({ perfil })}`),
  createUsuario: (u) => request('/usuarios', send('POST', u)),

  listEquipamentos: () => request('/equipamentos'),
  createEquipamento: (e) => request('/equipamentos', send('POST', e)),

  listTickets: (filters) => request(`/tickets${qs(filters)}`),
  getTicket: (id) => request(`/tickets/${id}`),
  createTicket: (t) => request('/tickets', send('POST', t)),
  updateStatus: (id, status) => request(`/tickets/${id}/status${qs({ status })}`, { method: 'PATCH' }),
  assignTecnico: (id, tecnicoId) => request(`/tickets/${id}/tecnico${qs({ tecnicoId })}`, { method: 'PATCH' }),
  statsStatus: () => request('/tickets/estatisticas/status'),
  statsEquipamentos: () => request('/tickets/estatisticas/equipamentos'),
}

export const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'
export const api = USE_MOCK ? mockApi : realApi

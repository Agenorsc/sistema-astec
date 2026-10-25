import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth/AuthContext'
import { useAsync } from '../utils/useAsync'
import { Async } from '../components/State'
import { TIPO, URGENCIA, gerarCodigo } from '../utils/format'

// RF01 - abertura de chamado.
export default function TicketNew() {
  const { usuario } = useAuth()
  const navigate = useNavigate()
  const isAdmin = usuario.perfil === 'ADMIN'
  const [form, setForm] = useState({ titulo: '', descricao: '', tipoAtendimento: 'CORRETIVA', urgencia: 'MEDIA', equipamentoId: '', clienteId: '' })
  const [erro, setErro] = useState('')
  const [busy, setBusy] = useState(false)
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const state = useAsync(async () => ({
    equipamentos: await api.listEquipamentos(),
    clientes: isAdmin ? await api.listUsuarios('CLIENTE') : [],
  }))

  async function enviar(ev) {
    ev.preventDefault()
    setBusy(true); setErro('')
    try {
      const criado = await api.createTicket({
        codigo: gerarCodigo(),
        titulo: form.titulo,
        descricao: form.descricao,
        tipoAtendimento: form.tipoAtendimento,
        urgencia: form.urgencia,
        cliente: { id: Number(isAdmin ? form.clienteId : usuario.id) },
        equipamento: { id: Number(form.equipamentoId) },
      })
      navigate(`/chamados/${criado.id}`)
    } catch (err) {
      setErro(err.message)
      setBusy(false)
    }
  }

  return (
    <>
      <h1>Novo chamado</h1>
      <Async state={state}>
        {({ equipamentos, clientes }) => (
          <form className="card form" onSubmit={enviar}>
            {isAdmin && (
              <label>Cliente
                <select value={form.clienteId} onChange={set('clienteId')} required>
                  <option value="">Selecione…</option>
                  {clientes.map((c) => <option key={c.id} value={c.id}>{c.nome}</option>)}
                </select>
              </label>
            )}
            <label>Equipamento
              <select value={form.equipamentoId} onChange={set('equipamentoId')} required>
                <option value="">Selecione…</option>
                {equipamentos.map((e) => <option key={e.id} value={e.id}>{e.nome} — {e.modelo}</option>)}
              </select>
            </label>
            <div className="row two">
              <label>Tipo de atendimento
                <select value={form.tipoAtendimento} onChange={set('tipoAtendimento')}>
                  {Object.entries(TIPO).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
                </select>
              </label>
              <label>Urgência
                <select value={form.urgencia} onChange={set('urgencia')}>
                  {Object.entries(URGENCIA).map(([k, v]) => <option key={k} value={k}>{v.label}</option>)}
                </select>
              </label>
            </div>
            <label>Título<input value={form.titulo} onChange={set('titulo')} maxLength={255} required /></label>
            <label>Descrição da falha<textarea rows={5} value={form.descricao} onChange={set('descricao')} required /></label>
            {erro && <p className="error">{erro}</p>}
            <div className="row">
              <button className="btn primary" disabled={busy}>{busy ? 'Enviando…' : 'Abrir chamado'}</button>
              <button type="button" className="btn" onClick={() => navigate(-1)}>Cancelar</button>
            </div>
          </form>
        )}
      </Async>
    </>
  )
}

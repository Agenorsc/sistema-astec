import { useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth/AuthContext'
import { useAsync } from '../utils/useAsync'
import { Async } from '../components/State'
import { StatusBadge, UrgenciaBadge } from '../components/Badges'
import { STATUS, TIPO, formatDate } from '../utils/format'

// RF07 (painel do cliente) e RF08 (painel do tecnico/admin).
export default function Tickets() {
  const { usuario, isStaff } = useAuth()
  const [params, setParams] = useSearchParams()
  const [busca, setBusca] = useState('')
  const [equipId, setEquipId] = useState('')
  const [somenteMeus, setSomenteMeus] = useState(false)
  const status = params.get('status') || ''

  const state = useAsync(async () => {
    const [tickets, equipamentos] = await Promise.all([
      isStaff ? api.listTickets() : api.listTickets({ clienteId: usuario.id }),
      api.listEquipamentos(),
    ])
    return { tickets, equipamentos }
  }, [usuario.id])

  const setStatus = (v) => (v ? setParams({ status: v }) : setParams({}))

  const filtrar = (tickets) => {
    const q = busca.trim().toLowerCase()
    return tickets
      .filter((t) => !status || t.status === status)
      .filter((t) => !equipId || t.equipamento.id === Number(equipId))
      .filter((t) => !somenteMeus || t.tecnicoResponsavel?.id === usuario.id)
      .filter((t) => !q || `${t.codigo} ${t.titulo} ${t.cliente.nome}`.toLowerCase().includes(q))
      .sort((a, b) => b.dataCriacao.localeCompare(a.dataCriacao))
  }

  return (
    <>
      <div className="page-head">
        <h1>{isStaff ? 'Todos os chamados' : 'Meus chamados'}</h1>
        {usuario.perfil !== 'TECNICO' && <Link className="btn primary" to="/chamados/novo">+ Novo chamado</Link>}
      </div>

      <Async state={state}>
        {({ tickets, equipamentos }) => {
          const lista = filtrar(tickets)
          return (
            <>
              <div className="card filters">
                <input placeholder="Buscar por código, título ou cliente…" value={busca} onChange={(e) => setBusca(e.target.value)} />
                <select value={status} onChange={(e) => setStatus(e.target.value)}>
                  <option value="">Todos os status</option>
                  {Object.entries(STATUS).map(([k, v]) => <option key={k} value={k}>{v.label}</option>)}
                </select>
                <select value={equipId} onChange={(e) => setEquipId(e.target.value)}>
                  <option value="">Todas as máquinas</option>
                  {equipamentos.map((e) => <option key={e.id} value={e.id}>{e.nome}</option>)}
                </select>
                {usuario.perfil === 'TECNICO' && (
                  <label className="check"><input type="checkbox" checked={somenteMeus} onChange={(e) => setSomenteMeus(e.target.checked)} /> Só os meus</label>
                )}
              </div>

              <div className="card table-wrap">
                <table>
                  <thead>
                    <tr><th>Código</th><th>Título</th>{isStaff && <th>Cliente</th>}<th>Equipamento</th><th>Tipo</th><th>Urgência</th><th>Status</th><th>Técnico</th><th>Abertura</th></tr>
                  </thead>
                  <tbody>
                    {lista.map((t) => (
                      <tr key={t.id}>
                        <td><Link to={`/chamados/${t.id}`}><strong>{t.codigo}</strong></Link></td>
                        <td>{t.titulo}</td>
                        {isStaff && <td>{t.cliente.nome}</td>}
                        <td>{t.equipamento.nome}</td>
                        <td>{TIPO[t.tipoAtendimento]}</td>
                        <td><UrgenciaBadge value={t.urgencia} /></td>
                        <td><StatusBadge value={t.status} /></td>
                        <td>{t.tecnicoResponsavel?.nome || <span className="muted">—</span>}</td>
                        <td>{formatDate(t.dataCriacao)}</td>
                      </tr>
                    ))}
                    {lista.length === 0 && <tr><td colSpan={9} className="muted center">Nenhum chamado encontrado.</td></tr>}
                  </tbody>
                </table>
              </div>
            </>
          )
        }}
      </Async>
    </>
  )
}

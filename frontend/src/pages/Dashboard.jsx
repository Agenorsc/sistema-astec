import { Link } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth/AuthContext'
import { useAsync } from '../utils/useAsync'
import { Async } from '../components/State'
import { StatusBadge, UrgenciaBadge } from '../components/Badges'
import { STATUS, formatDate } from '../utils/format'

function contar(tickets, chave) {
  const m = new Map()
  tickets.forEach((t) => { const k = chave(t); m.set(k, (m.get(k) || 0) + 1) })
  return m
}

export default function Dashboard() {
  const { usuario, isStaff } = useAuth()

  // Equipe usa os endpoints de estatistica; cliente calcula so com os proprios chamados (RNF03).
  const state = useAsync(async () => {
    if (isStaff) {
      const [tickets, porStatus, porEquip] = await Promise.all([
        api.listTickets(), api.statsStatus(), api.statsEquipamentos(),
      ])
      return {
        tickets,
        status: new Map(porStatus.map((s) => [s.status, s.quantidade])),
        equip: porEquip.map((e) => ({ nome: e.equipamento.nome, qtd: e.quantidade })),
      }
    }
    const tickets = await api.listTickets({ clienteId: usuario.id })
    return {
      tickets,
      status: contar(tickets, (t) => t.status),
      equip: [...contar(tickets, (t) => t.equipamento.nome)].map(([nome, qtd]) => ({ nome, qtd })),
    }
  }, [usuario.id])

  return (
    <>
      <h1>Olá, {usuario.nome.split(' ')[0]}</h1>
      <Async state={state}>
        {({ tickets, status, equip }) => {
          const max = Math.max(1, ...equip.map((e) => e.qtd))
          const recentes = [...tickets].sort((a, b) => b.dataCriacao.localeCompare(a.dataCriacao)).slice(0, 5)
          return (
            <>
              <div className="grid cards">
                {Object.entries(STATUS).map(([k, v]) => (
                  <Link to={`/chamados?status=${k}`} key={k} className="card stat" style={{ '--c': v.color }}>
                    <span className="num">{status.get(k) || 0}</span>
                    <span>{v.label}</span>
                  </Link>
                ))}
              </div>

              <div className="grid two">
                <section className="card">
                  <h2>Chamados por equipamento</h2>
                  {equip.length === 0 && <p className="muted">Sem dados.</p>}
                  {equip.map((e) => (
                    <div key={e.nome} className="bar-row">
                      <span>{e.nome}</span>
                      <div className="bar"><i style={{ width: `${(e.qtd / max) * 100}%` }} /></div>
                      <b>{e.qtd}</b>
                    </div>
                  ))}
                </section>

                <section className="card">
                  <h2>Chamados recentes</h2>
                  {recentes.length === 0 && <p className="muted">Nenhum chamado ainda.</p>}
                  <ul className="list">
                    {recentes.map((t) => (
                      <li key={t.id}>
                        <Link to={`/chamados/${t.id}`}>
                          <strong>{t.codigo}</strong> {t.titulo}
                        </Link>
                        <span className="row"><StatusBadge value={t.status} /><UrgenciaBadge value={t.urgencia} /><small className="muted">{formatDate(t.dataCriacao)}</small></span>
                      </li>
                    ))}
                  </ul>
                </section>
              </div>
            </>
          )
        }}
      </Async>
    </>
  )
}

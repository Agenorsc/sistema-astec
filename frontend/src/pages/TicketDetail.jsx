import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth/AuthContext'
import { useAsync } from '../utils/useAsync'
import { Async } from '../components/State'
import { StatusBadge, Stars, UrgenciaBadge } from '../components/Badges'
import { STATUS, TIPO, formatDate } from '../utils/format'

export default function TicketDetail() {
  const { id } = useParams()
  const { usuario, isStaff } = useAuth()
  const [erro, setErro] = useState('')
  const state = useAsync(async () => {
    const ticket = await api.getTicket(id)
    const tecnicos = usuario.perfil === 'ADMIN' ? await api.listUsuarios('TECNICO') : []
    return { ticket, tecnicos }
  }, [id])

  // RNF03: cliente so ve os proprios chamados (a checagem real precisa existir no backend tambem).
  const bloqueado = (t) => usuario.perfil === 'CLIENTE' && t.cliente.id !== usuario.id

  async function acao(fn) {
    setErro('')
    try { await fn(); await state.reload() } catch (e) { setErro(e.message) }
  }

  return (
    <Async state={state}>
      {({ ticket: t, tecnicos }) => bloqueado(t) ? <p className="error">Você não tem acesso a este chamado.</p> : (
        <>
          <p><Link to="/chamados">← Voltar</Link></p>
          <div className="page-head">
            <h1>{t.codigo} · {t.titulo}</h1>
            <span className="row"><StatusBadge value={t.status} /><UrgenciaBadge value={t.urgencia} /></span>
          </div>
          {erro && <p className="error">{erro}</p>}

          <div className="grid two">
            <section className="card">
              <h2>Detalhes</h2>
              <dl className="meta">
                <dt>Cliente</dt><dd>{t.cliente.nome}</dd>
                <dt>Equipamento</dt><dd>{t.equipamento.nome} ({t.equipamento.modelo})<br /><small className="muted">Série {t.equipamento.numeroSerie}</small></dd>
                <dt>Tipo</dt><dd>{TIPO[t.tipoAtendimento]}</dd>
                <dt>Técnico</dt><dd>{t.tecnicoResponsavel?.nome || <span className="muted">Não atribuído</span>}</dd>
                <dt>Aberto em</dt><dd>{formatDate(t.dataCriacao)}</dd>
                <dt>Encerrado em</dt><dd>{formatDate(t.dataFechamento)}</dd>
              </dl>
              <h3>Descrição</h3>
              <p className="pre">{t.descricao}</p>
            </section>

            <div className="stack">
              {isStaff && (
                <section className="card">
                  <h2>Gerenciar</h2>
                  <label>Status
                    <select value={t.status} onChange={(e) => acao(() => api.updateStatus(t.id, e.target.value))}>
                      {Object.entries(STATUS).map(([k, v]) => <option key={k} value={k}>{v.label}</option>)}
                    </select>
                  </label>
                  {usuario.perfil === 'ADMIN' && (
                    <label>Atribuir técnico
                      <select value={t.tecnicoResponsavel?.id || ''} onChange={(e) => e.target.value && acao(() => api.assignTecnico(t.id, e.target.value))}>
                        <option value="">Selecione…</option>
                        {tecnicos.map((x) => <option key={x.id} value={x.id}>{x.nome}</option>)}
                      </select>
                    </label>
                  )}
                </section>
              )}

              <section className="card">
                <h2>Laudo técnico</h2>
                {t.laudoTecnico ? (
                  <>
                    <p className="pre">{t.laudoTecnico.diagnosticoSolucao}</p>
                    <p className="muted">Horas trabalhadas: <b>{t.laudoTecnico.horasTrabalhadas ?? '—'}</b></p>
                  </>
                ) : <p className="muted">Ainda não há laudo para este chamado.</p>}
              </section>

              <section className="card">
                <h2>Avaliação</h2>
                {t.avaliacao ? (
                  <>
                    <Stars nota={t.avaliacao.nota} />
                    <p className="pre">{t.avaliacao.comentario}</p>
                  </>
                ) : <p className="muted">Chamado ainda não avaliado.</p>}
              </section>
            </div>
          </div>
        </>
      )}
    </Async>
  )
}

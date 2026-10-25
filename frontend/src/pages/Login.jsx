import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

// Contas criadas pelo DataInitializer do backend (apenas para desenvolvimento).
const DEMO = [
  { rotulo: 'Administrador', email: 'agenor@agemquinas.com.br', senha: 'admin123' },
  { rotulo: 'Técnico', email: 'felipe.tecnico@agemquinas.com.br', senha: 'tec123' },
  { rotulo: 'Cliente', email: 'contato@metaljoin.com.br', senha: 'cli123' },
]

export default function Login() {
  const { usuario, login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState('')
  const [busy, setBusy] = useState(false)

  if (usuario) return <Navigate to="/" replace />

  async function entrar(e, s) {
    setBusy(true); setErro('')
    try {
      await login(e, s)
      navigate('/')
    } catch (err) {
      setErro(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="login-page">
      <form className="card login" onSubmit={(ev) => { ev.preventDefault(); entrar(email, senha) }}>
        <h1><span className="logo">A</span> Sistema Astec</h1>
        <p className="muted">Gestão de assistência técnica — AGE Máquinas e Manutenção</p>
        <label>E-mail<input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoFocus /></label>
        <label>Senha<input type="password" value={senha} onChange={(e) => setSenha(e.target.value)} required /></label>
        {erro && <p className="error">{erro}</p>}
        <button className="btn primary" disabled={busy}>{busy ? 'Entrando…' : 'Entrar'}</button>
        <hr />
        <p className="muted small">Acesso rápido (dados de teste):</p>
        <div className="row">
          {DEMO.map((d) => (
            <button type="button" key={d.email} className="btn" disabled={busy} onClick={() => entrar(d.email, d.senha)}>{d.rotulo}</button>
          ))}
        </div>
      </form>
    </div>
  )
}

import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { USE_MOCK } from '../api'
import { PERFIL } from '../utils/format'

export default function Layout() {
  const { usuario, logout, isStaff } = useAuth()
  const navigate = useNavigate()
  const sair = () => { logout(); navigate('/login') }

  const links = [
    { to: '/', label: 'Painel', end: true },
    { to: '/chamados', label: isStaff ? 'Todos os chamados' : 'Meus chamados', end: true },
    ...(usuario.perfil !== 'TECNICO' ? [{ to: '/chamados/novo', label: 'Novo chamado' }] : []),
    ...(isStaff ? [{ to: '/equipamentos', label: 'Equipamentos' }] : []),
    ...(usuario.perfil === 'ADMIN' ? [{ to: '/usuarios', label: 'Usuários' }] : []),
  ]

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand"><span className="logo">A</span> Astec</div>
        <nav>
          {links.map((l) => (
            <NavLink key={l.to} to={l.to} end={l.end}>{l.label}</NavLink>
          ))}
        </nav>
        <div className="user">
          <strong>{usuario.nome}</strong>
          <span>{PERFIL[usuario.perfil]}</span>
          <button className="btn ghost" onClick={sair}>Sair</button>
        </div>
      </aside>
      <main className="content">
        {USE_MOCK && <div className="banner">Modo demonstração: dados falsos em memória (VITE_USE_MOCK=true)</div>}
        <Outlet />
      </main>
    </div>
  )
}

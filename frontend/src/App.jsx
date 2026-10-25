import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './auth/AuthContext'
import Layout from './components/Layout'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Tickets from './pages/Tickets'
import TicketNew from './pages/TicketNew'
import TicketDetail from './pages/TicketDetail'
import Equipamentos from './pages/Equipamentos'
import Usuarios from './pages/Usuarios'

// Protege rotas por login e, opcionalmente, por perfil (RBAC - RNF03).
function Protected({ perfis, children }) {
  const { usuario } = useAuth()
  if (!usuario) return <Navigate to="/login" replace />
  if (perfis && !perfis.includes(usuario.perfil)) return <Navigate to="/" replace />
  return children
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route element={<Protected><Layout /></Protected>}>
        <Route index element={<Dashboard />} />
        <Route path="chamados" element={<Tickets />} />
        <Route path="chamados/novo" element={<Protected perfis={['CLIENTE', 'ADMIN']}><TicketNew /></Protected>} />
        <Route path="chamados/:id" element={<TicketDetail />} />
        <Route path="equipamentos" element={<Protected perfis={['TECNICO', 'ADMIN']}><Equipamentos /></Protected>} />
        <Route path="usuarios" element={<Protected perfis={['ADMIN']}><Usuarios /></Protected>} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

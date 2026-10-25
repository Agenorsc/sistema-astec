import { createContext, useContext, useState } from 'react'
import { api } from '../api'

// O backend ainda nao tem endpoint de login/JWT. Por isso o login busca
// /api/usuarios e confere e-mail + senha no cliente. Troque `login` por um
// POST /api/auth/login quando o backend tiver autenticacao de verdade.
const AuthContext = createContext(null)
const KEY = 'astec.usuario'

function loadSaved() {
  try {
    return JSON.parse(localStorage.getItem(KEY))
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(loadSaved)

  async function login(email, senha) {
    const usuarios = await api.listUsuarios()
    const u = usuarios.find((x) => x.email.toLowerCase() === email.trim().toLowerCase() && x.senha === senha)
    if (!u) throw new Error('E-mail ou senha inválidos.')
    const { senha: _omit, ...seguro } = u
    localStorage.setItem(KEY, JSON.stringify(seguro))
    setUsuario(seguro)
  }

  function logout() {
    localStorage.removeItem(KEY)
    setUsuario(null)
  }

  const isStaff = usuario?.perfil === 'TECNICO' || usuario?.perfil === 'ADMIN'
  return <AuthContext.Provider value={{ usuario, login, logout, isStaff }}>{children}</AuthContext.Provider>
}

export const useAuth = () => useContext(AuthContext)

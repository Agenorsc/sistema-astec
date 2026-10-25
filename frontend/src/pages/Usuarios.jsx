import { useState } from 'react'
import { api } from '../api'
import { useAsync } from '../utils/useAsync'
import { Async } from '../components/State'
import { PERFIL } from '../utils/format'

export default function Usuarios() {
  const state = useAsync(() => api.listUsuarios())
  const vazio = { nome: '', email: '', senha: '', perfil: 'CLIENTE' }
  const [form, setForm] = useState(vazio)
  const [erro, setErro] = useState('')
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  async function salvar(ev) {
    ev.preventDefault(); setErro('')
    try { await api.createUsuario(form); setForm(vazio); state.reload() } catch (e) { setErro(e.message) }
  }

  return (
    <>
      <h1>Usuários</h1>
      <div className="grid two">
        <section className="card table-wrap">
          <Async state={state}>
            {(lista) => (
              <table>
                <thead><tr><th>Nome</th><th>E-mail</th><th>Perfil</th></tr></thead>
                <tbody>{lista.map((u) => <tr key={u.id}><td>{u.nome}</td><td>{u.email}</td><td>{PERFIL[u.perfil]}</td></tr>)}</tbody>
              </table>
            )}
          </Async>
        </section>
        <form className="card form" onSubmit={salvar}>
          <h2>Cadastrar usuário</h2>
          <label>Nome<input value={form.nome} onChange={set('nome')} required /></label>
          <label>E-mail<input type="email" value={form.email} onChange={set('email')} required /></label>
          <label>Senha<input type="password" value={form.senha} onChange={set('senha')} required /></label>
          <label>Perfil
            <select value={form.perfil} onChange={set('perfil')}>
              {Object.entries(PERFIL).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
            </select>
          </label>
          {erro && <p className="error">{erro}</p>}
          <button className="btn primary">Salvar</button>
        </form>
      </div>
    </>
  )
}

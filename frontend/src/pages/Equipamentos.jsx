import { useState } from 'react'
import { api } from '../api'
import { useAsync } from '../utils/useAsync'
import { Async } from '../components/State'

export default function Equipamentos() {
  const state = useAsync(() => api.listEquipamentos())
  const [form, setForm] = useState({ nome: '', modelo: '', numeroSerie: '' })
  const [erro, setErro] = useState('')
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  async function salvar(ev) {
    ev.preventDefault(); setErro('')
    try {
      await api.createEquipamento(form)
      setForm({ nome: '', modelo: '', numeroSerie: '' })
      state.reload()
    } catch (e) { setErro(e.message) }
  }

  return (
    <>
      <h1>Equipamentos</h1>
      <div className="grid two">
        <section className="card table-wrap">
          <Async state={state}>
            {(lista) => (
              <table>
                <thead><tr><th>Nome</th><th>Modelo</th><th>Nº de série</th></tr></thead>
                <tbody>{lista.map((e) => <tr key={e.id}><td>{e.nome}</td><td>{e.modelo}</td><td>{e.numeroSerie}</td></tr>)}</tbody>
              </table>
            )}
          </Async>
        </section>
        <form className="card form" onSubmit={salvar}>
          <h2>Cadastrar equipamento</h2>
          <label>Nome<input value={form.nome} onChange={set('nome')} required /></label>
          <label>Modelo<input value={form.modelo} onChange={set('modelo')} /></label>
          <label>Nº de série<input value={form.numeroSerie} onChange={set('numeroSerie')} required /></label>
          {erro && <p className="error">{erro}</p>}
          <button className="btn primary">Salvar</button>
        </form>
      </div>
    </>
  )
}

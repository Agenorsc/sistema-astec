// Estados padrao de carregamento / erro para as paginas.
export function Loading() {
  return <p className="muted">Carregando…</p>
}

export function ErrorBox({ message, onRetry }) {
  return (
    <div className="error">
      <p>{message}</p>
      {onRetry && <button className="btn" onClick={onRetry}>Tentar novamente</button>}
    </div>
  )
}

export function Async({ state, children }) {
  if (state.loading && !state.data) return <Loading />
  if (state.error) return <ErrorBox message={state.error} onRetry={state.reload} />
  return children(state.data)
}

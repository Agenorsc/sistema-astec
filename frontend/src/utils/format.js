// Rotulos e helpers de formatacao. Os valores (chaves) sao os enums do backend.

export const STATUS = {
  ABERTO: { label: 'Aberto', color: '#2563eb' },
  EM_ANALISE: { label: 'Em análise', color: '#7c3aed' },
  AGUARDANDO_PECAS: { label: 'Aguardando peças', color: '#d97706' },
  EM_ATENDIMENTO: { label: 'Em atendimento', color: '#0891b2' },
  RESOLVIDO: { label: 'Resolvido', color: '#16a34a' },
  FECHADO: { label: 'Fechado', color: '#64748b' },
}

export const URGENCIA = {
  BAIXA: { label: 'Baixa', color: '#64748b' },
  MEDIA: { label: 'Média', color: '#d97706' },
  ALTA: { label: 'Alta', color: '#ea580c' },
  CRITICA: { label: 'Crítica', color: '#dc2626' },
}

export const TIPO = {
  CORRETIVA: 'Corretiva',
  PREVENTIVA: 'Preventiva',
  DUVIDA_TECNICA: 'Dúvida técnica',
  PECA_DEFEITO: 'Peça com defeito',
}

export const PERFIL = { CLIENTE: 'Cliente', TECNICO: 'Técnico', ADMIN: 'Administrador' }

export function formatDate(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
}

export function gerarCodigo() {
  const d = new Date()
  const ymd = `${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}${String(d.getDate()).padStart(2, '0')}`
  return `AST-${ymd}${Math.floor(Math.random() * 9000 + 1000)}`
}

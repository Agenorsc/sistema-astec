import { STATUS, URGENCIA } from '../utils/format'

const Badge = ({ label, color }) => (
  <span className="badge" style={{ '--c': color }}>{label}</span>
)

export const StatusBadge = ({ value }) => <Badge {...(STATUS[value] || { label: value, color: '#64748b' })} />
export const UrgenciaBadge = ({ value }) => <Badge {...(URGENCIA[value] || { label: value, color: '#64748b' })} />

export function Stars({ nota }) {
  return <span className="stars" aria-label={`${nota} de 5`}>{'★'.repeat(nota)}{'☆'.repeat(5 - nota)}</span>
}

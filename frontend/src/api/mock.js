// Backend falso em memoria. Espelha o JSON do Spring (mesmos campos e enums)
// e os mesmos endpoints. Ative com VITE_USE_MOCK=true no arquivo .env.

const wait = (v) => new Promise((r) => setTimeout(() => r(structuredClone(v)), 150))
const ago = (h) => new Date(Date.now() - h * 3600e3).toISOString().slice(0, 19)

let seq = 100
const usuarios = [
  { id: 1, nome: 'Agenor Alvise', email: 'agenor@agemquinas.com.br', senha: 'admin123', perfil: 'ADMIN' },
  { id: 2, nome: 'Felipe Santos', email: 'felipe.tecnico@agemquinas.com.br', senha: 'tec123', perfil: 'TECNICO' },
  { id: 3, nome: 'Henrique Cezar', email: 'henrique.tecnico@agemquinas.com.br', senha: 'tec456', perfil: 'TECNICO' },
  { id: 4, nome: 'Metalúrgica Joinville LTDA', email: 'contato@metaljoin.com.br', senha: 'cli123', perfil: 'CLIENTE' },
  { id: 5, nome: 'Indústria Têxtil Catarinense', email: 'manutencao@textilcat.com.br', senha: 'cli456', perfil: 'CLIENTE' },
]
const equipamentos = [
  { id: 1, nome: 'Torno CNC Industrial 5000', modelo: 'TC-5000X', numeroSerie: 'SN-CNC-2023-001' },
  { id: 2, nome: 'Centro de Usinagem Vertical', modelo: 'CUV-800', numeroSerie: 'SN-CUV-2022-042' },
  { id: 3, nome: 'Prensa Hidráulica 100T', modelo: 'PH-100', numeroSerie: 'SN-PRENSA-2021-099' },
]
const tickets = [
  {
    id: 1, codigo: 'AST-20261001', titulo: 'Vibração excessiva no fuso principal',
    descricao: 'Durante a usinagem em alta rotação, o torno emite vibração atípica e superaquecimento.',
    tipoAtendimento: 'CORRETIVA', urgencia: 'ALTA', status: 'EM_ATENDIMENTO',
    dataCriacao: ago(30), dataFechamento: null,
    cliente: usuarios[3], tecnicoResponsavel: usuarios[1], equipamento: equipamentos[0],
    laudoTecnico: null, avaliacao: null,
  },
  {
    id: 2, codigo: 'AST-20261002', titulo: 'Manutenção preventiva semestral',
    descricao: 'Troca de filtros, verificação de sensores pneumáticos e alinhamento de guias lineares.',
    tipoAtendimento: 'PREVENTIVA', urgencia: 'MEDIA', status: 'RESOLVIDO',
    dataCriacao: ago(72), dataFechamento: ago(2),
    cliente: usuarios[4], tecnicoResponsavel: usuarios[2], equipamento: equipamentos[1],
    laudoTecnico: { id: 1, diagnosticoSolucao: 'Realizada a limpeza completa dos bicos de lubrificação e troca do jogo de retentores.', horasTrabalhadas: 4.5 },
    avaliacao: { id: 1, nota: 5, comentario: 'Atendimento exemplar do técnico Henrique, resolveu com agilidade e perícia!', dataAvaliacao: ago(1) },
  },
  {
    id: 3, codigo: 'AST-20261003', titulo: 'Vazamento no retentor do cilindro hidráulico',
    descricao: 'Prensa perdendo pressão máxima de compactação de 100 toneladas devido a vazamento de óleo.',
    tipoAtendimento: 'PECA_DEFEITO', urgencia: 'CRITICA', status: 'ABERTO',
    dataCriacao: ago(5), dataFechamento: null,
    cliente: usuarios[3], tecnicoResponsavel: null, equipamento: equipamentos[2],
    laudoTecnico: null, avaliacao: null,
  },
]
const find = (arr, id) => arr.find((x) => x.id === Number(id))

export const mockApi = {
  listUsuarios: (perfil) => wait(perfil ? usuarios.filter((u) => u.perfil === perfil) : usuarios),
  createUsuario: (u) => { const n = { ...u, id: ++seq }; usuarios.push(n); return wait(n) },

  listEquipamentos: () => wait(equipamentos),
  createEquipamento: (e) => { const n = { ...e, id: ++seq }; equipamentos.push(n); return wait(n) },

  listTickets: ({ clienteId, tecnicoId, status } = {}) => {
    let r = tickets
    if (clienteId) r = r.filter((t) => t.cliente.id === Number(clienteId))
    else if (tecnicoId) r = r.filter((t) => t.tecnicoResponsavel?.id === Number(tecnicoId))
    else if (status) r = r.filter((t) => t.status === status)
    return wait(r)
  },
  getTicket: (id) => {
    const t = find(tickets, id)
    return t ? wait(t) : Promise.reject(new Error('Chamado não encontrado (404)'))
  },
  createTicket: (body) => {
    const t = {
      ...body, id: ++seq, status: 'ABERTO', dataCriacao: new Date().toISOString().slice(0, 19),
      dataFechamento: null, tecnicoResponsavel: null, laudoTecnico: null, avaliacao: null,
      cliente: find(usuarios, body.cliente.id), equipamento: find(equipamentos, body.equipamento.id),
    }
    tickets.push(t)
    return wait(t)
  },
  updateStatus: (id, status) => {
    const t = find(tickets, id)
    t.status = status
    if (status === 'RESOLVIDO' || status === 'FECHADO') t.dataFechamento = new Date().toISOString().slice(0, 19)
    return wait(t)
  },
  assignTecnico: (id, tecnicoId) => {
    const t = find(tickets, id)
    t.tecnicoResponsavel = find(usuarios, tecnicoId)
    if (t.status === 'ABERTO') t.status = 'EM_ANALISE'
    return wait(t)
  },
  statsStatus: () => {
    const m = {}
    tickets.forEach((t) => (m[t.status] = (m[t.status] || 0) + 1))
    return wait(Object.entries(m).map(([status, quantidade]) => ({ status, quantidade })))
  },
  statsEquipamentos: () => {
    const m = new Map()
    tickets.forEach((t) => m.set(t.equipamento.id, (m.get(t.equipamento.id) || 0) + 1))
    return wait([...m].map(([id, quantidade]) => ({ equipamento: find(equipamentos, id), quantidade })))
  },
}

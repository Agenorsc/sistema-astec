package br.com.astec.model;

import br.com.astec.model.enums.StatusTicketEnum;
import br.com.astec.model.enums.TipoAtendimentoEnum;
import br.com.astec.model.enums.UrgenciaEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_ticket")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoAtendimentoEnum tipoAtendimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UrgenciaEnum urgencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StatusTicketEnum status;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    private LocalDateTime dataFechamento;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private Usuario tecnicoResponsavel;

    @ManyToOne
    @JoinColumn(name = "equipamento_id", nullable = false)
    private Equipamento equipamento;

    @JsonIgnore
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MensagemTicket> mensagens = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Anexo> anexos = new ArrayList<>();

    @OneToOne(mappedBy = "ticket", cascade = CascadeType.ALL)
    private LaudoTecnico laudoTecnico;

    @OneToOne(mappedBy = "ticket", cascade = CascadeType.ALL)
    private Avaliacao avaliacao;

    public Ticket() {
        this.dataCriacao = LocalDateTime.now();
        this.status = StatusTicketEnum.ABERTO;
    }

    public Ticket(Long id, String codigo, String titulo, String descricao,
                  TipoAtendimentoEnum tipoAtendimento, UrgenciaEnum urgencia,
                  StatusTicketEnum status, Usuario cliente, Equipamento equipamento) {
        this.id = id;
        this.codigo = codigo;
        this.titulo = titulo;
        this.descricao = descricao;
        this.tipoAtendimento = tipoAtendimento;
        this.urgencia = urgencia;
        this.status = (status != null) ? status : StatusTicketEnum.ABERTO;
        this.dataCriacao = LocalDateTime.now();
        this.cliente = cliente;
        this.equipamento = equipamento;
    }

    public void alterarStatus(StatusTicketEnum novoStatus) {
        this.status = novoStatus;
        if (novoStatus == StatusTicketEnum.RESOLVIDO || novoStatus == StatusTicketEnum.FECHADO) {
            this.dataFechamento = LocalDateTime.now();
        }
    }

    public void atribuirTecnico(Usuario tecnico) {
        this.tecnicoResponsavel = tecnico;
        if (this.status == StatusTicketEnum.ABERTO) {
            this.status = StatusTicketEnum.EM_ANALISE;
        }
    }

    public void encerrarTicket() {
        this.status = StatusTicketEnum.FECHADO;
        this.dataFechamento = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public TipoAtendimentoEnum getTipoAtendimento() {
        return tipoAtendimento;
    }

    public void setTipoAtendimento(TipoAtendimentoEnum tipoAtendimento) {
        this.tipoAtendimento = tipoAtendimento;
    }

    public UrgenciaEnum getUrgencia() {
        return urgencia;
    }

    public void setUrgencia(UrgenciaEnum urgencia) {
        this.urgencia = urgencia;
    }

    public StatusTicketEnum getStatus() {
        return status;
    }

    public void setStatus(StatusTicketEnum status) {
        this.status = status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataFechamento() {
        return dataFechamento;
    }

    public void setDataFechamento(LocalDateTime dataFechamento) {
        this.dataFechamento = dataFechamento;
    }

    public Usuario getCliente() {
        return cliente;
    }

    public void setCliente(Usuario cliente) {
        this.cliente = cliente;
    }

    public Usuario getTecnicoResponsavel() {
        return tecnicoResponsavel;
    }

    public void setTecnicoResponsavel(Usuario tecnicoResponsavel) {
        this.tecnicoResponsavel = tecnicoResponsavel;
    }

    public Equipamento getEquipamento() {
        return equipamento;
    }

    public void setEquipamento(Equipamento equipamento) {
        this.equipamento = equipamento;
    }

    public List<MensagemTicket> getMensagens() {
        return mensagens;
    }

    public void setMensagens(List<MensagemTicket> mensagens) {
        this.mensagens = mensagens;
    }

    public List<Anexo> getAnexos() {
        return anexos;
    }

    public void setAnexos(List<Anexo> anexos) {
        this.anexos = anexos;
    }

    public LaudoTecnico getLaudoTecnico() {
        return laudoTecnico;
    }

    public void setLaudoTecnico(LaudoTecnico laudoTecnico) {
        this.laudoTecnico = laudoTecnico;
    }

    public Avaliacao getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(Avaliacao avaliacao) {
        this.avaliacao = avaliacao;
    }
}

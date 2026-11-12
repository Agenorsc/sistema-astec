package br.com.astec.model;

import br.com.astec.model.enums.StatusTicketEnum;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_historico_status_ticket")
public class HistoricoStatusTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private StatusTicketEnum statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StatusTicketEnum statusNovo;

    @Column(length = 1000)
    private String observacao;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataAlteracao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ticket_id", nullable = false, updatable = false)
    private Ticket ticket;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, updatable = false)
    private Usuario usuario;

    protected HistoricoStatusTicket() {
    }

    public HistoricoStatusTicket(
            Ticket ticket,
            Usuario usuario,
            StatusTicketEnum statusAnterior,
            StatusTicketEnum statusNovo,
            String observacao) {
        this.ticket = ticket;
        this.usuario = usuario;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.observacao = observacao;
    }

    @PrePersist
    void registrarDataAlteracao() {
        if (dataAlteracao == null) {
            dataAlteracao = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public StatusTicketEnum getStatusAnterior() {
        return statusAnterior;
    }

    public StatusTicketEnum getStatusNovo() {
        return statusNovo;
    }

    public String getObservacao() {
        return observacao;
    }

    public LocalDateTime getDataAlteracao() {
        return dataAlteracao;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}

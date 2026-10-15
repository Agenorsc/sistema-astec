package br.com.astec.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_laudo_tecnico")
public class LaudoTecnico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String diagnosticoSolucao;

    private Double horasTrabalhadas;

    @OneToOne
    @JoinColumn(name = "ticket_id", nullable = false, unique = true)
    private Ticket ticket;

    @JsonIgnore
    @OneToMany(mappedBy = "laudoTecnico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPecaSubstituida> pecasSubstituidas = new ArrayList<>();

    public LaudoTecnico() {
    }

    public LaudoTecnico(Long id, String diagnosticoSolucao, Double horasTrabalhadas, Ticket ticket) {
        this.id = id;
        this.diagnosticoSolucao = diagnosticoSolucao;
        this.horasTrabalhadas = horasTrabalhadas;
        this.ticket = ticket;
    }

    public void finalizarAtendimento() {
        if (this.ticket != null) {
            this.ticket.encerrarTicket();
        }
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDiagnosticoSolucao() {
        return diagnosticoSolucao;
    }

    public void setDiagnosticoSolucao(String diagnosticoSolucao) {
        this.diagnosticoSolucao = diagnosticoSolucao;
    }

    public Double getHorasTrabalhadas() {
        return horasTrabalhadas;
    }

    public void setHorasTrabalhadas(Double horasTrabalhadas) {
        this.horasTrabalhadas = horasTrabalhadas;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public List<ItemPecaSubstituida> getPecasSubstituidas() {
        return pecasSubstituidas;
    }

    public void setPecasSubstituidas(List<ItemPecaSubstituida> pecasSubstituidas) {
        this.pecasSubstituidas = pecasSubstituidas;
    }
}

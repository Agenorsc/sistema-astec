package br.com.astec.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_anexo")
public class Anexo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomeArquivo;

    @Column(nullable = false, length = 500)
    private String urlArquivo;

    @Column(length = 50)
    private String tipoArquivo;

    private Double tamanhoMb;

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    public Anexo() {
    }

    public Anexo(Long id, String nomeArquivo, String urlArquivo, String tipoArquivo, Double tamanhoMb, Ticket ticket) {
        this.id = id;
        this.nomeArquivo = nomeArquivo;
        this.urlArquivo = urlArquivo;
        this.tipoArquivo = tipoArquivo;
        this.tamanhoMb = tamanhoMb;
        this.ticket = ticket;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public String getUrlArquivo() {
        return urlArquivo;
    }

    public void setUrlArquivo(String urlArquivo) {
        this.urlArquivo = urlArquivo;
    }

    public String getTipoArquivo() {
        return tipoArquivo;
    }

    public void setTipoArquivo(String tipoArquivo) {
        this.tipoArquivo = tipoArquivo;
    }

    public Double getTamanhoMb() {
        return tamanhoMb;
    }

    public void setTamanhoMb(Double tamanhoMb) {
        this.tamanhoMb = tamanhoMb;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }
}

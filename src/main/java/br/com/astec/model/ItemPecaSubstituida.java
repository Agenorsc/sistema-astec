package br.com.astec.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tb_item_peca")
public class ItemPecaSubstituida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomePeca;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal valorUnitario;

    @ManyToOne
    @JoinColumn(name = "laudo_tecnico_id", nullable = false)
    private LaudoTecnico laudoTecnico;

    public ItemPecaSubstituida() {
    }

    public ItemPecaSubstituida(Long id, String nomePeca, Integer quantidade, BigDecimal valorUnitario, LaudoTecnico laudoTecnico) {
        this.id = id;
        this.nomePeca = nomePeca;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.laudoTecnico = laudoTecnico;
    }

    public BigDecimal calcularSubtotal() {
        if (quantidade == null || valorUnitario == null) {
            return BigDecimal.ZERO;
        }
        return valorUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomePeca() {
        return nomePeca;
    }

    public void setNomePeca(String nomePeca) {
        this.nomePeca = nomePeca;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public LaudoTecnico getLaudoTecnico() {
        return laudoTecnico;
    }

    public void setLaudoTecnico(LaudoTecnico laudoTecnico) {
        this.laudoTecnico = laudoTecnico;
    }
}

package br.com.astec.dto.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class RequisicaoNovoLaudo {

    @NotBlank(message = "O diagnóstico e solução técnica são obrigatórios")
    @Size(min = 10, max = 3000, message = "O laudo deve conter entre 10 e 3000 caracteres")
    private String diagnosticoSolucao;

    @NotNull(message = "Informe as horas trabalhadas")
    @Min(value = 1, message = "Horas trabalhadas deve ser de no mínimo 1")
    private Integer horasTrabalhadas;

    private String nomePeca;
    private Integer quantidadePeca;
    private BigDecimal valorUnitarioPeca;

    public String getDiagnosticoSolucao() {
        return diagnosticoSolucao;
    }

    public void setDiagnosticoSolucao(String diagnosticoSolucao) {
        this.diagnosticoSolucao = diagnosticoSolucao;
    }

    public Integer getHorasTrabalhadas() {
        return horasTrabalhadas;
    }

    public void setHorasTrabalhadas(Integer horasTrabalhadas) {
        this.horasTrabalhadas = horasTrabalhadas;
    }

    public String getNomePeca() {
        return nomePeca;
    }

    public void setNomePeca(String nomePeca) {
        this.nomePeca = nomePeca;
    }

    public Integer getQuantidadePeca() {
        return quantidadePeca;
    }

    public void setQuantidadePeca(Integer quantidadePeca) {
        this.quantidadePeca = quantidadePeca;
    }

    public BigDecimal getValorUnitarioPeca() {
        return valorUnitarioPeca;
    }

    public void setValorUnitarioPeca(BigDecimal valorUnitarioPeca) {
        this.valorUnitarioPeca = valorUnitarioPeca;
    }
}

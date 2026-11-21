package br.com.astec.dto.web;

import br.com.astec.model.Equipamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO para captura e validação de dados de cadastro de Equipamento via formulário Web.
 * Segue o padrão de formulários Spring MVC com Bean Validation (Deck 4).
 */
public class RequisicaoNovoEquipamento {

    @NotBlank(message = "O nome do equipamento é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @NotBlank(message = "O modelo é obrigatório")
    private String modelo;

    @NotBlank(message = "O número de série é obrigatório")
    private String numeroSerie;

    private String tagsTexto; // Tags separadas por vírgula (ex: "Dell, Notebook, I7")

    private List<Long> categoriaIds = new ArrayList<>();

    public Equipamento toEquipamento() {
        Equipamento eq = new Equipamento();
        eq.setNome(this.nome);
        eq.setModelo(this.modelo);
        eq.setNumeroSerie(this.numeroSerie);

        if (this.tagsTexto != null && !this.tagsTexto.isBlank()) {
            List<String> tags = Arrays.stream(this.tagsTexto.split(","))
                    .map(String::trim)
                    .filter(t -> !t.isEmpty())
                    .collect(Collectors.toList());
            eq.setTags(tags);
        }
        return eq;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public String getTagsTexto() {
        return tagsTexto;
    }

    public void setTagsTexto(String tagsTexto) {
        this.tagsTexto = tagsTexto;
    }

    public List<Long> getCategoriaIds() {
        return categoriaIds;
    }

    public void setCategoriaIds(List<Long> categoriaIds) {
        this.categoriaIds = categoriaIds;
    }
}

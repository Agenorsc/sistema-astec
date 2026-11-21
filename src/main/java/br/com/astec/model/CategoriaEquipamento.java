package br.com.astec.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade de Categoria para demonstrar relacionamento @ManyToMany bidirecional
 * Conforme ensinado na aula de JPA (Slides @ManyToMany e @JoinTable).
 */
@Entity
@Table(name = "tb_categoria_equipamento")
public class CategoriaEquipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @JsonIgnore
    @ManyToMany(mappedBy = "categorias")
    private List<Equipamento> equipamentos = new ArrayList<>();

    public CategoriaEquipamento() {
    }

    public CategoriaEquipamento(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public CategoriaEquipamento(Long id, String nome, String descricao) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<Equipamento> getEquipamentos() {
        return equipamentos;
    }

    public void setEquipamentos(List<Equipamento> equipamentos) {
        this.equipamentos = equipamentos;
    }
}

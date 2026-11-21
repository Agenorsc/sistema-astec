package br.com.astec.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_equipamento")
@NamedQuery(name = "Equipamento.listarTodos", query = "SELECT e FROM Equipamento e ORDER BY e.nome")
public class Equipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String modelo;

    @Column(unique = true, length = 100)
    private String numeroSerie;

    @ElementCollection
    @CollectionTable(name = "tb_equipamento_tags", joinColumns = @JoinColumn(name = "equipamento_id"))
    @Column(name = "tag", length = 50)
    private List<String> tags = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "tb_equipamento_categoria",
        joinColumns = @JoinColumn(name = "equipamento_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private List<CategoriaEquipamento> categorias = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "equipamento")
    private List<Ticket> tickets = new ArrayList<>();

    public Equipamento() {
    }

    public Equipamento(Long id, String nome, String modelo, String numeroSerie) {
        this.id = id;
        this.nome = nome;
        this.modelo = modelo;
        this.numeroSerie = numeroSerie;
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

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<CategoriaEquipamento> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<CategoriaEquipamento> categorias) {
        this.categorias = categorias;
    }
}

package br.com.astec.model;

import br.com.astec.model.enums.PerfilEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(length = 20)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PerfilEnum perfil;

    @Embedded
    private Endereco endereco;

    @JsonIgnore
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Ticket> ticketsAbertos = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "tecnicoResponsavel")
    private List<Ticket> ticketsAtendidos = new ArrayList<>();

    public Usuario() {
    }

    public Usuario(Long id, String nome, String email, String senha, PerfilEnum perfil) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public PerfilEnum getPerfil() {
        return perfil;
    }

    public void setPerfil(PerfilEnum perfil) {
        this.perfil = perfil;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public List<Ticket> getTicketsAbertos() {
        return ticketsAbertos;
    }

    public void setTicketsAbertos(List<Ticket> ticketsAbertos) {
        this.ticketsAbertos = ticketsAbertos;
    }

    public List<Ticket> getTicketsAtendidos() {
        return ticketsAtendidos;
    }

    public void setTicketsAtendidos(List<Ticket> ticketsAtendidos) {
        this.ticketsAtendidos = ticketsAtendidos;
    }
}

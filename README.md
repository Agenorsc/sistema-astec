# Sistema Astec - Gestão de Assistência Técnica Especializada

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot 3.3.4](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-green.svg)](https://www.thymeleaf.org/)
[![Bootstrap](https://img.shields.io/badge/Bootstrap-4.6.0-purple.svg)](https://getbootstrap.com/)
[![OpenAPI 3.0](https://img.shields.io/badge/OpenAPI-3.0%20Swagger-green.svg)](https://swagger.io/)
[![Build Status](https://img.shields.io/badge/Tests-23%20Passed%20(100%25)-success.svg)]()

> **Projeto Final da Disciplina de Programação WEB**  
> **Instituição:** Centro Universitário - Católica de Santa Catarina  
> **Professor:** Leonardo Vitazik Neto  

---

## 📌 Visão Geral

O **Sistema Astec** é uma aplicação corporativa robusta desenvolvida para o gerenciamento integral do ciclo de vida de ordens de serviço, chamados técnicos industriais, inventário de equipamentos e laudos periciais com substituição de componentes.

O projeto foi construído estritamente de acordo com o plano de ensino e os slides do professor:
- **Arquitetura Web Monolítica (Server-Side Rendering):** Spring MVC integrado ao motor de templates **Thymeleaf**, utilizando layouts responsivos em **Bootstrap 4.6.0** e fragmentos modulares (`base.html`).
- **Eliminação de SPA/React:** Todo o fluxo visual e a validação de formulários operam no backend com **Bean Validation (JSR-380)**, vinculação de atributos (`th:field`, `th:errorclass`) e mensagens em `messages.properties`.
- **Persistência Avançada com Spring Data JPA & Hibernate:**
  - Mapeamentos: `@Entity`, `@Id`, `@GeneratedValue`, `@Embeddable`/`@Embedded` (`Endereco`), `@ElementCollection` (`tags`), `@ManyToMany` com `@JoinTable` (`categorias`), `@OneToMany`/`@ManyToOne` (`mensagens`, `itens`), `@OneToOne` bidirecional (`laudoTecnico`, `avaliacao`), `@Version` (concorrência otimista) e `@Transient` (cálculo de subtotais e formatação de status).
  - Consultas: Derived Queries, Consultas Nativas SQL (`nativeQuery = true`), Agregações JPQL com Projeção DTO (`SELECT new ...`), Projeções por Interface (`TicketResumoProjecao`), Consultas Dinâmicas com JPA Criteria (`Specification`), Paginação com `Pageable` e `@NamedQuery`.
- **API RESTful e Documentação OpenAPI 3.0:** Endpoints com semântica de verbos e códigos de estado HTTP estritos (201, 200, 204, 404), documentados e interativos via **Swagger UI**.

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
- **Java Development Kit (JDK) 17** instalado e no `PATH`.
- Terminal (PowerShell / Bash / CMD).

### 1. Clonar ou Acessar a Pasta do Projeto
```powershell
Set-Location -Path "C:\Sistema Astec"
```

### 2. Executar a Suíte de Testes Automatizados (23 Testes)
```powershell
.\mvnw.cmd test
```
*Garante 100% de aprovação (0 falhas, 0 erros).*

### 3. Iniciar o Servidor Spring Boot
```powershell
.\mvnw.cmd spring-boot:run
```

A aplicação inicializa na porta **8080**, executando automaticamente o `DataInitializer` com uma base de dados rica em chamados, equipamentos com tags/categorias, laudos com peças e avaliações.

---

## 🌐 URLs de Acesso

| Recurso | URL | Descrição |
|:---|:---|:---|
| **Painel Geral (Dashboard)** | [http://localhost:8080/home](http://localhost:8080/home) | Métricas, filtros dinâmicos e tabela paginada |
| **Abertura de Chamado** | [http://localhost:8080/tickets/novo](http://localhost:8080/tickets/novo) | Formulário com Bean Validation e mensagens |
| **Inventário de Equipamentos** | [http://localhost:8080/equipamentos](http://localhost:8080/equipamentos) | Lista de maquinários com tags e categorias |
| **Cadastro de Equipamento** | [http://localhost:8080/equipamentos/novo](http://localhost:8080/equipamentos/novo) | Formulário de novos dispositivos |
| **Documentação Swagger UI** | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) | OpenAPI 3.0 interativo |
| **Console H2 Database** | [http://localhost:8080/h2-console](http://localhost:8080/h2-console) | JDBC URL: `jdbc:h2:mem:astecdb` (Usuário: `sa`) |

---

## 📄 Documentação Completa do Projeto

Para consulta detalhada da modelagem UML (Mermaid), dicionário de dados, especificação de requisitos funcionais/não-funcionais, matriz de rastreabilidade e explicação de cada anotação JPA e Thymeleaf, consulte o arquivo oficial:

👉 [**DOCUMENTACAO_FINAL.md**](file:///C:/Sistema%20Astec/DOCUMENTACAO_FINAL.md)

---
*Desenvolvido para a disciplina de Programação WEB - Católica de Santa Catarina (2026).*

package br.com.astec.repository;

import br.com.astec.model.Equipamento;
import br.com.astec.model.Ticket;
import br.com.astec.model.enums.StatusTicketEnum;
import br.com.astec.model.enums.UrgenciaEnum;
import br.com.astec.repository.dto.TicketContagemPorEquipamentoDTO;
import br.com.astec.repository.dto.TicketContagemPorStatusDTO;
import br.com.astec.repository.projection.TicketResumoProjecao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório de Chamados (Ticket)
 * Implementa JpaRepository e JpaSpecificationExecutor (Consultas Dinâmicas)
 * Conforme ensinado nas aulas de Spring Data (Derived queries, JPQL, Native Queries, Paginação, Projeções e Criteria).
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    // --- Derived Queries (Deck 2, Slide 8) ---
    List<Ticket> findByClienteId(Long clienteId);

    List<Ticket> findByTecnicoResponsavelId(Long tecnicoId);

    List<Ticket> findByEquipamentoId(Long equipamentoId);

    List<Ticket> findByStatus(StatusTicketEnum status);

    Optional<Ticket> findByCodigo(String codigo);

    List<Ticket> findByUrgencia(UrgenciaEnum urgencia);

    long countByStatus(StatusTicketEnum status);

    long countByEquipamento(Equipamento equipamento);

    // --- Paginação e Ordenação (Deck 2, Slides 13-14) ---
    Page<Ticket> findByStatus(StatusTicketEnum status, Pageable pageable);

    // --- Projeção por Interface (Deck 2, Slides 5 e 15) ---
    List<TicketResumoProjecao> findProjecaoByStatus(StatusTicketEnum status);

    // --- Projeção DTO via JPQL com Select New e Funções de Agregação (Deck 2, Slides 37-44) ---
    @Query("SELECT new br.com.astec.repository.dto.TicketContagemPorEquipamentoDTO(t.equipamento, COUNT(t)) " +
           "FROM Ticket t " +
           "GROUP BY t.equipamento")
    List<TicketContagemPorEquipamentoDTO> contarTicketsPorEquipamento();

    @Query("SELECT new br.com.astec.repository.dto.TicketContagemPorStatusDTO(t.status, COUNT(t)) " +
           "FROM Ticket t " +
           "GROUP BY t.status")
    List<TicketContagemPorStatusDTO> contarTicketsPorStatus();

    // --- Native Query (Deck 2, Slide 11) ---
    @Query(value = "SELECT * FROM tb_ticket WHERE urgencia = :urgenciaStr", nativeQuery = true)
    List<Ticket> buscarPorUrgenciaNativa(@Param("urgenciaStr") String urgenciaStr);
}

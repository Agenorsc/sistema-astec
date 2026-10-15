package br.com.astec.repository;

import br.com.astec.model.Equipamento;
import br.com.astec.model.Ticket;
import br.com.astec.model.enums.StatusTicketEnum;
import br.com.astec.model.enums.UrgenciaEnum;
import br.com.astec.repository.dto.TicketContagemPorEquipamentoDTO;
import br.com.astec.repository.dto.TicketContagemPorStatusDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByClienteId(Long clienteId);

    List<Ticket> findByTecnicoResponsavelId(Long tecnicoId);

    List<Ticket> findByEquipamentoId(Long equipamentoId);

    List<Ticket> findByStatus(StatusTicketEnum status);

    Optional<Ticket> findByCodigo(String codigo);

    List<Ticket> findByUrgencia(UrgenciaEnum urgencia);

    long countByStatus(StatusTicketEnum status);

    long countByEquipamento(Equipamento equipamento);

    @Query("SELECT new br.com.astec.repository.dto.TicketContagemPorEquipamentoDTO(t.equipamento, COUNT(t)) " +
           "FROM Ticket t " +
           "GROUP BY t.equipamento")
    List<TicketContagemPorEquipamentoDTO> contarTicketsPorEquipamento();

    @Query("SELECT new br.com.astec.repository.dto.TicketContagemPorStatusDTO(t.status, COUNT(t)) " +
           "FROM Ticket t " +
           "GROUP BY t.status")
    List<TicketContagemPorStatusDTO> contarTicketsPorStatus();
}

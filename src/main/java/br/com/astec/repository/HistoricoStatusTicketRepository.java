package br.com.astec.repository;

import br.com.astec.model.HistoricoStatusTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoStatusTicketRepository extends JpaRepository<HistoricoStatusTicket, Long> {

    List<HistoricoStatusTicket> findByTicketIdOrderByDataAlteracaoAsc(Long ticketId);
}

package br.com.astec.repository;

import br.com.astec.model.MensagemTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensagemTicketRepository extends JpaRepository<MensagemTicket, Long> {

    List<MensagemTicket> findByTicketId(Long ticketId);

    List<MensagemTicket> findByTicketIdOrderByDataEnvioAsc(Long ticketId);

    List<MensagemTicket> findByAutorId(Long autorId);
}

package br.com.astec.repository;

import br.com.astec.model.LaudoTecnico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface LaudoTecnicoRepository extends JpaRepository<LaudoTecnico, Long> {

    Optional<LaudoTecnico> findByTicketId(Long ticketId);

    List<LaudoTecnico> findByHorasTrabalhadasGreaterThanEqual(Double horas);
}

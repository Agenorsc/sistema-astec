package br.com.astec.repository;

import br.com.astec.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    Optional<Avaliacao> findByTicketId(Long ticketId);

    List<Avaliacao> findByNota(Integer nota);

    List<Avaliacao> findByNotaGreaterThanEqual(Integer nota);
}

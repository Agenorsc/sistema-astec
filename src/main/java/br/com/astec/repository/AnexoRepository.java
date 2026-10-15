package br.com.astec.repository;

import br.com.astec.model.Anexo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnexoRepository extends JpaRepository<Anexo, Long> {

    List<Anexo> findByTicketId(Long ticketId);

    List<Anexo> findByTipoArquivo(String tipoArquivo);
}

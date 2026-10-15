package br.com.astec.repository;

import br.com.astec.model.ItemPecaSubstituida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemPecaSubstituidaRepository extends JpaRepository<ItemPecaSubstituida, Long> {

    List<ItemPecaSubstituida> findByLaudoTecnicoId(Long laudoTecnicoId);

    List<ItemPecaSubstituida> findByNomePecaContainingIgnoreCase(String nomePeca);
}

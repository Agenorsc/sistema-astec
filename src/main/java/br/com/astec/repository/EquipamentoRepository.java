package br.com.astec.repository;

import br.com.astec.model.Equipamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {

    Optional<Equipamento> findByNumeroSerie(String numeroSerie);

    List<Equipamento> findByNomeContainingIgnoreCase(String nome);

    List<Equipamento> findByModeloContainingIgnoreCase(String modelo);

    long countByModelo(String modelo);
}

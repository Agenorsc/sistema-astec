package br.com.astec.repository;

import br.com.astec.model.CategoriaEquipamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaEquipamentoRepository extends JpaRepository<CategoriaEquipamento, Long> {
    Optional<CategoriaEquipamento> findByNome(String nome);
}

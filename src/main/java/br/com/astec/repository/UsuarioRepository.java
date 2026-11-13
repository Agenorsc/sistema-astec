package br.com.astec.repository;

import br.com.astec.model.Usuario;
import br.com.astec.model.enums.PerfilEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByPerfil(PerfilEnum perfil);

    List<Usuario> findByNomeContainingIgnoreCase(String nome);

    long countByPerfil(PerfilEnum perfil);

    boolean existsByPerfil(PerfilEnum perfil);

    Page<Usuario> findByPerfil(PerfilEnum perfil, Pageable pageable);

    Page<Usuario> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    Page<Usuario> findByPerfilAndNomeContainingIgnoreCase(PerfilEnum perfil, String nome, Pageable pageable);
}

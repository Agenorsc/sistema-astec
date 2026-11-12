package br.com.astec.service;

import br.com.astec.dto.PageResponse;
import br.com.astec.dto.usuario.UsuarioRequest;
import br.com.astec.dto.usuario.UsuarioResponse;
import br.com.astec.exception.ApiException;
import br.com.astec.model.Usuario;
import br.com.astec.model.enums.PerfilEnum;
import br.com.astec.repository.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public PageResponse<UsuarioResponse> listar(PerfilEnum perfil, String nome, int pagina, int tamanho) {
        PageRequest pageRequest = PageRequest.of(pagina, tamanho, Sort.by("nome").ascending());
        var resultado = perfil == null
                ? (nome == null || nome.isBlank()
                    ? usuarioRepository.findAll(pageRequest)
                    : usuarioRepository.findByNomeContainingIgnoreCase(nome, pageRequest))
                : (nome == null || nome.isBlank()
                    ? usuarioRepository.findByPerfil(perfil, pageRequest)
                    : usuarioRepository.findByPerfilAndNomeContainingIgnoreCase(perfil, nome, pageRequest));
        return PageResponse.from(resultado, UsuarioResponse::from);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscar(Long id) {
        return UsuarioResponse.from(encontrar(id));
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        String email = request.email().trim().toLowerCase(java.util.Locale.ROOT);
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Já existe um usuário com esse email.");
        }
        Usuario usuario = new Usuario(null, request.nome().trim(), email,
                passwordEncoder.encode(request.senha()), request.perfil());
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse atualizarPerfil(Long id, PerfilEnum perfil) {
        Usuario usuario = encontrar(id);
        usuario.setPerfil(perfil);
        return UsuarioResponse.from(usuario);
    }

    @Transactional
    public void remover(Long id) {
        Usuario usuario = encontrar(id);
        if (usuario.getPerfil() == PerfilEnum.ADMIN && usuarioRepository.countByPerfil(PerfilEnum.ADMIN) <= 1) {
            throw new ApiException(HttpStatus.CONFLICT, "O último administrador não pode ser removido.");
        }
        usuarioRepository.delete(usuario);
    }

    private Usuario encontrar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }
}

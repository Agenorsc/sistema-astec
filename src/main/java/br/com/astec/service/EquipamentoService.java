package br.com.astec.service;

import br.com.astec.dto.PageResponse;
import br.com.astec.dto.equipamento.EquipamentoRequest;
import br.com.astec.dto.equipamento.EquipamentoResponse;
import br.com.astec.exception.ApiException;
import br.com.astec.model.Equipamento;
import br.com.astec.model.Usuario;
import br.com.astec.model.enums.PerfilEnum;
import br.com.astec.repository.EquipamentoRepository;
import br.com.astec.repository.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EquipamentoService {

    private final EquipamentoRepository equipamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CurrentUserService currentUserService;

    public EquipamentoService(
            EquipamentoRepository equipamentoRepository,
            UsuarioRepository usuarioRepository,
            CurrentUserService currentUserService) {
        this.equipamentoRepository = equipamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public PageResponse<EquipamentoResponse> listar(int pagina, int tamanho) {
        Usuario usuario = currentUserService.usuario();
        var pageRequest = PageRequest.of(pagina, tamanho, Sort.by("nome").ascending());
        var resultado = usuario.getPerfil() == PerfilEnum.CLIENTE
                ? equipamentoRepository.findByClienteId(usuario.getId(), pageRequest)
                : equipamentoRepository.findAll(pageRequest);
        return PageResponse.from(resultado, EquipamentoResponse::from);
    }

    @Transactional(readOnly = true)
    public EquipamentoResponse buscar(Long id) {
        Equipamento equipamento = encontrar(id);
        verificarAcesso(equipamento);
        return EquipamentoResponse.from(equipamento);
    }

    @Transactional
    public EquipamentoResponse criar(EquipamentoRequest request) {
        Usuario usuario = currentUserService.usuario();
        if (usuario.getPerfil() == PerfilEnum.ADMIN && request.clienteId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "O cliente do equipamento é obrigatório.");
        }
        Usuario cliente = usuario.getPerfil() == PerfilEnum.ADMIN
                ? usuarioRepository.findById(request.clienteId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cliente não encontrado."))
                : usuario;
        if (cliente.getPerfil() != PerfilEnum.CLIENTE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "O equipamento deve pertencer a um usuário cliente.");
        }
        validarNumeroSerie(request.numeroSerie(), null);
        Equipamento equipamento = new Equipamento(null, request.nome().trim(), request.modelo(),
                normalizarNumeroSerie(request.numeroSerie()));
        equipamento.setCliente(cliente);
        return EquipamentoResponse.from(equipamentoRepository.save(equipamento));
    }

    @Transactional
    public EquipamentoResponse atualizar(Long id, EquipamentoRequest request) {
        Equipamento equipamento = encontrar(id);
        Usuario usuario = currentUserService.usuario();
        if (usuario.getPerfil() == PerfilEnum.ADMIN && request.clienteId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "O cliente do equipamento é obrigatório.");
        }
        Usuario cliente = usuario.getPerfil() == PerfilEnum.ADMIN
                ? usuarioRepository.findById(request.clienteId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cliente não encontrado."))
                : usuario;
        if (cliente.getPerfil() != PerfilEnum.CLIENTE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "O equipamento deve pertencer a um usuário cliente.");
        }
        if (usuario.getPerfil() != PerfilEnum.ADMIN) {
            verificarAcesso(equipamento);
        }
        validarNumeroSerie(request.numeroSerie(), id);
        equipamento.setNome(request.nome().trim());
        equipamento.setModelo(request.modelo());
        equipamento.setNumeroSerie(normalizarNumeroSerie(request.numeroSerie()));
        equipamento.setCliente(cliente);
        return EquipamentoResponse.from(equipamento);
    }

    @Transactional
    public void remover(Long id) {
        Equipamento equipamento = encontrar(id);
        if (currentUserService.usuario().getPerfil() != PerfilEnum.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Somente administradores podem remover equipamentos.");
        }
        equipamentoRepository.delete(equipamento);
    }

    private Equipamento encontrar(Long id) {
        return equipamentoRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Equipamento não encontrado."));
    }

    private void verificarAcesso(Equipamento equipamento) {
        Usuario usuario = currentUserService.usuario();
        if (usuario.getPerfil() == PerfilEnum.CLIENTE
                && (equipamento.getCliente() == null || !equipamento.getCliente().getId().equals(usuario.getId()))) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Equipamento não encontrado.");
        }
    }

    private void validarNumeroSerie(String numeroSerie, Long idIgnorado) {
        if (numeroSerie == null || numeroSerie.isBlank()) {
            return;
        }
        equipamentoRepository.findByNumeroSerie(numeroSerie.trim())
                .filter(equipamento -> idIgnorado == null || !equipamento.getId().equals(idIgnorado))
                .ifPresent(equipamento -> {
                    throw new ApiException(HttpStatus.CONFLICT, "Já existe um equipamento com esse número de série.");
                });
    }

    private String normalizarNumeroSerie(String numeroSerie) {
        return numeroSerie == null || numeroSerie.isBlank() ? null : numeroSerie.trim();
    }
}

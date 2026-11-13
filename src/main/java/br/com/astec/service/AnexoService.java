package br.com.astec.service;

import br.com.astec.dto.anexo.AnexoResponse;
import br.com.astec.exception.ApiException;
import br.com.astec.model.Anexo;
import br.com.astec.model.Ticket;
import br.com.astec.model.Usuario;
import br.com.astec.model.enums.PerfilEnum;
import br.com.astec.repository.AnexoRepository;
import br.com.astec.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class AnexoService {

    private static final long LIMITE_BYTES = 20L * 1024 * 1024;
    private final AnexoRepository anexoRepository;
    private final TicketRepository ticketRepository;
    private final CurrentUserService currentUserService;
    private final Path diretorio;

    public AnexoService(
            AnexoRepository anexoRepository,
            TicketRepository ticketRepository,
            CurrentUserService currentUserService,
            @Value("${app.attachments.directory:./data/anexos}") String diretorio) {
        this.anexoRepository = anexoRepository;
        this.ticketRepository = ticketRepository;
        this.currentUserService = currentUserService;
        this.diretorio = Path.of(diretorio).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public java.util.List<AnexoResponse> listar(Long ticketId) {
        Ticket ticket = encontrarTicket(ticketId);
        verificarAcesso(ticket);
        return anexoRepository.findByTicketId(ticketId).stream().map(AnexoResponse::from).toList();
    }

    @Transactional
    public AnexoResponse salvar(Long ticketId, MultipartFile arquivo) {
        Ticket ticket = encontrarTicket(ticketId);
        verificarAcesso(ticket);
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "O arquivo enviado está vazio.");
        }
        if (arquivo.getSize() > LIMITE_BYTES) {
            throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "O arquivo excede o limite de 20 MB.");
        }

        String nomeOriginal = nomeOriginal(arquivo.getOriginalFilename());
        String extensao = extensao(nomeOriginal);
        byte[] conteudo;
        try {
            conteudo = arquivo.getBytes();
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível ler o arquivo enviado.");
        }
        validarConteudo(extensao, conteudo);

        String nomeArmazenado = UUID.randomUUID() + "." + extensao;
        Path destino = diretorio.resolve(nomeArmazenado).normalize();
        if (!destino.startsWith(diretorio)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Nome de arquivo inválido.");
        }
        try {
            Files.createDirectories(diretorio);
            Files.write(destino, conteudo, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível armazenar o arquivo.");
        }

        String tipo = switch (extensao) {
            case "jpg" -> "image/jpeg";
            case "pdf" -> "application/pdf";
            case "mp4" -> "video/mp4";
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Tipo de arquivo não permitido.");
        };
        Anexo anexo = new Anexo(null, nomeOriginal, nomeArmazenado, tipo,
                (double) conteudo.length / (1024 * 1024), ticket);
        return AnexoResponse.from(anexoRepository.save(anexo));
    }

    @Transactional(readOnly = true)
    public DownloadAnexo baixar(Long ticketId, Long anexoId) {
        Ticket ticket = encontrarTicket(ticketId);
        verificarAcesso(ticket);
        Anexo anexo = anexoRepository.findByIdAndTicketId(anexoId, ticketId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Anexo não encontrado."));
        Path caminho = caminhoSeguro(anexo.getUrlArquivo());
        if (!Files.isRegularFile(caminho)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Arquivo do anexo não encontrado.");
        }
        return new DownloadAnexo(anexo.getNomeArquivo(), anexo.getTipoArquivo(), new FileSystemResource(caminho));
    }

    @Transactional
    public void remover(Long ticketId, Long anexoId) {
        Ticket ticket = encontrarTicket(ticketId);
        verificarAcesso(ticket);
        if (currentUserService.usuario().getPerfil() == PerfilEnum.CLIENTE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Somente técnicos e administradores podem remover anexos.");
        }
        Anexo anexo = anexoRepository.findByIdAndTicketId(anexoId, ticketId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Anexo não encontrado."));
        Path caminho = caminhoSeguro(anexo.getUrlArquivo());
        anexoRepository.delete(anexo);
        try {
            Files.deleteIfExists(caminho);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "O registro foi removido, mas o arquivo não pôde ser excluído.");
        }
    }

    private Ticket encontrarTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Chamado não encontrado."));
    }

    private void verificarAcesso(Ticket ticket) {
        Usuario usuario = currentUserService.usuario();
        if (usuario.getPerfil() == PerfilEnum.CLIENTE && !ticket.getCliente().getId().equals(usuario.getId())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Chamado não encontrado.");
        }
    }

    private Path caminhoSeguro(String nomeArmazenado) {
        Path caminho = diretorio.resolve(nomeArmazenado).normalize();
        if (!caminho.startsWith(diretorio)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Anexo não encontrado.");
        }
        return caminho;
    }

    private String nomeOriginal(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "O nome do arquivo é obrigatório.");
        }
        String basename = nome.substring(Math.max(nome.lastIndexOf('/'), nome.lastIndexOf('\\')) + 1);
        return basename.replaceAll("[\\r\\n]", "").trim();
    }

    private String extensao(String nome) {
        int ponto = nome.lastIndexOf('.');
        if (ponto < 0 || ponto == nome.length() - 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Extensão de arquivo não permitida.");
        }
        String extensao = nome.substring(ponto + 1).toLowerCase(Locale.ROOT);
        if (!java.util.Set.of("jpg", "pdf", "mp4").contains(extensao)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Somente arquivos JPG, PDF e MP4 são permitidos.");
        }
        return extensao;
    }

    private void validarConteudo(String extensao, byte[] bytes) {
        boolean valido = switch (extensao) {
            case "jpg" -> bytes.length >= 3 && (bytes[0] & 0xFF) == 0xFF
                    && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF;
            case "pdf" -> bytes.length >= 5 && bytes[0] == '%'
                    && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F' && bytes[4] == '-';
            case "mp4" -> bytes.length >= 8 && bytes[4] == 'f'
                    && bytes[5] == 't' && bytes[6] == 'y' && bytes[7] == 'p';
            default -> false;
        };
        if (!valido) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "O conteúdo do arquivo não corresponde à extensão informada.");
        }
    }

    public record DownloadAnexo(String nome, String tipoConteudo, Resource recurso) {
    }
}

package br.com.astec.controller.web;

import br.com.astec.dto.web.RequisicaoAvaliacao;
import br.com.astec.dto.web.RequisicaoNovaMensagem;
import br.com.astec.dto.web.RequisicaoNovoLaudo;
import br.com.astec.dto.web.RequisicaoNovoTicket;
import br.com.astec.model.Ticket;
import br.com.astec.model.Usuario;
import br.com.astec.model.enums.PerfilEnum;
import br.com.astec.model.enums.StatusTicketEnum;
import br.com.astec.model.enums.UrgenciaEnum;
import br.com.astec.repository.EquipamentoRepository;
import br.com.astec.repository.UsuarioRepository;
import br.com.astec.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/tickets")
public class TicketWebController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @GetMapping("/novo")
    public String formularioNovo(Model model) {
        if (!model.containsAttribute("requisicaoNovoTicket")) {
            model.addAttribute("requisicaoNovoTicket", new RequisicaoNovoTicket());
        }
        carregarDadosFormulario(model);
        return "tickets/formulario";
    }

    @PostMapping("/novo")
    public String salvarNovo(
            @Valid @ModelAttribute("requisicaoNovoTicket") RequisicaoNovoTicket requisicao,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            carregarDadosFormulario(model);
            return "tickets/formulario";
        }

        Ticket criado = ticketService.criarTicket(requisicao);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Chamado #" + criado.getId() + " cadastrado com sucesso!");
        return "redirect:/tickets/" + criado.getId();
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model) {
        Ticket ticket = ticketService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Chamado não encontrado"));

        model.addAttribute("ticket", ticket);
        model.addAttribute("todosStatus", StatusTicketEnum.values());
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("requisicaoNovaMensagem", new RequisicaoNovaMensagem());
        model.addAttribute("requisicaoNovoLaudo", new RequisicaoNovoLaudo());
        model.addAttribute("requisicaoAvaliacao", new RequisicaoAvaliacao());

        return "tickets/detalhes";
    }

    @PostMapping("/{id}/status")
    public String atualizarStatus(
            @PathVariable Long id,
            @RequestParam StatusTicketEnum status,
            RedirectAttributes redirectAttributes) {
        ticketService.alterarStatus(id, status);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Status atualizado para: " + status.getDescricao());
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/mensagens")
    public String enviarMensagem(
            @PathVariable Long id,
            @Valid @ModelAttribute("requisicaoNovaMensagem") RequisicaoNovaMensagem requisicao,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Não foi possível enviar a mensagem: campos inválidos.");
            return "redirect:/tickets/" + id;
        }

        ticketService.adicionarMensagem(id, requisicao);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Mensagem enviada com sucesso!");
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/laudo")
    public String registrarLaudo(
            @PathVariable Long id,
            @Valid @ModelAttribute("requisicaoNovoLaudo") RequisicaoNovoLaudo requisicao,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao emitir laudo: preencha todos os campos obrigatórios.");
            return "redirect:/tickets/" + id;
        }

        ticketService.emitirLaudo(id, requisicao);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Laudo Técnico registrado e chamado marcado como RESOLVIDO!");
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/avaliacao")
    public String avaliar(
            @PathVariable Long id,
            @Valid @ModelAttribute("requisicaoAvaliacao") RequisicaoAvaliacao requisicao,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao registrar avaliação: informe nota entre 1 e 5.");
            return "redirect:/tickets/" + id;
        }

        ticketService.avaliarTicket(id, requisicao);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Avaliação registrada e chamado concluído (FECHADO)!");
        return "redirect:/tickets/" + id;
    }

    private void carregarDadosFormulario(Model model) {
        List<Usuario> clientes = usuarioRepository.findByPerfil(PerfilEnum.CLIENTE);
        model.addAttribute("clientes", clientes);
        model.addAttribute("equipamentos", equipamentoRepository.findAll());
        model.addAttribute("urgencias", UrgenciaEnum.values());
    }
}

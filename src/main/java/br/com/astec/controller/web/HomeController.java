package br.com.astec.controller.web;

import br.com.astec.model.Ticket;
import br.com.astec.model.enums.StatusTicketEnum;
import br.com.astec.model.enums.UrgenciaEnum;
import br.com.astec.repository.TicketRepository;
import br.com.astec.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @GetMapping({"/", "/home"})
    public String home(
            @RequestParam(required = false) StatusTicketEnum status,
            @RequestParam(required = false) UrgenciaEnum urgencia,
            @RequestParam(required = false) String termo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho,
            Model model) {

        PageRequest pageable = PageRequest.of(pagina, tamanho, Sort.by("dataCriacao").descending());
        Page<Ticket> pageTickets = ticketService.pesquisarComFiltro(status, urgencia, termo, pageable);

        // Estatísticas do Dashboard
        long totalTickets = ticketRepository.count();
        long totalAbertos = ticketRepository.countByStatus(StatusTicketEnum.ABERTO);
        long totalEmAtendimento = ticketRepository.countByStatus(StatusTicketEnum.EM_ATENDIMENTO);
        long totalResolvidos = ticketRepository.countByStatus(StatusTicketEnum.RESOLVIDO);
        long totalFechados = ticketRepository.countByStatus(StatusTicketEnum.FECHADO);

        model.addAttribute("tickets", pageTickets.getContent());
        model.addAttribute("paginaAtual", pagina);
        model.addAttribute("totalPaginas", pageTickets.getTotalPages());
        model.addAttribute("totalElementos", pageTickets.getTotalElements());

        // Parâmetros de filtro
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("urgenciaSelecionada", urgencia);
        model.addAttribute("termoPesquisa", termo);
        model.addAttribute("todosStatus", StatusTicketEnum.values());
        model.addAttribute("todasUrgencias", UrgenciaEnum.values());

        // Métricas
        model.addAttribute("totalTickets", totalTickets);
        model.addAttribute("totalAbertos", totalAbertos);
        model.addAttribute("totalEmAtendimento", totalEmAtendimento);
        model.addAttribute("totalResolvidos", totalResolvidos);
        model.addAttribute("totalFechados", totalFechados);

        return "home";
    }
}

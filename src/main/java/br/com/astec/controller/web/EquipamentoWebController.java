package br.com.astec.controller.web;

import br.com.astec.dto.web.RequisicaoNovoEquipamento;
import br.com.astec.model.Equipamento;
import br.com.astec.service.EquipamentoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/equipamentos")
public class EquipamentoWebController {

    @Autowired
    private EquipamentoService equipamentoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("equipamentos", equipamentoService.listarTodos());
        return "equipamentos/lista";
    }

    @GetMapping("/novo")
    public String formularioNovo(Model model) {
        if (!model.containsAttribute("requisicaoNovoEquipamento")) {
            model.addAttribute("requisicaoNovoEquipamento", new RequisicaoNovoEquipamento());
        }
        model.addAttribute("categorias", equipamentoService.listarCategorias());
        return "equipamentos/formulario";
    }

    @PostMapping("/novo")
    public String salvarNovo(
            @Valid @ModelAttribute("requisicaoNovoEquipamento") RequisicaoNovoEquipamento requisicao,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("categorias", equipamentoService.listarCategorias());
            return "equipamentos/formulario";
        }

        Equipamento criado = equipamentoService.criarEquipamento(requisicao);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Equipamento '" + criado.getNome() + "' cadastrado com sucesso!");
        return "redirect:/equipamentos";
    }
}

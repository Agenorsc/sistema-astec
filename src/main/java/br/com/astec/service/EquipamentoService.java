package br.com.astec.service;

import br.com.astec.dto.web.RequisicaoNovoEquipamento;
import br.com.astec.model.CategoriaEquipamento;
import br.com.astec.model.Equipamento;
import br.com.astec.repository.CategoriaEquipamentoRepository;
import br.com.astec.repository.EquipamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EquipamentoService {

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @Autowired
    private CategoriaEquipamentoRepository categoriaRepository;

    public List<Equipamento> listarTodos() {
        return equipamentoRepository.findAll();
    }

    public Optional<Equipamento> buscarPorId(Long id) {
        return equipamentoRepository.findById(id);
    }

    public List<CategoriaEquipamento> listarCategorias() {
        return categoriaRepository.findAll();
    }

    @Transactional
    public Equipamento criarEquipamento(RequisicaoNovoEquipamento req) {
        Equipamento eq = req.toEquipamento();
        if (req.getCategoriaIds() != null && !req.getCategoriaIds().isEmpty()) {
            List<CategoriaEquipamento> cats = categoriaRepository.findAllById(req.getCategoriaIds());
            eq.setCategorias(cats);
        }
        return equipamentoRepository.save(eq);
    }
}

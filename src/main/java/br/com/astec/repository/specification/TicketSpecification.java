package br.com.astec.repository.specification;

import br.com.astec.model.Ticket;
import br.com.astec.model.enums.StatusTicketEnum;
import br.com.astec.model.enums.UrgenciaEnum;
import org.springframework.data.jpa.domain.Specification;

/**
 * Especificações para Consultas Dinâmicas (JPA Criteria / Spring Data Specification)
 * Conforme ensinado na aula de Spring Data (Slides de Consultas Dinâmicas / Criteria - slides 18 a 21).
 */
public class TicketSpecification {

    public static Specification<Ticket> comStatus(StatusTicketEnum status) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Ticket> comUrgencia(UrgenciaEnum urgencia) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (urgencia == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("urgencia"), urgencia);
        };
    }

    public static Specification<Ticket> comTermoTitulo(String termo) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (termo == null || termo.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("titulo")), "%" + termo.toLowerCase() + "%");
        };
    }

    public static Specification<Ticket> comClienteId(Long clienteId) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (clienteId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("cliente").get("id"), clienteId);
        };
    }

    public static Specification<Ticket> comFiltros(StatusTicketEnum status, UrgenciaEnum urgencia, String termo) {
        return Specification.where(comStatus(status))
                .and(comUrgencia(urgencia))
                .and(comTermoTitulo(termo));
    }
}

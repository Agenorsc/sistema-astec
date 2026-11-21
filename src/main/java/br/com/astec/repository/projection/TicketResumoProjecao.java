package br.com.astec.repository.projection;

import br.com.astec.model.enums.StatusTicketEnum;
import br.com.astec.model.enums.UrgenciaEnum;

/**
 * Interface de Projeção (Projeções com Spring Data)
 * Conforme ensinado na aula de Spring Data (Slides Projeção - slides 5 e 15).
 */
public interface TicketResumoProjecao {
    Long getId();
    String getCodigo();
    String getTitulo();
    StatusTicketEnum getStatus();
    UrgenciaEnum getUrgencia();
}

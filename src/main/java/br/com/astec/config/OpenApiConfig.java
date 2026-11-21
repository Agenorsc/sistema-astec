package br.com.astec.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Sistema Astec - API de Assistência Técnica",
                version = "1.0.0",
                description = "API RESTful desenvolvida conforme as especificações da disciplina de Programação WEB (Católica SC). " +
                        "Fornece endpoints para gerenciamento de chamados técnicos, equipamentos, laudos e avaliações com semântica HTTP estrita.",
                contact = @Contact(
                        name = "Equipe Astec - Católica de Santa Catarina",
                        email = "suporte@astec.com.br"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                )
        )
)
public class OpenApiConfig {
}

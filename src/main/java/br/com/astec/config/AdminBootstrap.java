package br.com.astec.config;

import br.com.astec.model.Usuario;
import br.com.astec.model.enums.PerfilEnum;
import br.com.astec.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Configuration
public class AdminBootstrap {

    @Bean
    public ApplicationRunner createInitialAdmin(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.demo-data:false}") boolean demoDataEnabled,
            @Value("${app.bootstrap.admin.name:}") String name,
            @Value("${app.bootstrap.admin.email:}") String email,
            @Value("${app.bootstrap.admin.password:}") String password) {
        return args -> {
            if (demoDataEnabled) {
                return;
            }

            boolean hasName = StringUtils.hasText(name);
            boolean hasEmail = StringUtils.hasText(email);
            boolean hasPassword = StringUtils.hasText(password);
            if (!hasName && !hasEmail && !hasPassword) {
                return;
            }
            if (!hasName || !hasEmail || !hasPassword) {
                throw new IllegalStateException(
                        "Configure ASTEC_BOOTSTRAP_ADMIN_NAME, ASTEC_BOOTSTRAP_ADMIN_EMAIL e ASTEC_BOOTSTRAP_ADMIN_PASSWORD juntos.");
            }
            if (password.length() < 12) {
                throw new IllegalStateException("A senha inicial do administrador deve ter pelo menos 12 caracteres.");
            }
            if (usuarioRepository.existsByPerfil(PerfilEnum.ADMIN)) {
                return;
            }

            String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
            if (usuarioRepository.findByEmail(normalizedEmail).isPresent()) {
                throw new IllegalStateException("O email configurado para o administrador já pertence a outro perfil.");
            }

            usuarioRepository.save(new Usuario(
                    null,
                    name.trim(),
                    normalizedEmail,
                    passwordEncoder.encode(password),
                    PerfilEnum.ADMIN));
        };
    }
}

package br.com.astec.dto.usuario;

import br.com.astec.model.Usuario;
import br.com.astec.model.enums.PerfilEnum;

public record UsuarioResponse(Long id, String nome, String email, PerfilEnum perfil) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil());
    }
}

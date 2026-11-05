package br.com.hobbyhub.dto;

import br.com.hobbyhub.model.Usuario;
import java.util.List;

public record EditarPerfilDTO(String nome, String email, String nomeUsuario, String biografia,
        String urlFotoPerfil, List<Long> tagsInteresseIds) {
    public static EditarPerfilDTO from(Usuario usuario) {
        return new EditarPerfilDTO(
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getNomeUsuario(),
                usuario.getBiografia(),
                usuario.getUrlFotoPerfil(),
                usuario.getTagsSeguidas().stream().map(tag -> tag.getId()).sorted().toList());
    }
}

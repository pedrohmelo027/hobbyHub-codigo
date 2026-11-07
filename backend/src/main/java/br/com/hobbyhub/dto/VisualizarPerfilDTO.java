package br.com.hobbyhub.dto;

import br.com.hobbyhub.model.Usuario;
import java.util.List;

public record VisualizarPerfilDTO(
        String nome,
        String nomeUsuario,
        String biografia,
        String urlFotoPerfil,
        List<PostViewDTO> posts,
        int diasSeguidos,
        boolean meuPerfil) {

    public static VisualizarPerfilDTO from(
            Usuario usuario, List<PostViewDTO> posts, int diasSeguidos, boolean meuPerfil) {
        return new VisualizarPerfilDTO(
                usuario.getNome(),
                usuario.getNomeUsuario(),
                usuario.getBiografia(),
                usuario.getUrlFotoPerfil(),
                posts,
                diasSeguidos,
                meuPerfil);
    }
}

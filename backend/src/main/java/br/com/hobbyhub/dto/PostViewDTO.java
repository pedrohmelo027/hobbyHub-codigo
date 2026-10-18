package br.com.hobbyhub.dto;

import br.com.hobbyhub.model.Post;
import br.com.hobbyhub.model.Tag;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record PostViewDTO(
        Long id,
        String conteudo,
        LocalDateTime dataCriacao,
        String urlImagem,
        String autor,
        List<String> tags,
        boolean proprio) {

    public static PostViewDTO from(Post post, String email) {
        List<String> nomesTags =
                post.getTags().stream()
                        .sorted(Comparator.comparing(Tag::getTipo))
                        .map(Tag::getNome)
                        .toList();

        return new PostViewDTO(
                post.getId(),
                post.getConteudo(),
                post.getDataCriacao(),
                post.getUrlImagem(),
                post.getUsuario().getNomeUsuario(),
                nomesTags,
                post.getUsuario().getEmail().equalsIgnoreCase(email));
    }

    public boolean isVideo() {
        return urlImagem != null && (urlImagem.endsWith(".mp4") || urlImagem.endsWith(".webm"));
    }
}

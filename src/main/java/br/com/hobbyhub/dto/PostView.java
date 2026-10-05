package br.com.hobbyhub.dto;

import br.com.hobbyhub.model.Post;
import br.com.hobbyhub.model.Tag;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record PostView(
        Long id,
        String conteudo,
        LocalDateTime dataCriacao,
        String urlImagem,
        String autor,
        List<String> tags) {

    public static PostView from(Post post) {
        List<String> nomesTags =
                post.getTags().stream()
                        .sorted(Comparator.comparing(Tag::getTipo))
                        .map(Tag::getNome)
                        .toList();

        return new PostView(
                post.getId(),
                post.getConteudo(),
                post.getDataCriacao(),
                post.getUrlImagem(),
                post.getUsuario().getNomeUsuario(),
                nomesTags);
    }

    public boolean isVideo() {
        return urlImagem != null && (urlImagem.endsWith(".mp4") || urlImagem.endsWith(".webm"));
    }
}

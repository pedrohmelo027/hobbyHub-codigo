package br.com.hobbyhub.jsf.utils;

import br.com.hobbyhub.jsf.dto.PostViewDTO;
import java.util.List;
import java.util.Locale;

public final class FiltroPostsPorTag {

    private FiltroPostsPorTag() {
    }

    public static List<PostViewDTO> filtrar(List<PostViewDTO> posts, String pesquisa) {
        if (pesquisa == null || pesquisa.isBlank()) {
            return posts;
        }

        String termo = pesquisa.strip().toLowerCase(Locale.ROOT);
        return posts.stream()
                .filter(post -> post.getTags() != null)
                .filter(post -> post.getTags().stream()
                        .anyMatch(tag -> tag != null && tag.toLowerCase(Locale.ROOT).contains(termo)))
                .toList();
    }
}

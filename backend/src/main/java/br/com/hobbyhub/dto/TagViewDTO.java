package br.com.hobbyhub.dto;

import br.com.hobbyhub.model.Tag;
import br.com.hobbyhub.model.TipoDeTag;

public record TagViewDTO(Long id, String nome, TipoDeTag tipo, Long tagGeralId) {

    public static TagViewDTO from(Tag tag) {
        return new TagViewDTO(tag.getId(), tag.getNome(), tag.getTipo(),
                tag.getTagGeral() == null ? null : tag.getTagGeral().getId());
    }
}

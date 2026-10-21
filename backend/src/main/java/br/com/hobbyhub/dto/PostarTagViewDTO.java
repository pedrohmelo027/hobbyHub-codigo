package br.com.hobbyhub.dto;

import br.com.hobbyhub.model.Tag;
import br.com.hobbyhub.model.TipoDeTag;

public record PostarTagViewDTO(Long id, String nome, TipoDeTag tipo, Long tagGeralId) {

    public static PostarTagViewDTO from(Tag tag) {
        return new PostarTagViewDTO(tag.getId(), tag.getNome(), tag.getTipo(),
                tag.getTagGeral() == null ? null : tag.getTagGeral().getId());
    }
}

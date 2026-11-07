package br.com.hobbyhub.dto;

import br.com.hobbyhub.model.Tag;
import br.com.hobbyhub.model.TipoDeTag;

public record ManterTagDTO(Long id, String nome, TipoDeTag tipo, Long tagGeralId, String tagGeralNome) {
    public static ManterTagDTO from(Tag tag) {
        return new ManterTagDTO(
                tag.getId(),
                tag.getNome(),
                tag.getTipo(),
                tag.getTagGeral() == null ? null : tag.getTagGeral().getId(),
                tag.getTagGeral() == null ? null : tag.getTagGeral().getNome());
    }
}

package br.com.hobbyhub.dto;

import br.com.hobbyhub.model.TipoDeTag;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManterTagFormDTO {
    @NotBlank(message = "Informe o nome da tag.")
    private String nome;

    private TipoDeTag tipo;
    private Long tagGeralId;
}

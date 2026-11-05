package br.com.hobbyhub.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditarPerfilFormDTO {
    @NotBlank(message = "Informe seu nome.")
    private String nome;

    private String biografia;

    private List<Long> tagsInteresseIds;
}

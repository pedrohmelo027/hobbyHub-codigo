package br.com.hobbyhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostEdicaoFormDTO {

    @NotBlank(message = "Escreva o conteúdo do post.")
    @Size(max = 2000, message = "Use até 2000 caracteres.")
    private String conteudo;
}

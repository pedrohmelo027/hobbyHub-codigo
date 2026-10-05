package br.com.hobbyhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostForm {

    @NotBlank(message = "Escreva o conteúdo do post.")
    @Size(max = 2000, message = "Use até 2000 caracteres.")
    private String conteudo;

    @NotNull(message = "Selecione uma tag geral.")
    private Long tagGeralId;

    @NotNull(message = "Selecione uma tag específica.")
    private Long tagEspecificaId;
}

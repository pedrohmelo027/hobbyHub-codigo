package br.com.hobbyhub.jsf.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
public class PostFormDTO implements Serializable {

    @NotBlank(message = "Escreva o conteúdo do post.")
    @Size(max = 2000, message = "Use até 2000 caracteres.")
    private String conteudo;

    @NotNull(message = "Selecione uma tag geral.")
    private Long tagGeralId;

    @NotNull(message = "Selecione uma tag específica.")
    private Long tagEspecificaId;
}

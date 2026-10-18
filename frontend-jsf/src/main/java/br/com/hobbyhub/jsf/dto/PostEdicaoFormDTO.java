package br.com.hobbyhub.jsf.dto;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostEdicaoFormDTO implements Serializable {

    @NotBlank(message = "Escreva o conteúdo do post.")
    @Size(max = 2000, message = "Use até 2000 caracteres.")
    private String conteudo;
}

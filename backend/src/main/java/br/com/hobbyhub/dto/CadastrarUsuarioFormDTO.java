package br.com.hobbyhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CadastrarUsuarioFormDTO {
    @NotBlank(message = "Informe seu nome.")
    private String nome;

    @NotBlank(message = "Informe seu e-mail.")
    @Email(message = "Informe um e-mail válido.")
    private String email;

    @NotBlank(message = "Informe um nome de usuário.")
    private String nomeUsuario;

    @NotBlank(message = "Informe uma senha.")
    private String senha;

    private String biografia;

    private List<Long> tagsInteresseIds;
}

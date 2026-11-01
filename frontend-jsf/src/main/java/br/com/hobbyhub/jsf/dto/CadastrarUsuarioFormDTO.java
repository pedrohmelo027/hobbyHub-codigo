package br.com.hobbyhub.jsf.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CadastrarUsuarioFormDTO implements Serializable {
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

    private List<Long> tagsInteresseIds = new ArrayList<>();
}

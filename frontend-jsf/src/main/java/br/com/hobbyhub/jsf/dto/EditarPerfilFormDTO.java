package br.com.hobbyhub.jsf.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditarPerfilFormDTO implements Serializable {
    private String nome;
    private String email;
    private String nomeUsuario;
    private String biografia;
    private String urlFotoPerfil;

    private List<Long> tagsInteresseIds = new ArrayList<>();
}

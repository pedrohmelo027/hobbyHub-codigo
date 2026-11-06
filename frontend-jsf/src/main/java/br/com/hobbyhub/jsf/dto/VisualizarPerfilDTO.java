package br.com.hobbyhub.jsf.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class VisualizarPerfilDTO implements Serializable {
    private String nome;
    private String nomeUsuario;
    private String biografia;
    private String urlFotoPerfil;
    private List<PostViewDTO> posts = List.of();
    private int diasSeguidos;
    private boolean meuPerfil;
}

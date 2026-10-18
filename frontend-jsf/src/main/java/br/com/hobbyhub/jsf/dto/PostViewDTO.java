package br.com.hobbyhub.jsf.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostViewDTO implements Serializable {

    private Long id;
    private String conteudo;
    private LocalDateTime dataCriacao;
    private String urlImagem;
    private String autor;
    private List<String> tags;
    private boolean video;
    private boolean proprio;

    public String getDataFormatada() {
        return dataCriacao == null ? "" : dataCriacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}

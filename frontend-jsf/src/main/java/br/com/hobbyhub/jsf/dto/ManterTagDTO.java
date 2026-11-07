package br.com.hobbyhub.jsf.dto;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManterTagDTO implements Serializable {
    private Long id;
    private String nome;
    private String tipo;
    private Long tagGeralId;
    private String tagGeralNome;
}

package br.com.hobbyhub.jsf.dto;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManterTagFormDTO implements Serializable {
    private Long id;
    private String nome;
    private String tipo = "GERAL";
    private Long tagGeralId;
}

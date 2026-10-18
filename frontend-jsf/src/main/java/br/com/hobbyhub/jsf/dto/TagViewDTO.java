package br.com.hobbyhub.jsf.dto;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
public class TagViewDTO implements Serializable {

    private Long id;
    private String nome;
    private String tipo;
    private Long tagGeralId;
}

package br.com.hobbyhub.jsf.dto;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostFormValidationTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private final Validator validator = FACTORY.getValidator();

    @AfterAll
    static void encerrar() {
        FACTORY.close();
    }

    @Test
    void validaConteudoETagsDoFormJsf() {
        PostFormDTO form = new PostFormDTO();
        form.setConteudo("   ");
        assertThat(validator.validate(form)).hasSize(3);
    }

    @Test
    void limitaConteudoAntesDeChamarApi() {
        PostFormDTO form = valido();
        form.setConteudo("x".repeat(2001));
        assertThat(validator.validate(form)).hasSize(1);
    }

    @Test
    void aceitaFormularioCompleto() {
        assertThat(validator.validate(valido())).isEmpty();
    }

    private PostFormDTO valido() {
        PostFormDTO form = new PostFormDTO();
        form.setConteudo("Meu hobby favorito");
        form.setTagGeralId(1L);
        form.setTagEspecificaId(4L);
        return form;
    }

    @Test
    void validaTextoDaEdicao() {
        PostEdicaoFormDTO form = new PostEdicaoFormDTO();
        form.setConteudo("   ");
        assertThat(validator.validate(form)).hasSize(1);
        form.setConteudo("x".repeat(2001));
        assertThat(validator.validate(form)).hasSize(1);
        form.setConteudo("Texto editado");
        assertThat(validator.validate(form)).isEmpty();
    }
}

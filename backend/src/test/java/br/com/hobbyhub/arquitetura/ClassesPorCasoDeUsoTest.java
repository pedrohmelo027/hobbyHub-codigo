package br.com.hobbyhub.arquitetura;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ClassesPorCasoDeUsoTest {

    @Test
    void controllersEServicesSeguemCamadasENomesDosCasosDeUso() {
        assertClassesExistem(
                "br.com.hobbyhub.controller.PostarController",
                "br.com.hobbyhub.controller.VisualizarPostController",
                "br.com.hobbyhub.controller.ManterPostsController",
                "br.com.hobbyhub.service.PostarService",
                "br.com.hobbyhub.service.VisualizarPostService",
                "br.com.hobbyhub.service.ManterPostsService",
                "br.com.hobbyhub.dto.PostarFormDTO",
                "br.com.hobbyhub.dto.ManterPostsFormDTO",
                "br.com.hobbyhub.utils.ApiExceptionHandler",
                "br.com.hobbyhub.utils.MidiaService");
    }

    @Test
    void operacoesDoMesmoCasoDeUsoNaoCriamClassesSeparadas() {
        assertClassesAusentes(
                "br.com.hobbyhub.controller.EditarPostController",
                "br.com.hobbyhub.controller.ExcluirPostController",
                "br.com.hobbyhub.controller.ListarTagsController",
                "br.com.hobbyhub.controller.VisualizarMidiaController",
                "br.com.hobbyhub.service.EditarPostService",
                "br.com.hobbyhub.service.ExcluirPostService",
                "br.com.hobbyhub.service.ListarTagsService");
    }

    private void assertClassesExistem(String... nomes) {
        for (String nome : nomes) {
            assertThat(getClass().getClassLoader().getResource(nome.replace('.', '/') + ".class"))
                    .as("classe %s", nome)
                    .isNotNull();
        }
    }

    private void assertClassesAusentes(String... nomes) {
        for (String nome : nomes) {
            assertThat(getClass().getClassLoader().getResource(nome.replace('.', '/') + ".class"))
                    .as("classe obsoleta %s", nome)
                    .isNull();
        }
    }
}

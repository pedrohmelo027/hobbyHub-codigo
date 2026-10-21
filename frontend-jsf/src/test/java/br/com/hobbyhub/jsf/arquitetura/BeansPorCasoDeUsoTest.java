package br.com.hobbyhub.jsf.arquitetura;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BeansPorCasoDeUsoTest {

    @Test
    void paginasJsfUsamBeansNoPacoteBean() {
        assertClassesExistem(
                "br.com.hobbyhub.jsf.bean.LoginBean",
                "br.com.hobbyhub.jsf.bean.PostarBean",
                "br.com.hobbyhub.jsf.bean.VisualizarPostsBean",
                "br.com.hobbyhub.jsf.utils.HobbyHubApiClient",
                "br.com.hobbyhub.jsf.utils.MidiaProxyServlet");
    }

    private void assertClassesExistem(String... nomes) {
        for (String nome : nomes) {
            assertThat(getClass().getClassLoader().getResource(nome.replace('.', '/') + ".class"))
                    .as("classe %s", nome)
                    .isNotNull();
        }
    }
}

package br.com.hobbyhub.jsf.service;

import br.com.hobbyhub.jsf.dto.PostFormDTO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.servlet.http.Part;
import java.io.ByteArrayInputStream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HobbyHubServiceTest {

    private HttpServer servidor;
    private String endereco;
    private final AtomicReference<String> cookieRecebido = new AtomicReference<>();
    private final AtomicReference<String> cookieNoLogin = new AtomicReference<>();
    private final AtomicReference<String> csrfRecebido = new AtomicReference<>();
    private final AtomicReference<String> postagem = new AtomicReference<>();
    private final AtomicReference<String> tipoPostagem = new AtomicReference<>();

    @BeforeEach
    void iniciarApiDeTeste() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        endereco = "http://localhost:" + servidor.getAddress().getPort();
        servidor.createContext("/api/csrf", exchange -> {
            if (exchange.getRequestHeaders().getFirst("Cookie") == null) {
                exchange.getResponseHeaders().add("Set-Cookie", "HOBBYHUB_API=csrf-sessao; Path=/; HttpOnly");
            }
            responder(exchange, 200, "{\"token\":\"token-teste\"}");
        });
        servidor.createContext("/api/login", exchange -> {
            cookieNoLogin.set(exchange.getRequestHeaders().getFirst("Cookie"));
            csrfRecebido.set(exchange.getRequestHeaders().getFirst("X-CSRF-TOKEN"));
            exchange.getResponseHeaders().add("Set-Cookie", "HOBBYHUB_API=sessao-teste; Path=/; HttpOnly");
            responder(exchange, 200, "{\"email\":\"ana@teste.local\"}");
        });
        servidor.createContext("/api/posts", exchange -> {
            cookieRecebido.set(exchange.getRequestHeaders().getFirst("Cookie"));
            if (exchange.getRequestMethod().equals("POST")) {
                csrfRecebido.set(exchange.getRequestHeaders().getFirst("X-CSRF-TOKEN"));
                tipoPostagem.set(exchange.getRequestHeaders().getFirst("Content-Type"));
                postagem.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                responder(exchange, 201, "{}");
            } else {
                responder(exchange, 200, "[]");
            }
        });
        servidor.start();
    }

    @AfterEach
    void pararApiDeTeste() {
        servidor.stop(0);
    }

    @Test
    void publicaJsonComCookieETokenDaApi() {
        var client = novoCliente();
        client.entrar("ana@teste.local", "senha");
        client.postar(formulario(), null);
        assertThat(client.isAutenticado()).isTrue();
        assertThat(cookieNoLogin.get()).contains("HOBBYHUB_API=csrf-sessao");
        assertThat(cookieRecebido.get()).contains("HOBBYHUB_API=sessao-teste");
        assertThat(csrfRecebido.get()).isEqualTo("token-teste");
        assertThat(tipoPostagem.get()).isEqualTo("application/json");
        assertThat(postagem.get()).contains("Meu hobby", "tagGeralId", "tagEspecificaId");
        assertThat(postagem.get()).doesNotContain("senha", "usuarioId");
    }

    @Test
    void sessoesJsfNaoCompartilhamCookieDaApi() {
        var autenticado = novoCliente();
        autenticado.entrar("ana@teste.local", "senha");
        var outroUsuario = novoCliente();
        outroUsuario.listarPosts();
        assertThat(cookieRecebido.get()).isNull();
        assertThat(outroUsuario.isAutenticado()).isFalse();
    }

    @Test
    void encaminhaArquivoMultipartComOsCamposDoPost() {
        var client = novoCliente();
        client.entrar("ana@teste.local", "senha");
        client.postar(formulario(), arquivo("foto.gif", "GIF89a-teste"));
        assertThat(tipoPostagem.get()).startsWith("multipart/form-data; boundary=");
        assertThat(postagem.get()).contains("name=\"conteudo\"", "name=\"tagGeralId\"",
                "name=\"tagEspecificaId\"", "filename=\"foto.gif\"", "GIF89a-teste");
    }

    @Test
    void erroDaApiChegaAoConsumidorSemVirarSucesso() {
        servidor.removeContext("/api/posts");
        servidor.createContext("/api/posts", exchange -> responder(exchange, 400,
                "{\"erro\":\"A tag específica não pertence à tag geral selecionada.\"}"));
        var client = novoCliente();
        assertThatThrownBy(() -> client.postar(formulario(), null))
                .isInstanceOf(ApiException.class)
                .hasMessage("A tag específica não pertence à tag geral selecionada.");
    }

    @Test
    void sessaoExpiradaExigeNovaAutenticacao() {
        servidor.removeContext("/api/posts");
        servidor.createContext("/api/posts", exchange -> responder(exchange, 401,
                "{\"erro\":\"Autenticação necessária.\"}"));
        var client = novoCliente();
        client.entrar("ana@teste.local", "senha");
        assertThatThrownBy(client::listarPosts).isInstanceOf(ApiException.class);
        assertThat(client.isAutenticado()).isFalse();
    }

    private HobbyHubService novoCliente() {
        return new HobbyHubService(endereco);
    }

    private Part arquivo(String nome, String conteudo) {
        byte[] bytes = conteudo.getBytes(StandardCharsets.UTF_8);
        return new Part() {
            @Override public java.io.InputStream getInputStream() { return new ByteArrayInputStream(bytes); }
            @Override public String getContentType() { return "image/gif"; }
            @Override public String getName() { return "arquivo"; }
            @Override public String getSubmittedFileName() { return nome; }
            @Override public long getSize() { return bytes.length; }
            @Override public void write(String fileName) { throw new UnsupportedOperationException(); }
            @Override public void delete() { }
            @Override public String getHeader(String name) { return null; }
            @Override public java.util.Collection<String> getHeaders(String name) { return java.util.List.of(); }
            @Override public java.util.Collection<String> getHeaderNames() { return java.util.List.of(); }
        };
    }

    private PostFormDTO formulario() {
        PostFormDTO form = new PostFormDTO();
        form.setConteudo("Meu hobby");
        form.setTagGeralId(1L);
        form.setTagEspecificaId(4L);
        return form;
    }

    private void responder(HttpExchange exchange, int status, String corpo) throws IOException {
        byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    @Test
    void editaEExcluiComMetodoCookieECsrfCorretos() {
        var metodo = new AtomicReference<String>();
        servidor.createContext("/api/posts/42", exchange -> {
            metodo.set(exchange.getRequestMethod());
            cookieRecebido.set(exchange.getRequestHeaders().getFirst("Cookie"));
            csrfRecebido.set(exchange.getRequestHeaders().getFirst("X-CSRF-TOKEN"));
            postagem.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            if (exchange.getRequestMethod().equals("DELETE")) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
            } else {
                responder(exchange, 200, "{}");
            }
        });
        var client = novoCliente();
        client.entrar("ana@teste.local", "senha");
        var form = new br.com.hobbyhub.jsf.dto.PostEdicaoFormDTO();
        form.setConteudo("Texto editado");
        client.editarPost(42L, form);
        assertThat(metodo.get()).isEqualTo("PATCH");
        assertThat(postagem.get()).isEqualTo("{\"conteudo\":\"Texto editado\"}");
        assertThat(cookieRecebido.get()).contains("HOBBYHUB_API=sessao-teste");
        assertThat(csrfRecebido.get()).isEqualTo("token-teste");
        client.excluirPost(42L);
        assertThat(metodo.get()).isEqualTo("DELETE");
        assertThat(postagem.get()).isEmpty();
        assertThat(cookieRecebido.get()).contains("HOBBYHUB_API=sessao-teste");
        assertThat(csrfRecebido.get()).isEqualTo("token-teste");
    }
}

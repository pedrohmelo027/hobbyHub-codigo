package br.com.hobbyhub.jsf.utils;

import br.com.hobbyhub.jsf.dto.PostEdicaoFormDTO;
import br.com.hobbyhub.jsf.dto.PostFormDTO;
import br.com.hobbyhub.jsf.dto.PostViewDTO;
import br.com.hobbyhub.jsf.dto.TagViewDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.enterprise.context.SessionScoped;
import javax.servlet.http.Part;
import lombok.extern.log4j.Log4j2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@SessionScoped
public class HobbyHubApiClient implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private final String enderecoApi;
    // Cada sessão JSF mantém seus próprios cookies e a própria autenticação na API.
    private final Map<String, String> cookies = new ConcurrentHashMap<>();
    private boolean autenticado;

    public HobbyHubApiClient() {
        this(System.getProperty("hobbyhub.api.url",
                System.getenv().getOrDefault("HOBBYHUB_API_URL", "http://localhost:8081")));
    }

    HobbyHubApiClient(String enderecoApi) {
        this.enderecoApi = enderecoApi.endsWith("/") ? enderecoApi : enderecoApi + "/";
    }

    public boolean isAutenticado() {
        return autenticado;
    }

    public void entrar(String email, String senha) {
        cookies.clear();
        autenticado = false;
        String corpo = "email=" + codificar(email) + "&password=" + codificar(senha);
        String token = csrf();
        enviar(requisicao("api/login")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-CSRF-TOKEN", token)
                .POST(HttpRequest.BodyPublishers.ofString(corpo)));
        autenticado = true;
    }

    public void sair() {
        try {
            if (autenticado) {
                String token = csrf();
                enviar(requisicao("api/logout").header("X-CSRF-TOKEN", token)
                        .POST(HttpRequest.BodyPublishers.noBody()));
            }
        } finally {
            autenticado = false;
            cookies.clear();
        }
    }

    public List<PostViewDTO> listarPosts() {
        return Arrays.asList(ler(enviar(requisicao("api/posts").GET()), PostViewDTO[].class));
    }

    public List<TagViewDTO> listarTags(String tipo) {
        return Arrays.asList(ler(enviar(requisicao("api/tags?tipo=" + codificar(tipo)).GET()), TagViewDTO[].class));
    }

    public void postar(PostFormDTO form, Part arquivo) {
        try {
            String token = csrf();
            HttpRequest.Builder request = requisicao("api/posts").header("X-CSRF-TOKEN", token);
            if (arquivo == null || arquivo.getSize() == 0) {
                enviar(request.header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofByteArray(JSON.writeValueAsBytes(form))));
                return;
            }
            if (arquivo.getSize() > 25L * 1024 * 1024) {
                throw new ApiException("O arquivo deve ter no máximo 25 MB.");
            }
            String boundary = "HobbyHub" + UUID.randomUUID().toString().replace("-", "");
            byte[] corpo = multipart(form, arquivo, boundary);
            enviar(request.header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(corpo)));
        } catch (IOException erro) {
            throw new ApiException("Não foi possível ler o arquivo selecionado.", erro);
        }
    }

    public HttpResponse<byte[]> carregarMidia(String nome, String range) {
        if (!autenticado) throw new ApiException(401, "Entre novamente para visualizar a mídia.");
        if (!nome.matches("[a-fA-F0-9-]{36}[.](jpg|jpeg|png|gif|webp|mp4|webm)")) {
            throw new ApiException(404, "Referência de mídia inválida.");
        }
        HttpRequest.Builder request = requisicao("midias/" + nome).GET();
        if (range != null) request.header("Range", range);
        return enviar(request);
    }

    public void editarPost(Long id, PostEdicaoFormDTO form) {
        try {
            String token = csrf();
            enviar(requisicao("api/posts/" + id)
                    .header("X-CSRF-TOKEN", token)
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofByteArray(JSON.writeValueAsBytes(form))));
        } catch (IOException erro) {
            throw new ApiException("Não foi possível preparar a edição do post.", erro);
        }
    }

    public void excluirPost(Long id) {
        String token = csrf();
        enviar(requisicao("api/posts/" + id)
                .header("X-CSRF-TOKEN", token)
                .DELETE());
    }

    private String csrf() {
        try {
            return JSON.readTree(enviar(requisicao("api/csrf").GET()).body()).path("token").asText();
        } catch (IOException erro) {
            throw new ApiException("A API retornou uma resposta inválida.", erro);
        }
    }

    private HttpRequest.Builder requisicao(String caminho) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(enderecoApi).resolve(caminho))
                .timeout(Duration.ofSeconds(45));
        if (!cookies.isEmpty()) {
            request.header("Cookie", cookies.entrySet().stream()
                    .map(cookie -> cookie.getKey() + "=" + cookie.getValue())
                    .reduce((primeiro, proximo) -> primeiro + "; " + proximo).orElse(""));
        }
        return request;
    }

    private HttpResponse<byte[]> enviar(HttpRequest.Builder request) {
        try {
            HttpResponse<byte[]> response = HTTP.send(request.build(), HttpResponse.BodyHandlers.ofByteArray());
            atualizarCookies(response);
            log.debug("API respondeu HTTP {}.", response.statusCode());
            if (response.statusCode() == 401) autenticado = false;
            if (response.statusCode() >= 400) {
                String mensagem = "A API não conseguiu atender à solicitação (" + response.statusCode() + ").";
                try {
                    mensagem = JSON.readTree(response.body()).path("erro").asText(mensagem);
                } catch (IOException ignored) {
                    // Uma resposta não JSON ainda produz uma mensagem compreensível na tela.
                }
                log.warn("Solicitação à API recusada: HTTP {}.", response.statusCode());
                throw new ApiException(response.statusCode(), mensagem);
            }
            return response;
        } catch (InterruptedException erro) {
            Thread.currentThread().interrupt();
            throw new ApiException("Solicitação interrompida. Confira o feed antes de publicar novamente.", erro);
        } catch (IOException erro) {
            throw new ApiException("Não foi possível confirmar a resposta da API. Verifique a conexão e confira o feed antes de tentar novamente.", erro);
        }
    }

    private void atualizarCookies(HttpResponse<?> response) {
        response.headers().allValues("Set-Cookie").forEach(cookie -> {
            String par = cookie.substring(0, cookie.indexOf(';') >= 0 ? cookie.indexOf(';') : cookie.length());
            int separador = par.indexOf('=');
            if (separador < 1) return;
            String nome = par.substring(0, separador).trim();
            String valor = par.substring(separador + 1).trim();
            if (cookie.toLowerCase().contains("max-age=0")) cookies.remove(nome);
            else cookies.put(nome, valor);
        });
    }

    private <T> T ler(HttpResponse<byte[]> response, Class<T> tipo) {
        try {
            return JSON.readValue(response.body(), tipo);
        } catch (IOException erro) {
            throw new ApiException("A API retornou dados em formato inesperado.", erro);
        }
    }

    private byte[] multipart(PostFormDTO form, Part arquivo, String boundary) throws IOException {
        ByteArrayOutputStream corpo = new ByteArrayOutputStream();
        campo(corpo, boundary, "conteudo", form.getConteudo());
        campo(corpo, boundary, "tagGeralId", String.valueOf(form.getTagGeralId()));
        campo(corpo, boundary, "tagEspecificaId", String.valueOf(form.getTagEspecificaId()));
        String nome = arquivo.getSubmittedFileName().replaceAll("[^a-zA-Z0-9._-]", "_");
        texto(corpo, "--" + boundary + "\r\nContent-Disposition: form-data; name=\"arquivo\"; filename=\"" + nome
                + "\"\r\nContent-Type: application/octet-stream\r\n\r\n");
        try (var entrada = arquivo.getInputStream()) {
            entrada.transferTo(corpo);
        }
        texto(corpo, "\r\n--" + boundary + "--\r\n");
        return corpo.toByteArray();
    }

    private void campo(ByteArrayOutputStream corpo, String boundary, String nome, String valor) throws IOException {
        texto(corpo, "--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + nome
                + "\"\r\n\r\n" + valor + "\r\n");
    }

    private void texto(ByteArrayOutputStream corpo, String valor) throws IOException {
        corpo.write(valor.getBytes(StandardCharsets.UTF_8));
    }

    private String codificar(String valor) {
        return URLEncoder.encode(valor == null ? "" : valor, StandardCharsets.UTF_8);
    }
}

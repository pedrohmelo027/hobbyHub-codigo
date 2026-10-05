package br.com.hobbyhub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import br.com.hobbyhub.model.Post;
import br.com.hobbyhub.model.Role;
import br.com.hobbyhub.model.Tag;
import br.com.hobbyhub.model.TipoDeTag;
import br.com.hobbyhub.model.Usuario;
import br.com.hobbyhub.repository.PostRepository;
import br.com.hobbyhub.repository.TagRepository;
import br.com.hobbyhub.repository.UsuarioRepository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.UUID;

@SpringBootTest(
        properties = {
            "spring.jpa.hibernate.ddl-auto=update",
            "hobbyhub.midias.diretorio=./target/midias-teste"
        })
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostarIntegrationTest {

    // Cada execução recebe um schema exclusivo; nenhum teste usa o banco hobbyhub.
    private static final String BANCO_TESTE = "hobbyhub_test_" + UUID.randomUUID().toString().replace("-", "");
    private static final String SERVIDOR =
            System.getenv().getOrDefault("MYSQL_TEST_SERVER_URL", "jdbc:mariadb://localhost:3306/");

    @DynamicPropertySource
    static void configurarBancoDeTeste(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> SERVIDOR + BANCO_TESTE
                + "?createDatabaseIfNotExist=true&connectionTimeZone=America/Sao_Paulo");
    }

    @AfterAll
    static void removerBancoDeTeste() throws Exception {
        // O nome é gerado por esta classe e nunca vem de uma entrada externa.
        if (!BANCO_TESTE.matches("hobbyhub_test_[a-f0-9]{32}")) {
            throw new IllegalStateException("Nome de banco de teste inválido.");
        }
        String usuario = System.getenv().getOrDefault("MYSQL_USER", "root");
        String senha = System.getenv().getOrDefault("MYSQL_PASSWORD", "");
        try (Connection conexao = DriverManager.getConnection(SERVIDOR, usuario, senha);
                Statement comando = conexao.createStatement()) {
            comando.executeUpdate("DROP DATABASE IF EXISTS " + BANCO_TESTE);
        }
    }

    @Autowired MockMvc mvc;
    @Autowired PostRepository posts;
    @Autowired UsuarioRepository usuarios;
    @Autowired TagRepository tags;
    Long geral, especifica;

    @BeforeEach
    void preparar() {
        posts.deleteAll();
        usuarios.deleteAll();
        // Limpa as tags específicas primeiro para respeitar a chave estrangeira.
        tags.deleteAll(tags.findByTipoOrderByNomeAsc(TipoDeTag.ESPECIFICA));
        tags.deleteAll();
        Usuario u = new Usuario();
        u.setNome("Ana");
        u.setNomeUsuario("ana");
        u.setEmail("ana@teste.local");
        u.setSenha("hash-teste");
        u.setRole(Role.USUARIO);
        usuarios.save(u);
        Tag esporte = tags.save(new Tag("Esporte", TipoDeTag.GERAL));
        geral = esporte.getId();
        especifica = tags.save(Tag.especifica("Futebol", esporte)).getId();
    }

    @Test
    void feedDisponibilizaEditorComTagsSemNavegarParaFormulario() throws Exception {
        mvc.perform(get("/posts").with(user("ana@teste.local")))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/feed"))
                .andExpect(model().attributeExists("postForm", "tagsGerais", "tagsEspecificas"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"novo-post\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"noFeed\" value=\"true\"")));
    }

    @Test
    void erroDePostagemNoFeedMantemEditorAbertoEConteudoInformado() throws Exception {
        mvc.perform(post("/posts")
                        .with(user("ana@teste.local"))
                        .with(csrf())
                        .param("noFeed", "true")
                        .param("conteudo", "Meu hobby favorito")
                        .param("tagGeralId", geral.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/feed"))
                .andExpect(model().attribute("abrirEditor", true))
                .andExpect(model().attributeHasFieldErrors("postForm", "tagEspecificaId"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Meu hobby favorito")));
        assertThat(posts.count()).isZero();
    }

    @Test
    void publicaComAutorDaSessaoEDuasTags() throws Exception {
        mvc.perform(
                        post("/posts")
                                .with(user("ana@teste.local"))
                                .with(csrf())
                                .param("conteudo", "Meu primeiro treino!")
                                .param("tagGeralId", geral.toString())
                                .param("tagEspecificaId", especifica.toString())
                                .param("urlImagem", "")
                                .param("usuarioId", "999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"))
                .andExpect(flash().attribute("sucesso", "Post publicado com sucesso!"));
        assertThat(posts.count()).isEqualTo(1);
        Post p = posts.findAllByOrderByDataCriacaoDescIdDesc().getFirst();
        assertThat(p.getUsuario().getEmail()).isEqualTo("ana@teste.local");
        assertThat(p.getTags()).hasSize(2);
        assertThat(p.getDataCriacao()).isNotNull();
        mvc.perform(get("/posts").with(user("ana@teste.local")))
                .andExpect(status().isOk())
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.containsString(
                                                "Meu primeiro treino!")));
    }

    @Test
    void rejeitaConteudoVazio() throws Exception {
        enviar("   ", geral, especifica, "");
        assertThat(posts.count()).isZero();
    }

    @Test
    void rejeitaTagsInvertidas() throws Exception {
        enviar("Treino", especifica, geral, "");
        assertThat(posts.count()).isZero();
    }

    @Test
    void rejeitaTagsRepetidas() throws Exception {
        enviar("Treino", geral, geral, "");
        assertThat(posts.count()).isZero();
    }

    @Test
    void rejeitaTagInexistente() throws Exception {
        enviar("Treino", 999999L, especifica, "");
        assertThat(posts.count()).isZero();
    }

    @Test
    void rejeitaConteudoMuitoLongo() throws Exception {
        enviar("x".repeat(2001), geral, especifica, "");
        assertThat(posts.count()).isZero();
    }

    @Test
    void visitanteNaoPublica() throws Exception {
        mvc.perform(post("/posts").with(csrf()).param("conteudo", "Teste"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        assertThat(posts.count()).isZero();
    }

    @Test
    void exigeCsrf() throws Exception {
        mvc.perform(post("/posts").with(user("ana@teste.local"))).andExpect(status().isForbidden());
        assertThat(posts.count()).isZero();
    }

    @Test
    void renderizaFormulario() throws Exception {
        mvc.perform(get("/posts/novo").with(user("ana@teste.local")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Futebol")));
    }

    @Test
    void apiPublicaSemExporSenha() throws Exception {
        mvc.perform(
                        post("/api/posts")
                                .with(user("ana@teste.local"))
                                .with(csrf())
                                .contentType("application/json")
                                .content(
                                        "{\"conteudo\":\"Post via API\",\"tagGeralId\":"
                                                + geral
                                                + ",\"tagEspecificaId\":"
                                                + especifica
                                                + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.autor").value("ana"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void swaggerDisponivelAposLogin() throws Exception {
        mvc.perform(get("/v3/api-docs").with(user("ana@teste.local")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/posts']").exists());
    }

    @Test
    void rejeitaEspecificaDeOutraGeral() throws Exception {
        Long arte = tags.save(new Tag("Arte", TipoDeTag.GERAL)).getId();
        enviar("Treino", arte, especifica, "");
        assertThat(posts.count()).isZero();
    }

    @Test
    void armazenaImagemEServeOsMesmosBytes() throws Exception {
        byte[] imagem =
                java.util.Base64.getDecoder()
                        .decode(
                                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aT1cAAAAASUVORK5CYII=");
        MockMultipartFile arquivo =
                new MockMultipartFile("arquivo", "foto.png", "image/png", imagem);

        mvc.perform(
                        multipart("/posts")
                                .file(arquivo)
                                .with(user("ana@teste.local"))
                                .with(csrf())
                                .param("conteudo", "Meu treino")
                                .param("tagGeralId", geral.toString())
                                .param("tagEspecificaId", especifica.toString()))
                .andExpect(redirectedUrl("/posts"));

        Post post = posts.findAllByOrderByDataCriacaoDescIdDesc().getFirst();
        assertThat(post.getUrlImagem()).startsWith("/midias/").endsWith(".png");
        mvc.perform(get(post.getUrlImagem()).with(user("ana@teste.local")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(imagem));
    }

    @Test
    void aceitaVideoEAtendeRequisicaoParcial() throws Exception {
        // Cabeçalho sintético: verifica o fluxo de upload/HTTP, não a decodificação do vídeo.
        byte[] video = new byte[] {0, 0, 0, 24, 'f', 't', 'y', 'p', 'i', 's', 'o', 'm', 0, 0, 0, 0};
        MockMultipartFile arquivo =
                new MockMultipartFile("arquivo", "treino.mp4", "video/mp4", video);

        mvc.perform(
                        multipart("/api/posts")
                                .file(arquivo)
                                .with(user("ana@teste.local"))
                                .with(csrf())
                                .param("conteudo", "Vídeo de treino")
                                .param("tagGeralId", geral.toString())
                                .param("tagEspecificaId", especifica.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.video").value(true));

        Post post = posts.findAllByOrderByDataCriacaoDescIdDesc().getFirst();
        mvc.perform(
                        get(post.getUrlImagem())
                                .header("Range", "bytes=0-7")
                                .with(user("ana@teste.local")))
                .andExpect(status().isPartialContent())
                .andExpect(header().string("Content-Type", "video/mp4"));
        mvc.perform(get("/posts").with(user("ana@teste.local")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<video")));
    }

    @Test
    void rejeitaArquivoDisfarcadoDeImagem() throws Exception {
        MockMultipartFile arquivo =
                new MockMultipartFile(
                        "arquivo", "foto.jpg", "image/jpeg", "arquivo inválido".getBytes());
        mvc.perform(
                        multipart("/posts")
                                .file(arquivo)
                                .with(user("ana@teste.local"))
                                .with(csrf())
                                .param("conteudo", "Treino")
                                .param("tagGeralId", geral.toString())
                                .param("tagEspecificaId", especifica.toString()))
                .andExpect(view().name("posts/formulario"))
                .andExpect(model().attributeHasErrors("postForm"));
        assertThat(posts.count()).isZero();
    }

    @Test
    void removeArquivoQuandoBancoFazRollback() {
        org.springframework.transaction.support.TransactionTemplate transacao =
                new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        String url =
                transacao.execute(
                        status -> {
                            br.com.hobbyhub.dto.PostForm form = new br.com.hobbyhub.dto.PostForm();
                            form.setConteudo("Post cancelado");
                            form.setTagGeralId(geral);
                            form.setTagEspecificaId(especifica);
                            byte[] gif = "GIF89a-test".getBytes();
                            String midia =
                                    postService
                                            .postar(
                                                    form,
                                                    "ana@teste.local",
                                                    new MockMultipartFile(
                                                            "arquivo",
                                                            "foto.gif",
                                                            "image/gif",
                                                            gif))
                                            .urlImagem();
                            status.setRollbackOnly();
                            return midia;
                        });
        assertThat(posts.count()).isZero();
        assertThat(Path.of("target/midias-teste", url.substring("/midias/".length())))
                .doesNotExist();
    }

    @Test
    void naoPermiteAcessoAArquivoForaDoDiretorio() throws Exception {
        mvc.perform(get("/midias/pom.xml").with(user("ana@teste.local")))
                .andExpect(status().isNotFound());
    }

    @Autowired br.com.hobbyhub.service.PostService postService;
    @Autowired org.springframework.transaction.PlatformTransactionManager transactionManager;

    private void enviar(String conteudo, Long g, Long e, String imagem) throws Exception {
        mvc.perform(
                        post("/posts")
                                .with(user("ana@teste.local"))
                                .with(csrf())
                                .param("conteudo", conteudo)
                                .param("tagGeralId", g.toString())
                                .param("tagEspecificaId", e.toString())
                                .param("urlImagem", imagem))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/formulario"))
                .andExpect(model().attributeHasErrors("postForm"));
    }
}

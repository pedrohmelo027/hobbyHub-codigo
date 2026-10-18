package br.com.hobbyhub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        u.setSenha(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("SenhaTeste123!"));
        u.setRole(Role.USUARIO);
        usuarios.save(u);
        Tag esporte = tags.save(new Tag("Esporte", TipoDeTag.GERAL));
        geral = esporte.getId();
        especifica = tags.save(Tag.especifica("Futebol", esporte)).getId();
    }

    @Test
    void publicaComAutorDaSessaoEDuasTags() throws Exception {
        mvc.perform(multipart("/api/posts")
                        .with(user("ana@teste.local")).with(csrf())
                        .param("conteudo", "Meu primeiro treino!")
                        .param("tagGeralId", geral.toString())
                        .param("tagEspecificaId", especifica.toString())
                        .param("usuarioId", "999"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.autor").value("ana"));
        Post post = posts.findAllByOrderByDataCriacaoDescIdDesc().getFirst();
        assertThat(post.getUsuario().getEmail()).isEqualTo("ana@teste.local");
        assertThat(post.getTags()).hasSize(2);
        assertThat(post.getDataCriacao()).isNotNull();
        mvc.perform(get("/api/posts").with(user("ana@teste.local")))
                .andExpect(jsonPath("$[0].conteudo").value("Meu primeiro treino!"));
    }

    @Test
    void rejeitaConteudoVazio() throws Exception {
        rejeitar("   ", geral, especifica);
    }

    @Test
    void rejeitaTagsInvertidas() throws Exception {
        rejeitar("Treino", especifica, geral);
    }

    @Test
    void rejeitaTagsRepetidas() throws Exception {
        rejeitar("Treino", geral, geral);
    }

    @Test
    void rejeitaTagInexistente() throws Exception {
        rejeitar("Treino", 999999L, especifica);
    }

    @Test
    void rejeitaConteudoMuitoLongo() throws Exception {
        rejeitar("x".repeat(2001), geral, especifica);
    }

    @Test
    void rejeitaEspecificaDeOutraGeral() throws Exception {
        Long arte = tags.save(new Tag("Arte", TipoDeTag.GERAL)).getId();
        rejeitar("Treino", arte, especifica);
    }

    @Test
    void visitanteRecebe401SemRedirecionamentoHtml() throws Exception {
        mvc.perform(get("/api/posts"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    void exigeCsrf() throws Exception {
        mvc.perform(post("/api/posts").with(user("ana@teste.local")))
                .andExpect(status().isForbidden());
        assertThat(posts.count()).isZero();
    }

    @Test
    void loginRetornaJsonEPermiteSessaoAutenticada() throws Exception {
        var resultado = mvc.perform(post("/api/login").with(csrf())
                        .param("email", "ana@teste.local").param("password", "SenhaTeste123!"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@teste.local"))
                .andReturn();
        var sessao = (org.springframework.mock.web.MockHttpSession) resultado.getRequest().getSession();
        mvc.perform(get("/api/posts").session(sessao)).andExpect(status().isOk());
        mvc.perform(post("/api/logout").session(sessao).with(csrf()))
                .andExpect(status().isNoContent());
        assertThat(sessao.isInvalid()).isTrue();
    }

    @Test
    void loginInvalidoRetorna401() throws Exception {
        mvc.perform(post("/api/login").with(csrf())
                        .param("email", "ana@teste.local").param("password", "errada"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    void apiPublicaJsonSemExporSenha() throws Exception {
        mvc.perform(post("/api/posts").with(user("ana@teste.local")).with(csrf())
                        .contentType("application/json")
                        .content("{\"conteudo\":\"Post via API\",\"tagGeralId\":" + geral
                                + ",\"tagEspecificaId\":" + especifica + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.autor").value("ana"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void consultaTagsComHierarquiaSemExporEntidade() throws Exception {
        mvc.perform(get("/api/tags?tipo=ESPECIFICA").with(user("ana@teste.local")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Futebol"))
                .andExpect(jsonPath("$[0].tagGeralId").value(geral))
                .andExpect(jsonPath("$[0].tagGeral").doesNotExist());
    }

    @Test
    void swaggerDocumentaPostsETags() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/posts']").exists())
                .andExpect(jsonPath("$.paths['/api/tags']").exists());
    }

    @Test
    void armazenaImagemEServeOsMesmosBytes() throws Exception {
        byte[] imagem = java.util.Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aT1cAAAAASUVORK5CYII=");
        publicarArquivo(new MockMultipartFile("arquivo", "foto.png", "image/png", imagem))
                .andExpect(status().isCreated());
        Post post = posts.findAllByOrderByDataCriacaoDescIdDesc().getFirst();
        assertThat(post.getUrlImagem()).startsWith("/midias/").endsWith(".png");
        mvc.perform(get(post.getUrlImagem()).with(user("ana@teste.local")))
                .andExpect(status().isOk()).andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(imagem));
    }

    @Test
    void aceitaVideoEAtendeRequisicaoParcial() throws Exception {
        byte[] video = {0, 0, 0, 24, 'f', 't', 'y', 'p', 'i', 's', 'o', 'm', 0, 0, 0, 0};
        publicarArquivo(new MockMultipartFile("arquivo", "treino.mp4", "video/mp4", video))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.video").value(true));
        Post post = posts.findAllByOrderByDataCriacaoDescIdDesc().getFirst();
        mvc.perform(get(post.getUrlImagem()).header("Range", "bytes=0-7")
                        .with(user("ana@teste.local")))
                .andExpect(status().isPartialContent())
                .andExpect(header().string("Content-Type", "video/mp4"));
    }

    @Test
    void rejeitaArquivoDisfarcadoDeImagem() throws Exception {
        publicarArquivo(new MockMultipartFile("arquivo", "foto.jpg", "image/jpeg", "invalido".getBytes()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").exists());
        assertThat(posts.count()).isZero();
    }

    @Test
    void removeArquivoQuandoBancoFazRollback() {
        org.springframework.transaction.support.TransactionTemplate transacao =
                new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        String url =
                transacao.execute(
                        status -> {
                            br.com.hobbyhub.dto.PostFormDTO form = new br.com.hobbyhub.dto.PostFormDTO();
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

    private org.springframework.test.web.servlet.ResultActions publicarArquivo(MockMultipartFile arquivo) throws Exception {
        return mvc.perform(multipart("/api/posts").file(arquivo)
                .with(user("ana@teste.local")).with(csrf())
                .param("conteudo", "Meu treino").param("tagGeralId", geral.toString())
                .param("tagEspecificaId", especifica.toString()));
    }

    private void rejeitar(String conteudo, Long geralId, Long especificaId) throws Exception {
        mvc.perform(multipart("/api/posts").with(user("ana@teste.local")).with(csrf())
                        .param("conteudo", conteudo).param("tagGeralId", geralId.toString())
                        .param("tagEspecificaId", especificaId.toString()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").exists());
        assertThat(posts.count()).isZero();
    }

    @Test
    void editaSomenteTextoDoProprioPost() throws Exception {
        Long id = criarPostComMidia();
        Post original = posts.findAllByOrderByDataCriacaoDescIdDesc().getFirst();
        var dataOriginal = original.getDataCriacao();
        var urlOriginal = original.getUrlImagem();

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/posts/{id}", id)
                        .with(user("ana@teste.local")).with(csrf())
                        .contentType("application/json")
                        .content("{\"conteudo\":\" Texto atualizado \" ,\"tagGeralId\":999,\"urlImagem\":\"/midias/alterada.png\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo").value("Texto atualizado"))
                .andExpect(jsonPath("$.proprio").value(true));

        Post atualizado = posts.findAllByOrderByDataCriacaoDescIdDesc().getFirst();
        assertThat(atualizado.getDataCriacao()).isEqualTo(dataOriginal);
        assertThat(atualizado.getUrlImagem()).isEqualTo(urlOriginal);
        assertThat(atualizado.getTags()).extracting(Tag::getId).containsExactlyInAnyOrder(geral, especifica);
    }

    @Test
    void naoPermiteEditarNemExcluirPostDeOutroAutor() throws Exception {
        Long id = criarPostComMidia();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/posts/{id}", id)
                        .with(user("outro@teste.local")).with(csrf())
                        .contentType("application/json").content("{\"conteudo\":\"Tentativa\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/posts/{id}", id)
                        .with(user("outro@teste.local")).with(csrf()))
                .andExpect(status().isForbidden());
        assertThat(posts.existsById(id)).isTrue();
        assertThat(posts.findAllByOrderByDataCriacaoDescIdDesc().getFirst().getConteudo()).isEqualTo("Post com mídia");
        mvc.perform(get("/api/posts").with(user("outro@teste.local")))
                .andExpect(jsonPath("$[0].proprio").value(false));
    }

    @Test
    void excluirRemovePostVinculosEMidia() throws Exception {
        Long id = criarPostComMidia();
        String url = posts.findAllByOrderByDataCriacaoDescIdDesc().getFirst().getUrlImagem();
        Path arquivo = Path.of("target/midias-teste", url.substring("/midias/".length()));
        assertThat(arquivo).exists();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/posts/{id}", id)
                        .with(user("ana@teste.local")).with(csrf()))
                .andExpect(status().isNoContent());
        assertThat(posts.existsById(id)).isFalse();
        assertThat(arquivo).doesNotExist();
        assertThat(tags.existsById(geral)).isTrue();
        assertThat(usuarios.findByEmail("ana@teste.local")).isPresent();
    }

    @Test
    void edicaoRejeitaTextoVazioEExcessoDeCaracteres() throws Exception {
        Long id = criarPostComMidia();
        for (String texto : new String[]{"   ", "x".repeat(2001)}) {
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/posts/{id}", id)
                            .with(user("ana@teste.local")).with(csrf())
                            .contentType("application/json").content("{\"conteudo\":\"" + texto + "\"}"))
                    .andExpect(status().isBadRequest());
        }
        assertThat(posts.findAllByOrderByDataCriacaoDescIdDesc().getFirst().getConteudo()).isEqualTo("Post com mídia");
    }

    @Test
    void alteracoesExigemCsrfEPostExistente() throws Exception {
        Long id = criarPostComMidia();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/posts/{id}", id)
                        .with(user("ana@teste.local")))
                .andExpect(status().isForbidden());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/posts/{id}", id)
                        .with(user("ana@teste.local"))
                        .contentType("application/json").content("{\"conteudo\":\"Alteração\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/posts/{id}", Long.MAX_VALUE)
                        .with(user("ana@teste.local")).with(csrf()))
                .andExpect(status().isNotFound());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/posts/{id}", Long.MAX_VALUE)
                        .with(user("ana@teste.local")).with(csrf())
                        .contentType("application/json").content("{\"conteudo\":\"Alteração\"}"))
                .andExpect(status().isNotFound());
    }

    private Long criarPostComMidia() throws Exception {
        byte[] png = java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aT1cAAAAASUVORK5CYII=");
        mvc.perform(multipart("/api/posts")
                        .file(new MockMultipartFile("arquivo", "teste.png", "image/png", png))
                        .with(user("ana@teste.local")).with(csrf())
                        .param("conteudo", "Post com mídia")
                        .param("tagGeralId", geral.toString())
                        .param("tagEspecificaId", especifica.toString()))
                .andExpect(status().isCreated());
        return posts.findAllByOrderByDataCriacaoDescIdDesc().getFirst().getId();
    }
}

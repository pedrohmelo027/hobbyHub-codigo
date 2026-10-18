package br.com.hobbyhub.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import br.com.hobbyhub.dto.PostFormDTO;
import br.com.hobbyhub.dto.PostEdicaoFormDTO;
import java.util.NoSuchElementException;
import org.springframework.security.access.AccessDeniedException;
import br.com.hobbyhub.dto.PostViewDTO;
import br.com.hobbyhub.dto.TagViewDTO;
import br.com.hobbyhub.model.Post;
import br.com.hobbyhub.model.Tag;
import br.com.hobbyhub.model.TipoDeTag;
import br.com.hobbyhub.model.Usuario;
import br.com.hobbyhub.repository.PostRepository;
import br.com.hobbyhub.repository.TagRepository;
import br.com.hobbyhub.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UsuarioRepository usuarioRepository;
    private final TagRepository tagRepository;
    private final MidiaService midiaService;

    @Transactional
    public PostViewDTO postar(PostFormDTO form, String email, MultipartFile arquivo) {
        Usuario autor =
                usuarioRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () ->
                                        new RegraNegocioException(
                                                "Usuário autenticado não encontrado."));

        Tag geral = validarTag(form.getTagGeralId(), TipoDeTag.GERAL);
        Tag especifica = validarTag(form.getTagEspecificaId(), TipoDeTag.ESPECIFICA);

        if (!especifica.pertenceA(geral)) {
            throw new RegraNegocioException(
                    "A tag específica não pertence à tag geral selecionada.");
        }

        String urlMidia = midiaService.salvar(arquivo);
        removerMidiaSeTransacaoFalhar(urlMidia);

        Post post = new Post();
        post.setUsuario(autor);
        post.setConteudo(form.getConteudo().strip());
        post.setUrlImagem(urlMidia);
        post.setTags(Set.of(geral, especifica));

        Post salvo = postRepository.saveAndFlush(post);
        log.debug("Post {} gravado na transação.", salvo.getId());
        return PostViewDTO.from(salvo, email);
    }

    @Transactional(readOnly = true)
    public List<PostViewDTO> listar(String email) {
        return postRepository.findAllByOrderByDataCriacaoDescIdDesc().stream()
                .map(post -> PostViewDTO.from(post, email))
                .toList();
    }

    @Transactional
    public PostViewDTO editar(Long id, PostEdicaoFormDTO form, String email) {
        Post post = buscarPostDoAutor(id, email);
        post.setConteudo(form.getConteudo().strip());
        postRepository.flush();
        return PostViewDTO.from(post, email);
    }

    @Transactional
    public void excluir(Long id, String email) {
        Post post = buscarPostDoAutor(id, email);
        String urlMidia = post.getUrlImagem();
        postRepository.delete(post);
        postRepository.flush();

        // A mídia só é removida depois de a exclusão no banco ser confirmada.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                midiaService.excluir(urlMidia);
            }
        });
    }

    private Post buscarPostDoAutor(Long id, String email) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Post não encontrado."));

        if (!post.getUsuario().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("Você só pode alterar ou apagar seus próprios posts.");
        }
        return post;
    }

    @Transactional(readOnly = true)
    public List<TagViewDTO> listarTags(TipoDeTag tipo) {
        return tagRepository.findByTipoOrderByNomeAsc(tipo).stream()
                .map(TagViewDTO::from)
                .toList();
    }

    private Tag validarTag(Long id, TipoDeTag tipo) {
        if (id == null) {
            throw new RegraNegocioException("Selecione as duas tags.");
        }

        Tag tag =
                tagRepository
                        .findById(id)
                        .orElseThrow(() -> new RegraNegocioException("Tag não encontrada."));

        if (tag.getTipo() != tipo) {
            throw new RegraNegocioException(
                    "Selecione uma tag geral e uma específica nos campos corretos.");
        }
        return tag;
    }

    private void removerMidiaSeTransacaoFalhar(String urlMidia) {
        if (urlMidia == null) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status != STATUS_COMMITTED) {
                            midiaService.excluir(urlMidia);
                        }
                    }
                });
    }
}

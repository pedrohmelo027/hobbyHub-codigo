package br.com.hobbyhub.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import br.com.hobbyhub.dto.PostForm;
import br.com.hobbyhub.dto.PostView;
import br.com.hobbyhub.model.Post;
import br.com.hobbyhub.model.Tag;
import br.com.hobbyhub.model.TipoDeTag;
import br.com.hobbyhub.model.Usuario;
import br.com.hobbyhub.repository.PostRepository;
import br.com.hobbyhub.repository.TagRepository;
import br.com.hobbyhub.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UsuarioRepository usuarioRepository;
    private final TagRepository tagRepository;
    private final ArmazenamentoMidiaService armazenamentoMidiaService;

    @Transactional
    public PostView postar(PostForm form, String email, MultipartFile arquivo) {
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

        String urlMidia = armazenamentoMidiaService.salvar(arquivo);
        removerMidiaSeTransacaoFalhar(urlMidia);

        Post post = new Post();
        post.setUsuario(autor);
        post.setConteudo(form.getConteudo().strip());
        post.setUrlImagem(urlMidia);
        post.setTags(Set.of(geral, especifica));

        return PostView.from(postRepository.saveAndFlush(post));
    }

    @Transactional(readOnly = true)
    public List<PostView> listar() {
        return postRepository.findAllByOrderByDataCriacaoDescIdDesc().stream()
                .map(PostView::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Tag> listarTags(TipoDeTag tipo) {
        return tagRepository.findByTipoOrderByNomeAsc(tipo);
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
                            armazenamentoMidiaService.excluir(urlMidia);
                        }
                    }
                });
    }
}

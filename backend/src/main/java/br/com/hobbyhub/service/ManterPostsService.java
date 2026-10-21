package br.com.hobbyhub.service;

import br.com.hobbyhub.dto.ManterPostsFormDTO;
import br.com.hobbyhub.dto.PostViewDTO;
import br.com.hobbyhub.model.Post;
import br.com.hobbyhub.repository.PostRepository;
import br.com.hobbyhub.utils.MidiaService;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class ManterPostsService {

    private final PostRepository postRepository;
    private final MidiaService midiaService;

    @Transactional
    public PostViewDTO editar(Long id, ManterPostsFormDTO form, String email) {
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
}

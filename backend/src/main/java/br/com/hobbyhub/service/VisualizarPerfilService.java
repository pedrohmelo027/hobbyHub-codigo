package br.com.hobbyhub.service;

import br.com.hobbyhub.dto.PostViewDTO;
import br.com.hobbyhub.dto.VisualizarPerfilDTO;
import br.com.hobbyhub.model.Usuario;
import br.com.hobbyhub.repository.PostRepository;
import br.com.hobbyhub.repository.UsuarioRepository;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VisualizarPerfilService {

    private final UsuarioRepository usuarioRepository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public VisualizarPerfilDTO visualizar(String nomeUsuario, String emailAutenticado) {
        Usuario usuario = usuarioRepository.findByNomeUsuarioIgnoreCase(nomeUsuario)
                .orElseThrow(() -> new NoSuchElementException("Perfil não encontrado."));
        var publicacoes = postRepository.findByUsuario_NomeUsuarioIgnoreCaseOrderByDataCriacaoDescIdDesc(
                usuario.getNomeUsuario());
        var posts = publicacoes.stream()
                .map(post -> PostViewDTO.from(post, emailAutenticado))
                .toList();
        int diasSeguidos = SequenciaPublicacao.calcular(
                publicacoes.stream().map(post -> post.getDataCriacao()).toList(), LocalDate.now());
        boolean meuPerfil = usuario.getEmail().equalsIgnoreCase(emailAutenticado);
        return VisualizarPerfilDTO.from(usuario, posts, diasSeguidos, meuPerfil);
    }
}

package br.com.hobbyhub.service;

import br.com.hobbyhub.dto.EditarPerfilDTO;
import br.com.hobbyhub.dto.EditarPerfilFormDTO;
import br.com.hobbyhub.model.Tag;
import br.com.hobbyhub.model.Usuario;
import br.com.hobbyhub.repository.TagRepository;
import br.com.hobbyhub.repository.UsuarioRepository;
import br.com.hobbyhub.utils.RegraNegocioException;
import br.com.hobbyhub.utils.MidiaService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class EditarPerfilService {

    private final UsuarioRepository usuarioRepository;
    private final TagRepository tagRepository;
    private final MidiaService midiaService;

    @Transactional(readOnly = true)
    public EditarPerfilDTO carregar(String email) {
        return EditarPerfilDTO.from(buscarUsuario(email));
    }

    @Transactional
    public EditarPerfilDTO editar(String email, EditarPerfilFormDTO form) {
        Usuario usuario = buscarUsuario(email);
        usuario.setNome(form.getNome().strip());
        usuario.setBiografia(form.getBiografia() == null ? null : form.getBiografia().strip());
        usuario.setTagsSeguidas(buscarTags(form.getTagsInteresseIds()));
        return EditarPerfilDTO.from(usuarioRepository.save(usuario));
    }

    @Transactional
    public EditarPerfilDTO atualizarFoto(String email, MultipartFile foto) {
        Usuario usuario = buscarUsuario(email);
        String novaFoto = midiaService.salvarFotoPerfil(foto);
        String fotoAnterior = usuario.getUrlFotoPerfil();
        try {
            usuario.setUrlFotoPerfil(novaFoto);
            EditarPerfilDTO atualizado = EditarPerfilDTO.from(usuarioRepository.save(usuario));
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_COMMITTED) {
                        midiaService.excluir(fotoAnterior);
                    } else {
                        midiaService.excluir(novaFoto);
                    }
                }
            });
            return atualizado;
        } catch (RuntimeException erro) {
            midiaService.excluir(novaFoto);
            throw erro;
        }
    }

    private Set<Tag> buscarTags(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        Set<Long> unicos = new HashSet<>(ids);
        if (unicos.contains(null)) throw new RegraNegocioException("Uma das tags selecionadas não existe.");
        List<Tag> tags = tagRepository.findAllById(unicos);
        if (tags.size() != unicos.size()) throw new RegraNegocioException("Uma das tags selecionadas não existe.");
        return new HashSet<>(tags);
    }

    private Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RegraNegocioException("Usuário autenticado não encontrado."));
    }
}

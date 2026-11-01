package br.com.hobbyhub.service;

import br.com.hobbyhub.dto.CadastrarUsuarioFormDTO;
import br.com.hobbyhub.model.Role;
import br.com.hobbyhub.model.Tag;
import br.com.hobbyhub.model.Usuario;
import br.com.hobbyhub.repository.TagRepository;
import br.com.hobbyhub.repository.UsuarioRepository;
import br.com.hobbyhub.utils.MidiaService;
import br.com.hobbyhub.utils.RegraNegocioException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CadastrarUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TagRepository tagRepository;
    private final PasswordEncoder passwordEncoder;
    private final MidiaService midiaService;

    @Transactional
    public void cadastrar(CadastrarUsuarioFormDTO form) {
        cadastrar(form, null);
    }

    @Transactional
    public void cadastrar(CadastrarUsuarioFormDTO form, MultipartFile foto) {
        String email = form.getEmail().strip().toLowerCase(Locale.ROOT);
        String nomeUsuario = form.getNomeUsuario().strip();

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new RegraNegocioException("E-mail já cadastrado.");
        }
        if (usuarioRepository.existsByNomeUsuarioIgnoreCase(nomeUsuario)) {
            throw new RegraNegocioException("Nome de usuário já cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(form.getNome().strip());
        usuario.setEmail(email);
        usuario.setNomeUsuario(nomeUsuario);
        usuario.setSenha(passwordEncoder.encode(form.getSenha()));
        usuario.setBiografia(form.getBiografia() == null ? null : form.getBiografia().strip());
        usuario.setRole(Role.USUARIO);
        if (foto != null && !foto.isEmpty()) {
            String urlFoto = midiaService.salvarFotoPerfil(foto);
            usuario.setUrlFotoPerfil(urlFoto);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) {
                        midiaService.excluir(urlFoto);
                    }
                }
            });
        }
        usuario.setTagsSeguidas(buscarTags(form.getTagsInteresseIds()));
        usuarioRepository.saveAndFlush(usuario);
    }

    private Set<Tag> buscarTags(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        Set<Long> unicos = new HashSet<>(ids);
        if (unicos.contains(null)) throw new RegraNegocioException("Uma das tags selecionadas não existe.");
        List<Tag> tags = tagRepository.findAllById(unicos);
        if (tags.size() != unicos.size()) throw new RegraNegocioException("Uma das tags selecionadas não existe.");
        return new HashSet<>(tags);
    }
}

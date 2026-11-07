package br.com.hobbyhub.service;

import br.com.hobbyhub.dto.ManterTagDTO;
import br.com.hobbyhub.dto.ManterTagFormDTO;
import br.com.hobbyhub.model.Role;
import br.com.hobbyhub.model.Tag;
import br.com.hobbyhub.model.TipoDeTag;
import br.com.hobbyhub.repository.PostRepository;
import br.com.hobbyhub.repository.TagRepository;
import br.com.hobbyhub.repository.UsuarioRepository;
import br.com.hobbyhub.utils.RegraNegocioException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManterTagsService {

    private final TagRepository tagRepository;
    private final PostRepository postRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<ManterTagDTO> listar(String email) {
        exigirAdministrador(email);
        List<ManterTagDTO> tags = new ArrayList<>();
        tags.addAll(tagRepository.findByTipoOrderByNomeAsc(TipoDeTag.GERAL).stream()
                .map(ManterTagDTO::from).toList());
        tags.addAll(tagRepository.findByTipoOrderByNomeAsc(TipoDeTag.ESPECIFICA).stream()
                .map(ManterTagDTO::from).toList());
        return tags.stream()
                .sorted(Comparator.comparing(ManterTagDTO::nome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional
    public ManterTagDTO criar(ManterTagFormDTO form, String email) {
        exigirAdministrador(email);
        Tag tag = new Tag();
        atualizar(tag, form);
        return ManterTagDTO.from(tagRepository.saveAndFlush(tag));
    }

    @Transactional
    public ManterTagDTO editar(Long id, ManterTagFormDTO form, String email) {
        exigirAdministrador(email);
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Tag não encontrada."));
        validarAlteracaoEstrutural(tag, form);
        atualizar(tag, form);
        return ManterTagDTO.from(tagRepository.saveAndFlush(tag));
    }

    @Transactional
    public void excluir(Long id, String email) {
        exigirAdministrador(email);
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Tag não encontrada."));
        if (postRepository.existsByTags_Id(id)
                || usuarioRepository.existsByTagsSeguidas_Id(id)
                || tagRepository.existsByTagGeral_Id(id)) {
            throw new RegraNegocioException("A tag está em uso e não pode ser removida.");
        }
        tagRepository.delete(tag);
    }

    private void validarAlteracaoEstrutural(Tag tag, ManterTagFormDTO form) {
        if (form.getTipo() != null && form.getTipo() != tag.getTipo()) {
            if (postRepository.existsByTags_Id(tag.getId())) {
                throw new RegraNegocioException("Não é possível mudar o tipo de uma tag usada em posts.");
            }
            if (tag.getTipo() == TipoDeTag.GERAL && tagRepository.existsByTagGeral_Id(tag.getId())) {
                throw new RegraNegocioException("Remova ou mova as tags específicas antes de mudar o tipo.");
            }
        }
        if (tag.getTipo() == TipoDeTag.ESPECIFICA
                && form.getTipo() == TipoDeTag.ESPECIFICA
                && tag.getTagGeral() != null
                && !tag.getTagGeral().getId().equals(form.getTagGeralId())
                && postRepository.existsByTags_Id(tag.getId())) {
            throw new RegraNegocioException("Não é possível mover uma tag específica usada em posts.");
        }
    }

    private void atualizar(Tag tag, ManterTagFormDTO form) {
        if (form.getTipo() == null) {
            throw new RegraNegocioException("Selecione o tipo da tag.");
        }
        String nome = form.getNome().strip();
        boolean duplicada = tagRepository.findByTipoOrderByNomeAsc(form.getTipo()).stream()
                .anyMatch(outra -> !outra.getId().equals(tag.getId())
                        && outra.getNome().toLowerCase(Locale.ROOT).equals(nome.toLowerCase(Locale.ROOT)));
        if (duplicada) {
            throw new RegraNegocioException("Já existe uma tag desse tipo com esse nome.");
        }

        tag.setNome(nome);
        tag.setTipo(form.getTipo());
        if (form.getTipo() == TipoDeTag.GERAL) {
            tag.setTagGeral(null);
            return;
        }
        Tag geral = tagRepository.findById(form.getTagGeralId() == null ? -1L : form.getTagGeralId())
                .orElseThrow(() -> new RegraNegocioException("Selecione uma tag geral válida."));
        if (geral.getTipo() != TipoDeTag.GERAL || geral.getId().equals(tag.getId())) {
            throw new RegraNegocioException("A tag específica deve pertencer a uma tag geral.");
        }
        tag.setTagGeral(geral);
    }

    private void exigirAdministrador(String email) {
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RegraNegocioException("Usuário autenticado não encontrado."));
        if (usuario.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Somente administradores podem manter tags.");
        }
    }
}

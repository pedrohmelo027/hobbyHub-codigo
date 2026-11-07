package br.com.hobbyhub.jsf.bean;

import br.com.hobbyhub.jsf.dto.PostEdicaoFormDTO;
import br.com.hobbyhub.jsf.dto.PostViewDTO;
import br.com.hobbyhub.jsf.dto.EditarPerfilFormDTO;
import br.com.hobbyhub.jsf.dto.TagViewDTO;
import br.com.hobbyhub.jsf.utils.ApiException;
import br.com.hobbyhub.jsf.utils.FiltroPostsPorTag;
import br.com.hobbyhub.jsf.utils.HobbyHubApiClient;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.Getter;
import lombok.Setter;

@Named("visualizarPostsBean")
@ViewScoped
@Getter
public class VisualizarPostsBean implements Serializable {

    @Inject
    private HobbyHubApiClient api;

    private Long postEmEdicaoId;
    private PostEdicaoFormDTO edicao = new PostEdicaoFormDTO();
    private List<PostViewDTO> posts = List.of();
    private List<TagViewDTO> interesses = List.of();
    @Setter
    private Long tagId;
    @Setter
    private String pesquisaTag;

    public void carregar() {
        carregarInteresses();
        carregarPosts();
    }

    public TagViewDTO getTagAtiva() {
        if (tagId == null) return null;
        return interesses.stream().filter(tag -> tagId.equals(tag.getId())).findFirst().orElse(null);
    }

    public String getMensagemVazia() {
        if (pesquisaTag == null || pesquisaTag.isBlank()) {
            if (tagId != null) {
                TagViewDTO tag = getTagAtiva();
                return tag == null ? "Nenhum post encontrado para esta tag."
                        : "Nenhum post encontrado com a tag “" + tag.getNome() + "”.";
            }
            return "O primeiro post pode ser seu. Use o botão + para compartilhar um hobby.";
        }
        return "Nenhum post encontrado com a tag “" + pesquisaTag.strip() + "”.";
    }

    private void carregarPosts() {
        try {
            List<PostViewDTO> resultado = tagId == null ? api.listarPosts() : api.listarPosts(tagId);
            posts = FiltroPostsPorTag.filtrar(resultado, pesquisaTag);
        } catch (ApiException erro) {
            mensagem(FacesMessage.SEVERITY_ERROR, erro.getMessage());
        }
    }

    private void carregarInteresses() {
        try {
            EditarPerfilFormDTO perfil = api.carregarPerfil();
            List<Long> ids = perfil.getTagsInteresseIds();
            Set<Long> selecionados = ids == null ? Set.of() : new HashSet<>(ids);
            interesses = api.listarTagsDisponiveis().stream()
                    .filter(tag -> selecionados.contains(tag.getId()))
                    .toList();
        } catch (ApiException erro) {
            mensagem(FacesMessage.SEVERITY_ERROR, erro.getMessage());
        }
    }

    public void iniciarEdicao(PostViewDTO post) {
        if (post.isProprio()) {
            postEmEdicaoId = post.getId();
            edicao = new PostEdicaoFormDTO();
            edicao.setConteudo(post.getConteudo());
        }
    }

    public void cancelarEdicao() {
        postEmEdicaoId = null;
        edicao = new PostEdicaoFormDTO();
    }

    public String salvarEdicao() {
        try {
            api.editarPost(postEmEdicaoId, edicao);
            return concluir("Texto atualizado com sucesso!");
        } catch (ApiException erro) {
            mensagem(FacesMessage.SEVERITY_ERROR, erro.getMessage());
            return null;
        }
    }

    public String excluir(PostViewDTO post) {
        try {
            api.excluirPost(post.getId());
            return concluir("Post excluído com sucesso!");
        } catch (ApiException erro) {
            mensagem(FacesMessage.SEVERITY_ERROR, erro.getMessage());
            return null;
        }
    }

    private String concluir(String texto) {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        mensagem(FacesMessage.SEVERITY_INFO, texto);
        return "/visualizar-posts/feed.xhtml?faces-redirect=true";
    }

    private void mensagem(FacesMessage.Severity nivel, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(nivel, texto, null));
    }
}

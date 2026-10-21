package br.com.hobbyhub.jsf.bean;

import br.com.hobbyhub.jsf.dto.PostEdicaoFormDTO;
import br.com.hobbyhub.jsf.dto.PostViewDTO;
import br.com.hobbyhub.jsf.utils.ApiException;
import br.com.hobbyhub.jsf.utils.HobbyHubApiClient;
import java.io.Serializable;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.Getter;

@Named("visualizarPostsBean")
@ViewScoped
@Getter
public class VisualizarPostsBean implements Serializable {

    @Inject
    private HobbyHubApiClient api;

    private Long postEmEdicaoId;
    private PostEdicaoFormDTO edicao = new PostEdicaoFormDTO();
    private List<PostViewDTO> posts = List.of();

    public void carregar() {
        try {
            posts = api.listarPosts();
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

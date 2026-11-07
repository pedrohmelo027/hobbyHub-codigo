package br.com.hobbyhub.jsf.bean;

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

@Named("manterPostsBean")
@ViewScoped
@Getter
public class ManterPostsBean implements Serializable {

    @Inject
    private HobbyHubApiClient api;

    private List<PostViewDTO> posts = List.of();

    public void carregar() {
        try {
            posts = api.listarPosts().stream().filter(post -> !post.isProprio()).toList();
        } catch (ApiException erro) {
            posts = List.of();
            mensagem(FacesMessage.SEVERITY_ERROR, erro.getMessage());
        }
    }

    public String excluir(PostViewDTO post) {
        try {
            api.excluirPost(post.getId());
            carregar();
            mensagem(FacesMessage.SEVERITY_INFO, "Post excluído com sucesso.");
        } catch (ApiException erro) {
            mensagem(FacesMessage.SEVERITY_ERROR, erro.getMessage());
        }
        return null;
    }

    private void mensagem(FacesMessage.Severity nivel, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(nivel, texto, null));
    }
}

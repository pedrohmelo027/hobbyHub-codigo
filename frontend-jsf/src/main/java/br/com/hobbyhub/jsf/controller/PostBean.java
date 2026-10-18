package br.com.hobbyhub.jsf.controller;

import br.com.hobbyhub.jsf.service.ApiException;
import br.com.hobbyhub.jsf.service.HobbyHubService;
import br.com.hobbyhub.jsf.dto.PostFormDTO;
import br.com.hobbyhub.jsf.dto.PostEdicaoFormDTO;
import br.com.hobbyhub.jsf.dto.PostViewDTO;
import br.com.hobbyhub.jsf.dto.TagViewDTO;
import javax.faces.view.ViewScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.Part;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Named("postBean")
@ViewScoped
@Getter
public class PostBean implements Serializable {

    @Inject
    private HobbyHubService api;

    private Long postEmEdicaoId;

    private PostEdicaoFormDTO edicao = new PostEdicaoFormDTO();

    private PostFormDTO form = new PostFormDTO();

    private List<PostViewDTO> posts = List.of();

    private List<TagViewDTO> tagsGerais = List.of();

    private List<TagViewDTO> tagsEspecificas = List.of();

    private boolean editorAberto;

    @Getter
    @Setter
    private transient Part arquivo;

    public void carregar() {
        try {
            posts = api.listarPosts();
            tagsGerais = api.listarTags("GERAL");
            tagsEspecificas = api.listarTags("ESPECIFICA");
        } catch (ApiException erro) {
            mensagem(FacesMessage.SEVERITY_ERROR, erro.getMessage());
        }
    }

    public List<TagViewDTO> getTagsEspecificasDisponiveis() {
        return tagsEspecificas.stream()
                .filter(tag -> Objects.equals(tag.getTagGeralId(), form.getTagGeralId()))
                .toList();
    }

    public void alterarTagGeral() {
        form.setTagEspecificaId(null);
    }

    public String publicar() {
        editorAberto = true;
        try {
            api.postar(form, arquivo);
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            mensagem(FacesMessage.SEVERITY_INFO, "Post publicado com sucesso!");
            return "/feed.xhtml?faces-redirect=true";
        } catch (ApiException erro) {
            mensagem(FacesMessage.SEVERITY_ERROR, erro.getMessage());
            return null;
        } finally {
            arquivo = null;
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
        return "/feed.xhtml?faces-redirect=true";
    }

    private void mensagem(FacesMessage.Severity nivel, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(nivel, texto, null));
    }
}

package br.com.hobbyhub.jsf.bean;

import br.com.hobbyhub.jsf.dto.PostFormDTO;
import br.com.hobbyhub.jsf.dto.TagViewDTO;
import br.com.hobbyhub.jsf.utils.ApiException;
import br.com.hobbyhub.jsf.utils.HobbyHubApiClient;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;
import lombok.Getter;
import lombok.Setter;

@Named("postarBean")
@ViewScoped
@Getter
public class PostarBean implements Serializable {

    @Inject
    private HobbyHubApiClient api;

    private PostFormDTO form = new PostFormDTO();
    private List<TagViewDTO> tagsGerais = List.of();
    private List<TagViewDTO> tagsEspecificas = List.of();
    private boolean editorAberto;

    @Getter
    @Setter
    private transient Part arquivo;

    public void carregarFormulario() {
        try {
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
            return "/visualizar-posts/feed.xhtml?faces-redirect=true";
        } catch (ApiException erro) {
            mensagem(FacesMessage.SEVERITY_ERROR, erro.getMessage());
            return null;
        } finally {
            arquivo = null;
        }
    }

    private void mensagem(FacesMessage.Severity nivel, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(nivel, texto, null));
    }
}

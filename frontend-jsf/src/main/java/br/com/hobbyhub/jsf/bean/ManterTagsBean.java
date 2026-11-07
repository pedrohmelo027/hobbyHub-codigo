package br.com.hobbyhub.jsf.bean;

import br.com.hobbyhub.jsf.dto.ManterTagDTO;
import br.com.hobbyhub.jsf.dto.ManterTagFormDTO;
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

@Named("manterTagsBean")
@ViewScoped
@Getter
public class ManterTagsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private HobbyHubApiClient api;

    private List<ManterTagDTO> tags = List.of();
    private List<ManterTagDTO> tagsGerais = List.of();
    private ManterTagFormDTO form = new ManterTagFormDTO();

    public void carregar() {
        try {
            tags = api.listarTagsGerenciadas();
            tagsGerais = tags.stream().filter(tag -> "GERAL".equals(tag.getTipo())).toList();
        } catch (ApiException erro) {
            mensagem(FacesMessage.SEVERITY_ERROR, erro.getMessage());
        }
    }

    public void novaTag() {
        form = new ManterTagFormDTO();
    }

    public List<ManterTagDTO> tagsEspecificas(ManterTagDTO geral) {
        return tags.stream()
                .filter(tag -> "ESPECIFICA".equals(tag.getTipo()))
                .filter(tag -> geral.getId().equals(tag.getTagGeralId()))
                .toList();
    }

    public String resumoEspecificas(ManterTagDTO geral) {
        int quantidade = tagsEspecificas(geral).size();
        return quantidade == 1 ? "1 tag específica" : quantidade + " tags específicas";
    }

    public void editar(ManterTagDTO tag) {
        form = new ManterTagFormDTO();
        form.setId(tag.getId());
        form.setNome(tag.getNome());
        form.setTipo(tag.getTipo());
        form.setTagGeralId(tag.getTagGeralId());
    }

    public String salvar() {
        try {
            if (form.getId() == null) api.criarTag(form);
            else api.editarTag(form);
            carregar();
            novaTag();
            mensagem(FacesMessage.SEVERITY_INFO, "Tag salva.");
        } catch (ApiException erro) {
            mensagem(FacesMessage.SEVERITY_ERROR, erro.getMessage());
        }
        return null;
    }

    public void excluir(ManterTagDTO tag) {
        try {
            api.excluirTag(tag.getId());
            carregar();
            mensagem(FacesMessage.SEVERITY_INFO, "Tag removida.");
        } catch (ApiException erro) {
            mensagem(FacesMessage.SEVERITY_ERROR, erro.getMessage());
        }
    }

    private void mensagem(FacesMessage.Severity nivel, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(nivel, texto, null));
    }
}

package br.com.hobbyhub.jsf.bean;

import br.com.hobbyhub.jsf.dto.CadastrarUsuarioFormDTO;
import br.com.hobbyhub.jsf.dto.TagViewDTO;
import br.com.hobbyhub.jsf.utils.ApiException;
import br.com.hobbyhub.jsf.utils.HobbyHubApiClient;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;
import lombok.Getter;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;
import javax.faces.view.ViewScoped;
import lombok.Setter;
import java.util.stream.Stream;

@Named("cadastrarUsuarioBean")
@ViewScoped
@Getter
public class CadastrarUsuarioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private HobbyHubApiClient api;

    private CadastrarUsuarioFormDTO form = new CadastrarUsuarioFormDTO();
    private List<TagViewDTO> tagsDisponiveis = List.of();
    private List<TagViewDTO> tagsGerais = List.of();
    private List<TagViewDTO> tagsEspecificas = List.of();
    @Setter
    private transient Part fotoPerfil;
    @Setter
    private List<Long> tagsGeraisSelecionadas = new ArrayList<>();
    @Setter
    private List<Long> tagsEspecificasSelecionadas = new ArrayList<>();

    public void carregar() {
        try {
            tagsDisponiveis = api.listarTagsDisponiveis();
            tagsGerais = filtrarPorTipo("GERAL");
            tagsEspecificas = filtrarPorTipo("ESPECIFICA");
        } catch (ApiException erro) {
            mensagem(FacesMessage.SEVERITY_ERROR, erro.getMessage());
        }
    }

    public String cadastrar() {
        try {
            form.setTagsInteresseIds(Stream.concat(tagsGeraisSelecionadas.stream(),
                    tagsEspecificasSelecionadas.stream()).toList());
            api.cadastrarUsuario(form, fotoPerfil);
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            mensagem(FacesMessage.SEVERITY_INFO, "Cadastro realizado. Entre com seu e-mail e senha.");
            return "/logar/login.xhtml?faces-redirect=true";
        } catch (ApiException erro) {
            mensagem(FacesMessage.SEVERITY_ERROR, erro.getMessage());
            form.setSenha(null);
            return null;
        }
    }

    private List<TagViewDTO> filtrarPorTipo(String tipo) {
        return tagsDisponiveis.stream().filter(tag -> tipo.equals(tag.getTipo())).toList();
    }

    private void mensagem(FacesMessage.Severity nivel, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(nivel, texto, null));
    }
}

package br.com.hobbyhub.jsf.bean;

import br.com.hobbyhub.jsf.dto.EditarPerfilFormDTO;
import br.com.hobbyhub.jsf.dto.VisualizarPerfilDTO;
import br.com.hobbyhub.jsf.utils.ApiException;
import br.com.hobbyhub.jsf.utils.HobbyHubApiClient;
import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.Getter;
import lombok.Setter;

@Named("visualizarPerfilBean")
@ViewScoped
@Getter
@Setter
public class VisualizarPerfilBean implements Serializable {

    @Inject
    private HobbyHubApiClient api;

    private String nomeUsuario;
    private VisualizarPerfilDTO perfil;

    public void carregar() {
        try {
            if (nomeUsuario == null || nomeUsuario.isBlank()) {
                EditarPerfilFormDTO meuPerfil = api.carregarPerfil();
                nomeUsuario = meuPerfil.getNomeUsuario();
            }
            perfil = api.visualizarPerfil(nomeUsuario);
        } catch (ApiException erro) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, erro.getMessage(), null));
        }
    }
}

package br.com.hobbyhub.jsf.bean;

import br.com.hobbyhub.jsf.utils.ApiException;
import br.com.hobbyhub.jsf.utils.HobbyHubApiClient;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;

@Named("loginBean")
@RequestScoped
public class LoginBean {

    @Inject
    private HobbyHubApiClient api;

    @Getter
    @Setter
    private String email;

    @Getter
    @Setter
    private String senha;

    public String entrar() {
        try {
            api.entrar(email, senha);
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                    .getExternalContext().getRequest();
            request.changeSessionId();
            return "/visualizar-posts/feed.xhtml?faces-redirect=true";
        } catch (ApiException erro) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, erro.getMessage(), null));
            return null;
        } finally {
            senha = null;
        }
    }

    public String sair() {
        try {
            api.sair();
        } catch (ApiException ignored) {
            // A sessão local também deve terminar quando o backend estiver indisponível.
        } finally {
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        }
        return "/logar/login.xhtml?faces-redirect=true";
    }
}

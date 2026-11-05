package br.com.hobbyhub.jsf.bean;

import br.com.hobbyhub.jsf.dto.EditarPerfilFormDTO;
import br.com.hobbyhub.jsf.dto.TagViewDTO;
import br.com.hobbyhub.jsf.utils.ApiException;
import br.com.hobbyhub.jsf.utils.HobbyHubApiClient;
import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Named("editarPerfilBean")
@ViewScoped
@Getter
public class EditarPerfilBean implements Serializable {

    @Inject
    private HobbyHubApiClient api;

    private EditarPerfilFormDTO perfil = new EditarPerfilFormDTO();
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
            perfil = api.carregarPerfil();
            tagsDisponiveis = api.listarTagsDisponiveis();
            tagsGerais = filtrarPorTipo("GERAL");
            tagsEspecificas = filtrarPorTipo("ESPECIFICA");
            carregarSelecaoAtual();
        } catch (ApiException erro) {
            mensagem(FacesMessage.SEVERITY_ERROR, erro.getMessage());
        }
    }

    public String salvar() {
        try {
            perfil.setTagsInteresseIds(Stream.concat(tagsGeraisSelecionadas.stream(),
                    tagsEspecificasSelecionadas.stream()).toList());
            api.editarPerfil(perfil);
            boolean fotoAtualizada = fotoPerfil != null && fotoPerfil.getSize() > 0;
            if (fotoAtualizada) {
                api.atualizarFotoPerfil(fotoPerfil);
            }
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            mensagem(FacesMessage.SEVERITY_INFO, fotoAtualizada
                    ? "Perfil e foto atualizados." : "Perfil atualizado.");
            return "/editar-perfil/editar-perfil.xhtml?faces-redirect=true";
        } catch (ApiException erro) {
            mensagem(FacesMessage.SEVERITY_ERROR, erro.getMessage());
            return null;
        }
    }

    private List<TagViewDTO> filtrarPorTipo(String tipo) {
        return tagsDisponiveis.stream().filter(tag -> tipo.equals(tag.getTipo())).toList();
    }

    private void carregarSelecaoAtual() {
        Set<Long> idsSelecionados = perfil.getTagsInteresseIds() == null
                ? Set.of() : new HashSet<>(perfil.getTagsInteresseIds());
        tagsGeraisSelecionadas = selecionarIds(tagsGerais, idsSelecionados);
        tagsEspecificasSelecionadas = selecionarIds(tagsEspecificas, idsSelecionados);
    }

    private List<Long> selecionarIds(List<TagViewDTO> tags, Set<Long> idsSelecionados) {
        return tags.stream().map(TagViewDTO::getId).filter(idsSelecionados::contains).toList();
    }

    private void mensagem(FacesMessage.Severity nivel, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(nivel, texto, null));
    }
}

package br.com.hobbyhub.controller;

import br.com.hobbyhub.dto.EditarPerfilDTO;
import br.com.hobbyhub.dto.EditarPerfilFormDTO;
import br.com.hobbyhub.service.EditarPerfilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

@SecurityRequirement(name = "basicAuth")
@RestController
@RequestMapping("/api/perfil")
@RequiredArgsConstructor
public class EditarPerfilController {

    private final EditarPerfilService editarPerfilService;

    @GetMapping
    @Operation(summary = "Consultar perfil autenticado para edição")
    public EditarPerfilDTO carregar(Principal principal) {
        return editarPerfilService.carregar(principal.getName());
    }

    @PatchMapping
    @Operation(summary = "Editar nome e biografia do próprio perfil")
    public EditarPerfilDTO editar(@Valid @RequestBody EditarPerfilFormDTO form, Principal principal) {
        return editarPerfilService.editar(principal.getName(), form);
    }

    @PostMapping(value = "/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Atualizar foto do próprio perfil")
    public EditarPerfilDTO atualizarFoto(@RequestParam("foto") MultipartFile foto, Principal principal) {
        return editarPerfilService.atualizarFoto(principal.getName(), foto);
    }
}

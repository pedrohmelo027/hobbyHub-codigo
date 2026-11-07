package br.com.hobbyhub.controller;

import br.com.hobbyhub.dto.VisualizarPerfilDTO;
import br.com.hobbyhub.service.VisualizarPerfilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "basicAuth")
@RestController
@RequestMapping("/api/perfis")
@RequiredArgsConstructor
public class VisualizarPerfilController {

    private final VisualizarPerfilService visualizarPerfilService;

    @GetMapping("/{nomeUsuario}")
    @Operation(summary = "Visualizar perfil e posts de um usuário")
    public VisualizarPerfilDTO visualizar(@PathVariable String nomeUsuario, Principal principal) {
        return visualizarPerfilService.visualizar(nomeUsuario, principal.getName());
    }
}

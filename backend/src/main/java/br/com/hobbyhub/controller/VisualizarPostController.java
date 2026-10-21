package br.com.hobbyhub.controller;

import br.com.hobbyhub.dto.PostViewDTO;
import br.com.hobbyhub.service.VisualizarPostService;
import br.com.hobbyhub.utils.MidiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "basicAuth")
@RestController
@RequiredArgsConstructor
public class VisualizarPostController {

    private final VisualizarPostService visualizarPostService;
    private final MidiaService midiaService;

    @GetMapping("/api/posts")
    @Operation(summary = "Listar posts do feed")
    public List<PostViewDTO> listar(Principal principal) {
        return visualizarPostService.listar(principal.getName());
    }

    @GetMapping("/midias/{nome}")
    public ResponseEntity<Resource> visualizarMidia(@PathVariable String nome) {
        return ResponseEntity.ok()
                .contentType(midiaService.obterTipoConteudo(nome))
                .body(midiaService.carregar(nome));
    }
}

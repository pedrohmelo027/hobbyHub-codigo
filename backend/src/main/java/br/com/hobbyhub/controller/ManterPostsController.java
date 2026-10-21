package br.com.hobbyhub.controller;

import br.com.hobbyhub.dto.ManterPostsFormDTO;
import br.com.hobbyhub.dto.PostViewDTO;
import br.com.hobbyhub.service.ManterPostsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "basicAuth")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class ManterPostsController {

    private final ManterPostsService manterPostsService;

    @PatchMapping(value = "/{id}", consumes = "application/json")
    @Operation(summary = "Editar somente o texto de um post próprio",
            parameters = @Parameter(name = "X-CSRF-TOKEN", in = ParameterIn.HEADER, required = true))
    public PostViewDTO editar(@PathVariable Long id, @Valid @RequestBody ManterPostsFormDTO form,
            Principal principal) {
        return manterPostsService.editar(id, form, principal.getName());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir um post próprio e sua mídia",
            parameters = @Parameter(name = "X-CSRF-TOKEN", in = ParameterIn.HEADER, required = true))
    public ResponseEntity<Void> excluir(@PathVariable Long id, Principal principal) {
        manterPostsService.excluir(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}

package br.com.hobbyhub.controller;

import br.com.hobbyhub.dto.ManterTagDTO;
import br.com.hobbyhub.dto.ManterTagFormDTO;
import br.com.hobbyhub.service.ManterTagsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "basicAuth")
@RestController
@RequestMapping("/api/tags/gerenciadas")
@RequiredArgsConstructor
public class ManterTagsController {

    private final ManterTagsService manterTagsService;

    @GetMapping
    @Operation(summary = "Listar tags para administração")
    public List<ManterTagDTO> listar(Principal principal) {
        return manterTagsService.listar(principal.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar tag")
    public ManterTagDTO criar(@Valid @RequestBody ManterTagFormDTO form, Principal principal) {
        return manterTagsService.criar(form, principal.getName());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar tag")
    public ManterTagDTO editar(@PathVariable Long id, @Valid @RequestBody ManterTagFormDTO form,
            Principal principal) {
        return manterTagsService.editar(id, form, principal.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remover tag sem uso")
    public void excluir(@PathVariable Long id, Principal principal) {
        manterTagsService.excluir(id, principal.getName());
    }
}

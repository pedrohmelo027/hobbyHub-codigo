package br.com.hobbyhub.controller;

import br.com.hobbyhub.dto.TagViewDTO;
import br.com.hobbyhub.model.TipoDeTag;
import br.com.hobbyhub.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SecurityRequirement(name = "basicAuth")
@RestController
@RequiredArgsConstructor
public class TagController {

    private final PostService postService;

    @GetMapping("/api/tags")
    @Operation(summary = "Consultar tags disponíveis para postar")
    public List<TagViewDTO> listar(@RequestParam TipoDeTag tipo) {
        return postService.listarTags(tipo);
    }
}

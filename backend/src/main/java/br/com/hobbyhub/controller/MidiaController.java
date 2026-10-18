package br.com.hobbyhub.controller;

import br.com.hobbyhub.service.MidiaService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "basicAuth")
@RestController
@RequiredArgsConstructor
public class MidiaController {

    private final MidiaService midiaService;

    @GetMapping("/midias/{nome}")
    public ResponseEntity<Resource> visualizar(@PathVariable String nome) {
        Resource arquivo = midiaService.carregar(nome);
        return ResponseEntity.ok()
                .contentType(midiaService.obterTipoConteudo(nome))
                .body(arquivo);
    }
}

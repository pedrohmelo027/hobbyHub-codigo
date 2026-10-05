package br.com.hobbyhub.controller;

import br.com.hobbyhub.service.ArmazenamentoMidiaService;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MidiaController {

    private final ArmazenamentoMidiaService armazenamentoMidiaService;

    @GetMapping("/midias/{nome}")
    public ResponseEntity<Resource> visualizar(@PathVariable String nome) {
        Resource arquivo = armazenamentoMidiaService.carregar(nome);
        return ResponseEntity.ok()
                .contentType(armazenamentoMidiaService.obterTipoConteudo(nome))
                .body(arquivo);
    }
}

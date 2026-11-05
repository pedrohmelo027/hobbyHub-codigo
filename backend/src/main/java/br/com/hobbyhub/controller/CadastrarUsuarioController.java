package br.com.hobbyhub.controller;

import br.com.hobbyhub.dto.CadastrarUsuarioFormDTO;
import br.com.hobbyhub.service.CadastrarUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class CadastrarUsuarioController {

    private final CadastrarUsuarioService cadastrarUsuarioService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastrar usuário")
    public ResponseEntity<Void> cadastrar(@Valid @RequestBody CadastrarUsuarioFormDTO form) {
        cadastrarUsuarioService.cadastrar(form);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cadastrar usuário com foto de perfil opcional")
    public ResponseEntity<Void> cadastrarComFoto(
            @Valid @RequestPart("form") CadastrarUsuarioFormDTO form,
            @RequestPart(value = "foto", required = false) MultipartFile foto) {
        cadastrarUsuarioService.cadastrar(form, foto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

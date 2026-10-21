package br.com.hobbyhub.controller;

import br.com.hobbyhub.dto.PostarFormDTO;
import br.com.hobbyhub.dto.PostViewDTO;
import br.com.hobbyhub.dto.PostarTagViewDTO;
import br.com.hobbyhub.model.TipoDeTag;
import br.com.hobbyhub.service.PostarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@SecurityRequirement(name = "basicAuth")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostarController {

    private final PostarService postarService;

    @GetMapping("/csrf")
    @Operation(summary = "Obter token CSRF da sessão para publicar pela API")
    public Map<String, String> csrf(@Parameter(hidden = true) CsrfToken token) {
        return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
    }

    @GetMapping("/tags")
    @Operation(summary = "Consultar tags disponíveis para postar")
    public List<PostarTagViewDTO> listarTags(@RequestParam TipoDeTag tipo) {
        return postarService.listarTags(tipo);
    }

    @PostMapping(value = "/posts", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Publicar post", description = "Após login, obtenha /api/csrf e envie X-CSRF-TOKEN mantendo o cookie da sessão.",
            parameters = @Parameter(name = "X-CSRF-TOKEN", in = ParameterIn.HEADER, required = true,
                    description = "Token obtido em GET /api/csrf"))
    public ResponseEntity<PostViewDTO> postar(@Valid @RequestBody PostarFormDTO form, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postarService.postar(form, principal.getName(), null));
    }

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Publicar post com imagem ou vídeo")
    public ResponseEntity<PostViewDTO> postarComArquivo(@Valid @ModelAttribute PostarFormDTO form,
            @RequestParam(required = false) MultipartFile arquivo, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postarService.postar(form, principal.getName(), arquivo));
    }
}

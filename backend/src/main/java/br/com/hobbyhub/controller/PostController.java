package br.com.hobbyhub.controller;

import br.com.hobbyhub.dto.PostFormDTO;
import br.com.hobbyhub.dto.PostEdicaoFormDTO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import br.com.hobbyhub.dto.PostViewDTO;
import br.com.hobbyhub.service.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

import java.security.Principal;
import java.util.List;
import java.util.Map;

@SecurityRequirement(name = "basicAuth")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping("/csrf")
    @Operation(summary = "Obter token CSRF da sessão para publicar pela API")
    public Map<String, String> csrf(@Parameter(hidden = true) CsrfToken token) {
        return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
    }

    @GetMapping("/posts")
    @Operation(summary = "Listar posts do feed")
    public List<PostViewDTO> listar(Principal principal) {
        return postService.listar(principal.getName());
    }

    @PostMapping(value = "/posts", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Publicar post",
            description =
                    "Após login, obtenha /api/csrf e informe o token no header X-CSRF-TOKEN,"
                            + " mantendo o cookie da sessão.",
            parameters =
                    @Parameter(
                            name = "X-CSRF-TOKEN",
                            in = ParameterIn.HEADER,
                            required = true,
                            description = "Token obtido em GET /api/csrf"))
    public ResponseEntity<PostViewDTO> postar(@Valid @RequestBody PostFormDTO form, Principal principal) {
        PostViewDTO post = postService.postar(form, principal.getName(), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Publicar post com imagem ou vídeo")
    public ResponseEntity<PostViewDTO> postarComArquivo(
            @Valid @ModelAttribute PostFormDTO form,
            @RequestParam(required = false) MultipartFile arquivo,
            Principal principal) {
        PostViewDTO post = postService.postar(form, principal.getName(), arquivo);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @PatchMapping(value = "/posts/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Editar somente o texto de um post próprio",
            parameters = @Parameter(name = "X-CSRF-TOKEN", in = ParameterIn.HEADER, required = true))
    public PostViewDTO editar(@PathVariable Long id, @Valid @RequestBody PostEdicaoFormDTO form,
            Principal principal) {
        return postService.editar(id, form, principal.getName());
    }

    @DeleteMapping("/posts/{id}")
    @Operation(summary = "Excluir um post próprio e sua mídia",
            parameters = @Parameter(name = "X-CSRF-TOKEN", in = ParameterIn.HEADER, required = true))
    public ResponseEntity<Void> excluir(@PathVariable Long id, Principal principal) {
        postService.excluir(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}

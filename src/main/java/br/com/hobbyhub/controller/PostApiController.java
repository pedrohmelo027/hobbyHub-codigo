package br.com.hobbyhub.controller;

import br.com.hobbyhub.dto.PostForm;
import br.com.hobbyhub.dto.PostView;
import br.com.hobbyhub.service.PostService;
import br.com.hobbyhub.service.RegraNegocioException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostApiController {
    private final PostService postService;

    @GetMapping("/csrf")
    @Operation(summary = "Obter token CSRF da sessão para publicar pela API")
    public Map<String, String> csrf(@Parameter(hidden = true) CsrfToken token) {
        return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
    }

    @GetMapping("/posts")
    @Operation(summary = "Listar posts do feed")
    public List<PostView> listar() {
        return postService.listar();
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
    public ResponseEntity<PostView> postar(@Valid @RequestBody PostForm form, Principal principal) {
        PostView post = postService.postar(form, principal.getName(), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Publicar post com imagem ou vídeo")
    public ResponseEntity<PostView> postarComArquivo(
            @Valid @ModelAttribute PostForm form,
            @RequestParam(required = false) MultipartFile arquivo,
            Principal principal) {
        PostView post = postService.postar(form, principal.getName(), arquivo);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public Map<String, String> arquivoMuitoGrande() {
        return Map.of("erro", "O arquivo deve ter no máximo 25 MB.");
    }

    @ExceptionHandler(RegraNegocioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> regra(RegraNegocioException e) {
        return Map.of("erro", e.getMessage());
    }
}

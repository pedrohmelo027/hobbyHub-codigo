package br.com.hobbyhub.controller;

import br.com.hobbyhub.service.RegraNegocioException;
import java.util.NoSuchElementException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class UploadExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> acessoNegado(AccessDeniedException erro) {
        return Map.of("erro", erro.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> naoEncontrado(NoSuchElementException erro) {
        return Map.of("erro", erro.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public Map<String, String> arquivoMuitoGrande() {
        return Map.of("erro", "O arquivo deve ter no máximo 25 MB.");
    }

    @ExceptionHandler(RegraNegocioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> regra(RegraNegocioException erro) {
        return Map.of("erro", erro.getMessage());
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> validar(BindException erro) {
        String mensagem = erro.getBindingResult().getAllErrors().stream()
                .map(item -> item.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining(" "));
        return Map.of("erro", mensagem);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> formatoInvalido() {
        return Map.of("erro", "Dados da requisição inválidos.");
    }
}

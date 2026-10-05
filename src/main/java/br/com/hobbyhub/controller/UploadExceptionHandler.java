package br.com.hobbyhub.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.Map;

@ControllerAdvice
public class UploadExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView arquivoMuitoGrande(HttpServletRequest request) {
        ModelAndView resposta;

        if (request.getRequestURI().startsWith("/api/")) {
            resposta =
                    new ModelAndView(
                            new MappingJackson2JsonView(),
                            Map.of("erro", "O arquivo deve ter no máximo 25 MB."));
        } else {
            resposta = new ModelAndView("erro-upload");
        }

        resposta.setStatus(HttpStatus.PAYLOAD_TOO_LARGE);
        return resposta;
    }
}

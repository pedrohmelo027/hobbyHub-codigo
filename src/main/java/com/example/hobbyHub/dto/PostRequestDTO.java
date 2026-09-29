package com.example.hobbyHub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostRequestDTO(
        @NotBlank(message = "O conteúdo não pode estar vazio")
        String conteudo,

        @NotNull(message = "O usuarioId é obrigatório")
        Long usuarioId
) {}
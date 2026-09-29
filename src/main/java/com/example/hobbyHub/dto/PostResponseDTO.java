package com.example.hobbyHub.dto;

import java.time.LocalDateTime;

public record PostResponseDTO(
        Long id,
        String conteudo,
        Long usuarioId,
        LocalDateTime dataCriacao,
        String midiaUrl
) {}
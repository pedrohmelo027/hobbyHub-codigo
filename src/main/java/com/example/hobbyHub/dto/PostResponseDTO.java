package com.example.hobbyHub.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponseDTO(
        Long id,
        String conteudo,
        Long usuarioId,
        LocalDateTime dataCriacao,
        List<String> midiaUrls
) {}
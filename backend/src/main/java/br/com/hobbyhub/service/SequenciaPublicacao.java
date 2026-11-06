package br.com.hobbyhub.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class SequenciaPublicacao {

    private SequenciaPublicacao() {}

    public static int calcular(List<LocalDateTime> publicacoes, LocalDate hoje) {
        Set<LocalDate> diasPublicados = publicacoes.stream()
                .filter(data -> data != null)
                .map(LocalDateTime::toLocalDate)
                .collect(Collectors.toSet());
        LocalDate dia = diasPublicados.contains(hoje) ? hoje : hoje.minusDays(1);
        if (!diasPublicados.contains(dia)) return 0;

        int sequencia = 0;
        while (diasPublicados.contains(dia)) {
            sequencia++;
            dia = dia.minusDays(1);
        }
        return sequencia;
    }
}

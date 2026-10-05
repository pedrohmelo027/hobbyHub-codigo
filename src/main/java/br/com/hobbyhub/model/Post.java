package br.com.hobbyhub.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String conteudo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(length = 500)
    private String urlImagem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToMany
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    @PrePersist
    private void prepararPublicacao() {
        validarConteudo();
        validarAutorETags();
        dataCriacao = LocalDateTime.now();
    }

    private void validarConteudo() {
        if (conteudo == null || conteudo.isBlank() || conteudo.length() > 2000) {
            throw new IllegalStateException("O conteúdo deve ter entre 1 e 2000 caracteres.");
        }
    }

    private void validarAutorETags() {
        if (usuario == null || tags == null || tags.size() != 2) {
            throw new IllegalStateException("O post deve ter um autor e exatamente duas tags.");
        }

        Tag geral = obterTag(TipoDeTag.GERAL);
        Tag especifica = obterTag(TipoDeTag.ESPECIFICA);

        if (!especifica.pertenceA(geral)) {
            throw new IllegalStateException(
                    "A tag específica deve pertencer à tag geral selecionada.");
        }
    }

    private Tag obterTag(TipoDeTag tipo) {
        return tags.stream()
                .filter(tag -> tag.getTipo() == tipo)
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Selecione uma tag geral e uma específica."));
    }
}

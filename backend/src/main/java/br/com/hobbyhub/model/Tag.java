package br.com.hobbyhub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tags", uniqueConstraints = @UniqueConstraint(columnNames = {"nome", "tipo"}))
@Getter
@Setter
@NoArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDeTag tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_geral_id")
    private Tag tagGeral;

    public Tag(String nome, TipoDeTag tipo) {
        this.nome = nome;
        this.tipo = tipo;
    }

    public static Tag especifica(String nome, Tag tagGeral) {
        Tag tag = new Tag(nome, TipoDeTag.ESPECIFICA);
        tag.setTagGeral(tagGeral);
        return tag;
    }

    public boolean pertenceA(Tag geral) {
        return tipo == TipoDeTag.ESPECIFICA
                && tagGeral != null
                && tagGeral.getId() != null
                && tagGeral.getId().equals(geral.getId());
    }

    @PrePersist
    @PreUpdate
    private void validarHierarquia() {
        if (tipo == null) {
            throw new IllegalStateException("Informe o tipo da tag.");
        }

        if (tipo == TipoDeTag.GERAL && tagGeral != null) {
            throw new IllegalStateException("Uma tag geral não pode ter outra tag como pai.");
        }

        if (tipo == TipoDeTag.ESPECIFICA
                && (tagGeral == null || tagGeral.getTipo() != TipoDeTag.GERAL)) {
            throw new IllegalStateException("Uma tag específica deve pertencer a uma tag geral.");
        }
    }
}

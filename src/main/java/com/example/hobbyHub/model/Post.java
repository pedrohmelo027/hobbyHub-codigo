package com.example.hobbyHub.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String conteudo;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime dataCriacao;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostMidia> midias = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
    }

    public void adicionarMidia(PostMidia midia) {
        midia.setPost(this);
        this.midias.add(midia);
    }
}
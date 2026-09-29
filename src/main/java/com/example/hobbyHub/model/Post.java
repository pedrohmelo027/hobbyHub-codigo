package com.example.hobbyHub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
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

    @Column
    private String midiaUrl;

    @Enumerated(EnumType.STRING)
    private TipoMidia tipo;

    public Post() {
    }

    public Post(Long id, String conteudo, Long usuarioId, LocalDateTime dataCriacao, String midiaUrl, TipoMidia tipo) {
        this.id = id;
        this.conteudo = conteudo;
        this.usuarioId = usuarioId;
        this.dataCriacao = dataCriacao;
        this.midiaUrl = midiaUrl;
        this.tipo = tipo;
    }

    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getMidiaUrl() {
        return midiaUrl;
    }

    public void setMidiaUrl(String midiaUrl) {
        this.midiaUrl = midiaUrl;
    }

    public TipoMidia getTipo() {
        return tipo;
    }

    public void setTipo(TipoMidia tipo) {
        this.tipo = tipo;
    }
}
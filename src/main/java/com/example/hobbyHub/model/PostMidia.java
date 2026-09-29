package com.example.hobbyHub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_midia")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PostMidia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    private TipoMidia tipo;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore // evita loop infinito ao serializar (Post -> midias -> post -> midias...)
    private Post post;
}

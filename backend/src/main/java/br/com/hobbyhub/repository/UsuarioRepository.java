package br.com.hobbyhub.repository;

import br.com.hobbyhub.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByNomeUsuarioIgnoreCase(String nomeUsuario);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByNomeUsuarioIgnoreCase(String nomeUsuario);
}

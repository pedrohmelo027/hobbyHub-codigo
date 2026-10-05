package br.com.hobbyhub.repository;

import br.com.hobbyhub.model.Tag;
import br.com.hobbyhub.model.TipoDeTag;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    @EntityGraph(attributePaths = "tagGeral")
    List<Tag> findByTipoOrderByNomeAsc(TipoDeTag tipo);

    Optional<Tag> findByNomeAndTipo(String nome, TipoDeTag tipo);
}

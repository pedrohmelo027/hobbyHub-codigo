package br.com.hobbyhub.repository;

import br.com.hobbyhub.model.Post;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = {"usuario", "tags"})
    List<Post> findAllByOrderByDataCriacaoDescIdDesc();
}

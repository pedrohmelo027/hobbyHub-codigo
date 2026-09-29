package com.example.hobbyHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.hobbyHub.model.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
}
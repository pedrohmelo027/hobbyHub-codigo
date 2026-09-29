package com.example.hobbyHub.controller;

import com.example.hobbyHub.dto.PostResponseDTO;
import com.example.hobbyHub.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*") // Permite o acesso do frontend local (Live Server ou arquivo aberto)
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // UC05 - Postar (agora com upload de arquivos)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDTO> criarPost(
            @RequestParam("conteudo") String conteudo,
            @RequestParam("usuarioId") Long usuarioId,
            @RequestParam(value = "arquivos", required = false) MultipartFile arquivo
    ) throws IOException {
        PostResponseDTO response = postService.criarPost(conteudo, usuarioId, arquivo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PostResponseDTO>> listarPosts() {
        return ResponseEntity.ok(postService.listarPosts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(postService.buscarPorId(id));
    }
}
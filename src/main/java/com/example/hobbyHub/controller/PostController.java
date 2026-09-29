package com.example.hobbyHub.controller;

import com.example.hobbyHub.dto.PostResponseDTO;
import com.example.hobbyHub.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // UC05 - Postar (agora com upload de arquivos)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDTO> criarPost(
            @RequestParam("conteudo") String conteudo,
            @RequestParam("usuarioId") Long usuarioId,
            @RequestParam(value = "arquivos", required = false) List<MultipartFile> arquivos
    ) throws IOException {
        PostResponseDTO response = postService.criarPost(conteudo, usuarioId, arquivos);
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
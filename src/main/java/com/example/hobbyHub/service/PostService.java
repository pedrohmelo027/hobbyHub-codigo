package com.example.hobbyHub.service;

import com.example.hobbyHub.dto.PostResponseDTO;
import com.example.hobbyHub.model.Post;
import com.example.hobbyHub.model.TipoMidia;
import com.example.hobbyHub.repository.PostRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Value("${app.upload.dir}")
    private String uploadDir;

    public PostResponseDTO criarPost(String conteudo, Long usuarioId, MultipartFile arquivo) throws IOException {
        Post post = new Post();
        post.setConteudo(conteudo);
        post.setUsuarioId(usuarioId);

        if (arquivo != null && !arquivo.isEmpty()) {
            String nomeArquivo = salvarArquivoNoDisco(arquivo);
            TipoMidia tipo = detectarTipo(arquivo);

            post.setMidiaUrl("/uploads/" + nomeArquivo);
            post.setTipo(tipo);
        }

        Post salvo = postRepository.save(post);
        return toResponseDTO(salvo);
    }

    public List<PostResponseDTO> listarPosts() {
        return postRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public PostResponseDTO buscarPorId(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post não encontrado: " + id));
        return toResponseDTO(post);
    }

    private String salvarArquivoNoDisco(MultipartFile arquivo) throws IOException {
        Path pastaUploads = Paths.get(uploadDir);
        if (!Files.exists(pastaUploads)) {
            Files.createDirectories(pastaUploads);
        }

        String nomeOriginal = arquivo.getOriginalFilename();
        String extensao = "";
        if (nomeOriginal != null && nomeOriginal.contains(".")) {
            extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
        }

        String nomeUnico = UUID.randomUUID() + extensao;
        Path destino = pastaUploads.resolve(nomeUnico);
        Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        return nomeUnico;
    }

    private TipoMidia detectarTipo(MultipartFile arquivo) {
        String contentType = arquivo.getContentType();
        if (contentType != null && contentType.startsWith("video")) {
            return TipoMidia.VIDEO;
        }
        return TipoMidia.FOTO;
    }

    private PostResponseDTO toResponseDTO(Post post) {
        return new PostResponseDTO(
                post.getId(),
                post.getConteudo(),
                post.getUsuarioId(),
                post.getDataCriacao(),
                post.getMidiaUrl()
        );
    }
}
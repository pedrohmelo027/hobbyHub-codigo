package com.example.hobbyHub.service;

import com.example.hobbyHub.dto.PostResponseDTO;
import com.example.hobbyHub.model.Post;
import com.example.hobbyHub.model.PostMidia;
import com.example.hobbyHub.model.TipoMidia;
import com.example.hobbyHub.repository.PostRepository;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public PostResponseDTO criarPost(String conteudo, Long usuarioId, List<MultipartFile> arquivos) throws IOException {
        Post post = Post.builder()
                .conteudo(conteudo)
                .usuarioId(usuarioId)
                .build();

        if (arquivos != null) {
            for (MultipartFile arquivo : arquivos) {
                if (arquivo.isEmpty()) continue;

                String nomeArquivo = salvarArquivoNoDisco(arquivo);
                TipoMidia tipo = detectarTipo(arquivo);

                PostMidia midia = PostMidia.builder()
                        .url("/uploads/" + nomeArquivo)
                        .tipo(tipo)
                        .build();

                post.adicionarMidia(midia);
            }
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
        List<String> midiaUrls = post.getMidias()
                .stream()
                .map(PostMidia::getUrl)
                .toList();

        return new PostResponseDTO(
                post.getId(),
                post.getConteudo(),
                post.getUsuarioId(),
                post.getDataCriacao(),
                midiaUrls
        );
    }
}
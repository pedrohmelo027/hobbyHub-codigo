package br.com.hobbyhub.service;

import br.com.hobbyhub.dto.PostViewDTO;
import br.com.hobbyhub.model.Post;
import br.com.hobbyhub.repository.PostRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VisualizarPostService {

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public List<PostViewDTO> listar(String email) {
        return listar(email, null);
    }

    @Transactional(readOnly = true)
    public List<PostViewDTO> listar(String email, Long tagId) {
        List<Post> posts = tagId == null
                ? postRepository.findAllByOrderByDataCriacaoDescIdDesc()
                : postRepository.findDistinctByTags_IdOrderByDataCriacaoDescIdDesc(tagId);
        return posts.stream()
                .map(post -> PostViewDTO.from(post, email))
                .toList();
    }
}

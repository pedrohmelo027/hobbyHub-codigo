package br.com.hobbyhub.controller;

import br.com.hobbyhub.dto.PostForm;
import br.com.hobbyhub.model.TipoDeTag;
import br.com.hobbyhub.service.PostService;
import br.com.hobbyhub.service.RegraNegocioException;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping("/")
    public String inicio() {
        return "redirect:/posts";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/posts")
    public String feed(Model model) {
        model.addAttribute("postForm", new PostForm());
        model.addAttribute("posts", postService.listar());
        carregarTags(model);
        return "posts/feed";
    }

    @GetMapping("/posts/novo")
    public String formulario(Model model) {
        model.addAttribute("postForm", new PostForm());
        return preparar(model);
    }

    @PostMapping("/posts")
    public String postar(
            @Valid @ModelAttribute PostForm postForm,
            BindingResult erros,
            @RequestParam(required = false) MultipartFile arquivo,
            @RequestParam(defaultValue = "false") boolean noFeed,
            Principal principal,
            Model model,
            RedirectAttributes redirect) {
        if (erros.hasErrors()) return apresentarErros(model, noFeed);
        try {
            postService.postar(postForm, principal.getName(), arquivo);
        } catch (RegraNegocioException e) {
            erros.reject("post.invalido", e.getMessage());
            return apresentarErros(model, noFeed);
        }
        redirect.addFlashAttribute("sucesso", "Post publicado com sucesso!");
        return "redirect:/posts";
    }

    private String preparar(Model model) {
        carregarTags(model);
        return "posts/formulario";
    }

    private String apresentarErros(Model model, boolean noFeed) {
        if (!noFeed) return preparar(model);
        model.addAttribute("posts", postService.listar());
        model.addAttribute("abrirEditor", true);
        carregarTags(model);
        return "posts/feed";
    }

    private void carregarTags(Model model) {
        model.addAttribute("tagsGerais", postService.listarTags(TipoDeTag.GERAL));
        model.addAttribute("tagsEspecificas", postService.listarTags(TipoDeTag.ESPECIFICA));
    }
}

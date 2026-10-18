package br.com.hobbyhub.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.hobbyhub.model.Role;
import br.com.hobbyhub.model.Tag;
import br.com.hobbyhub.model.TipoDeTag;
import br.com.hobbyhub.model.Usuario;
import br.com.hobbyhub.repository.TagRepository;
import br.com.hobbyhub.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class DadosIniciais implements CommandLineRunner {

    private final UsuarioRepository usuarios;
    private final TagRepository tags;
    private final PasswordEncoder encoder;

    @Override
    @Transactional
    public void run(String... args) {
        criarUsuarioDemo();
        Tag esporte = obterOuCriarGeral("Esporte");
        Tag arte = obterOuCriarGeral("Arte");
        Tag tecnologia = obterOuCriarGeral("Tecnologia");
        vincularEspecifica("Futebol", esporte);
        vincularEspecifica("Pintura", arte);
        vincularEspecifica("Programação", tecnologia);
    }

    private void criarUsuarioDemo() {
        if (usuarios.findByEmail("demo@hobbyhub.local").isPresent()) {
            return;
        }
        Usuario usuario = new Usuario();
        usuario.setNome("Usuário de demonstração");
        usuario.setNomeUsuario("hobbylover");
        usuario.setEmail("demo@hobbyhub.local");
        usuario.setBiografia("Explorando novos hobbies.");
        usuario.setSenha(encoder.encode("HobbyHub123!"));
        usuario.setRole(Role.USUARIO);
        usuarios.save(usuario);
    }

    private Tag obterOuCriarGeral(String nome) {
        return tags.findByNomeAndTipo(nome, TipoDeTag.GERAL)
                .orElseGet(() -> tags.save(new Tag(nome, TipoDeTag.GERAL)));
    }

    private void vincularEspecifica(String nome, Tag geral) {
        Tag especifica =
                tags.findByNomeAndTipo(nome, TipoDeTag.ESPECIFICA)
                        .orElseGet(() -> Tag.especifica(nome, geral));

        if (especifica.getTagGeral() == null) {
            especifica.setTagGeral(geral);
        }
        tags.save(especifica);
    }
}

package br.com.hobbyhub.config;

import br.com.hobbyhub.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.util.Map;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(UsuarioRepository usuarios) {
        return email -> usuarios.findByEmail(email)
                .map(usuario -> User.withUsername(usuario.getEmail())
                        .password(usuario.getSenha())
                        .roles(usuario.getRole().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas."));
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper json) throws Exception {
        return http
                .authorizeHttpRequests(regras -> regras
                        .requestMatchers("/api/csrf", "/api/login", "/api/usuarios", "/api/tags", "/error",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .requestCache(cache -> cache.disable())
                .formLogin(login -> login
                        .loginPage("/api/login")
                        .loginProcessingUrl("/api/login")
                        .usernameParameter("email")
                        .successHandler((request, response, authentication) ->
                                responderLogin(json, response, authentication))
                        .failureHandler((request, response, exception) ->
                                responder(json, response, 401, "erro", "E-mail ou senha inválidos.")))
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint((request, response, exception) ->
                                responder(json, response, 401, "erro", "Autenticação necessária."))
                        .accessDeniedHandler((request, response, exception) ->
                                responder(json, response, 403, "erro", "Acesso negado ou token CSRF inválido.")))
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(204)))
                .build();
    }

    private void responder(ObjectMapper json, HttpServletResponse response,
            int status, String chave, String valor) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        json.writeValue(response.getWriter(), Map.of(chave, valor));
    }

    private void responderLogin(ObjectMapper json, HttpServletResponse response,
            org.springframework.security.core.Authentication authentication) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        boolean administrador = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        json.writeValue(response.getWriter(), Map.of(
                "email", authentication.getName(),
                "administrador", administrador));
    }
}

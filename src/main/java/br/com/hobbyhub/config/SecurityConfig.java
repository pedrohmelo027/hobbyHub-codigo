package br.com.hobbyhub.config;

import br.com.hobbyhub.repository.UsuarioRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(UsuarioRepository usuarios) {
        return email ->
                usuarios.findByEmail(email)
                        .map(
                                u ->
                                        User.withUsername(u.getEmail())
                                                .password(u.getSenha())
                                                .roles(u.getRole().name())
                                                .build())
                        .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas."));
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(
                        a ->
                                a.requestMatchers("/login", "/css/**", "/error")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .formLogin(
                        f ->
                                f.loginPage("/login")
                                        .usernameParameter("email")
                                        .defaultSuccessUrl("/posts", false)
                                        .permitAll())
                .logout(l -> l.logoutSuccessUrl("/login?logout"))
                .build();
    }
}

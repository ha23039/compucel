package edu.sv.ues.dam235.compucelapi.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Mismo formato de hash que usa la app ($2a$10$...), para que las claves sean compatibles
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Clientes queda abierto mientras se implementa JWT (fase 2 del roadmap)
                        .requestMatchers("/clientes", "/clientes/**").permitAll()
                        // Spring reenvia los errores (400, 404) a /error: si se protege, todo error se vuelve 403
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/info",
                                "/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs", "/v3/api-docs/**")
                        .permitAll()
                        // El resto queda protegido hasta agregar la autenticacion con JWT
                        .anyRequest().authenticated());
        return httpSecurity.build();
    }
}

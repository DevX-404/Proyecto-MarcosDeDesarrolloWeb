package com.example.demo.Config;

import com.example.demo.Util.JwtUtil;
import com.example.demo.Repository.AlumnoRepository; 
import com.example.demo.Repository.ProfesorRepository; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration("AppSecurityConfig") 
@EnableWebSecurity 
@EnableMethodSecurity(prePostEnabled = true) 
public class SecurityConfig {

    // ⭐ Mantenemos solo el filtro de autorización aquí
    @Autowired
    private JwtRequestFilter jwtRequestFilter;
    
    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;
    
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ⭐ DEFINIMOS JwtAuthenticationFilter COMO BEAN, INYECTANDO REPOSITORIOS LOCALMENTE
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
        JwtUtil jwtUtil, 
        ProfesorRepository profesorRepository, // Inyección local
        AlumnoRepository alumnoRepository // Inyección local
    ) throws Exception {
        return new JwtAuthenticationFilter(
            authenticationManager(), 
            jwtUtil, 
            profesorRepository, 
            alumnoRepository
        );
    }
    // ⭐ FIN DEFINICIÓN BEAN

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        
        http
            .csrf(csrf -> csrf.disable()) 
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(authorize -> authorize
                // Rutas públicas
                .requestMatchers("/login", "/css/**", "/js/**", "/img/**", "/initdata").permitAll()
                // Rutas protegidas por rol
                .requestMatchers("/profesor/**").hasRole("PROFESOR")
                .requestMatchers("/alumno/**").hasRole("ALUMNO")
                .anyRequest().authenticated()
            )
            // Agrega el filtro de autenticación (login)
            .addFilter(jwtAuthenticationFilter) 
            // Agrega el filtro de autorización (validación del token en cada petición)
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
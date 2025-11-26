package com.example.demo.Config;

import com.example.demo.Util.JwtUtil;
import com.example.demo.Repository.AlumnoRepository; // Necesario para la redirección
import com.example.demo.Repository.ProfesorRepository; // Necesario para la redirección
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Collections;

// Clase que intercepta el POST /login para generar el token
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtil jwtUtil;
    private final ProfesorRepository profesorRepository;
    private final AlumnoRepository alumnoRepository;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil, ProfesorRepository profesorRepository, AlumnoRepository alumnoRepository) {
        super(authenticationManager);
        this.jwtUtil = jwtUtil;
        this.profesorRepository = profesorRepository;
        this.alumnoRepository = alumnoRepository;
        // ⭐ CRÍTICO: Especifica que este filtro actúa en POST /login
        setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/login", "POST")); 
    }

    // Intenta autenticar al usuario usando el AuthenticationManager
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // Lectura directa de los campos del formulario
            String username = request.getParameter(getUsernameParameter());
            String password = request.getParameter(getPasswordParameter());

            if (username == null || password == null) {
                 throw new BadCredentialsException("Credenciales incompletas.");
            }

            // Realiza la autenticación
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(username, password, Collections.emptyList())
            );
        } catch (Exception e) {
            throw new BadCredentialsException("Credenciales inválidas.", e);
        }
    }

    // Autenticación exitosa: Genera el JWT y lo guarda en una cookie
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        UserDetails userDetails = (UserDetails) authResult.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        // 1. Guarda el JWT en una cookie HTTP-Only (Seguridad)
        Cookie jwtCookie = new Cookie("Authorization", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        response.addCookie(jwtCookie);

        // 2. Redirige según el rol (Similar a tu lógica original)
        String targetUrl = determineTargetUrl(authResult);
        response.sendRedirect(targetUrl);
    }
    
    // Autenticación fallida: Redirige al login con error
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        response.sendRedirect(request.getContextPath() + "/login?error=true");
    }

    // Determina la URL de destino final y busca el ID necesario para la ruta
    private String determineTargetUrl(Authentication authentication) {
        String username = authentication.getName(); 
        
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PROFESOR"))) {
            Long profesorId = profesorRepository.findByCodigoProfesor(username)
                .map(p -> p.getId())
                .orElseThrow(() -> new RuntimeException("Error fatal: ID de Profesor no encontrado."));
            return "/profesor/cursos/" + profesorId;
            
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ALUMNO"))) {
            Long alumnoId = alumnoRepository.findByCodigoAlumno(username)
                .map(a -> a.getId())
                .orElseThrow(() -> new RuntimeException("Error fatal: ID de Alumno no encontrado."));
            return "/alumno/cursos/" + alumnoId;
        }
        return "/login?error=true";
    }
}
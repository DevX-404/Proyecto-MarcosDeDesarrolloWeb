package com.example.demo.Security;

import com.example.demo.Model.Alumno;
import com.example.demo.Model.Profesor;
import com.example.demo.Repository.AlumnoRepository;
import com.example.demo.Repository.ProfesorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        // 1. Buscar como Profesor
        Optional<Profesor> profesorOpt = profesorRepository.findByCodigoProfesor(username);
        if (profesorOpt.isPresent()) {
            Profesor profesor = profesorOpt.get();
            return User.builder()
                    .username(profesor.getCodigoProfesor())
                    .password(profesor.getPassword())
                    .roles(profesor.getRole().replace("ROLE_", "")) // PROFESOR
                    .build();
        }

        // 2. Buscar como Alumno
        Optional<Alumno> alumnoOpt = alumnoRepository.findByCodigoAlumno(username);
        if (alumnoOpt.isPresent()) {
            Alumno alumno = alumnoOpt.get();
            return User.builder()
                    .username(alumno.getCodigoAlumno())
                    .password(alumno.getPassword())
                    .roles(alumno.getRole().replace("ROLE_", "")) // ALUMNO
                    .build();
        }

        throw new UsernameNotFoundException("Usuario no encontrado: " + username);
    }
}
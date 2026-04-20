package com.app.fisiolab_system.service;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.app.fisiolab_system.model.Usuario;
import com.app.fisiolab_system.repository.UsuarioRepository;

/**
 * Implementation of UserDetailService.
 * 
 * This class is responsible for loading user data from the database 
 * when Spring Security needs to authenticate a user.
 */
@Service
public class UserDetailServiceImpl implements UserDetailService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Constructor with dependency injection.
     * Receives the repository to query users from the database.
     */
    public UserDetailServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Main method used by Spring Security.
     * This method is called automatically during the login process.
     * 
     * It searches for the user by username (in our case, we use email)
     * and converts the user entity into a UserDetails object that 
     * Spring Security can understand and use for authentication.
     *
     * @param username In our case, this is the user's email
     * @return UserDetails object with authentication information
     * @throws UsernameNotFoundException If the user is not found in the database
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        // Step 1: Search for the user in the database using their email
        Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Step 2: Build the UserDetails object that Spring Security requires
        return org.springframework.security.core.userdetails.User.builder()
                // Step 2.1: Set the username (we use email as username)
                .username(usuario.getEmail())
                
                // Step 2.2: Set the encrypted password from the database
                .password(usuario.getPasswordHash())
                
                // Step 2.3: Assign user roles/authorities
                .authorities(List.of(
                    // Important: Spring Security expects roles to start with "ROLE_"
                    new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())
                ))
                .build();
    }
   
}
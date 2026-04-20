package com.app.fisiolab_system.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.app.fisiolab_system.model.Usuario;

// 1. Define a repository interface for the Usuario entity
//    It extends JpaRepository to inherit CRUD operations (Create, Read, Update, Delete)
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // 2. Find a user by email
    //    Returns an Optional to handle cases where the user may not exist
    Optional<Usuario> findByEmail(String email);

    // 3. Find a user by "cedula" (national ID)
    //    Also returns an Optional for safe null handling
    Optional<Usuario> findByCedula(String cedula);

    // 4. Check if a user exists by email
    //    Returns true if a user with the given email already exists
    boolean existsByEmail(String email);

    // 5. Check if a user exists by cedula
    //    Useful for validation before registration
    boolean existsByCedula(String cedula);

    // 6. Check if a user exists by "codigoRegistro" (registration code)
    //    This can be used to ensure unique registration codes for users
    boolean existsByCodigoRegistro(String codigoRegistro);

    // 7. Find a user by ID
    //    This is a common method to retrieve a user by their unique identifier
    Optional<Usuario> findById(Long id);

    List<Usuario> findByActivoTrue();
    List<Usuario> findByActivoFalse();
    List<Usuario> findByBloqueadoHastaAfter(LocalDateTime fecha);
}
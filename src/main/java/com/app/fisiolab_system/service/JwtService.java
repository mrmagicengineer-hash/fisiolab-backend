package com.app.fisiolab_system.service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.app.fisiolab_system.model.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Service responsible for generating and validating JWT tokens.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    // ===================================================================
    // 1. TOKEN GENERATION
    // ===================================================================

    /**
     * Generates a JWT token for the given user.
     *
     * @param usuario The user entity containing email, id, and role.
     * @return A signed JWT token as a String.
     */
    public String generateToken(Usuario usuario) {
        
        return Jwts.builder()
                // Step 1.1: Set the subject (usually the user's email or username)
                .subject(usuario.getEmail())
                
                // Step 1.2: Add custom claims (extra information stored in the token)
                .claims(Map.of(
                    "userId", usuario.getId(),           // Store user ID
                    "rol", usuario.getRol().name()       // Store user role as String
                ))
                
                // Step 1.3: Set token issuance time
                .issuedAt(new Date())
                
                // Step 1.4: Set token expiration time
                .expiration(new Date(System.currentTimeMillis() + expiration))
                
                // Step 1.5: Sign the token with the secret key using HS256 algorithm
                .signWith(getSigningKey())
                
                // Step 1.6: Build the final compact token (header.payload.signature)
                .compact();
    }

    // ===================================================================
    // 2. CLAIM EXTRACTION
    // ===================================================================

    /**
     * Extracts the username (subject) from the JWT token.
     *
     * @param token The JWT token
     * @return The username stored in the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Generic method to extract any claim from the token.
     *
     * @param token          The JWT token
     * @param claimsResolver Function that specifies which claim to extract
     * @return The value of the requested claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // ===================================================================
    // 3. TOKEN VALIDATION
    // ===================================================================

    /**
     * Validates if the token is valid and belongs to the given user.
     *
     * @param token        The JWT token to validate
     * @param userDetails  User details loaded from the database
     * @return true if the token is valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        
        // Token is valid if:
        // 1. The username matches
        // 2. The token has not expired
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Checks if the token has expired.
     *
     * @param token The JWT token
     * @return true if expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // ===================================================================
    // 4. INTERNAL HELPER METHODS
    // ===================================================================

    /**
     * Extracts all claims (payload) from the JWT token.
     * This method also verifies the token signature.
     *
     * @param token The JWT token
     * @return All claims contained in the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                // Verify the signature using the same secret key
                .verifyWith((javax.crypto.SecretKey) getSigningKey())
                .build()
                // Parse the token and get the payload
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Generates the signing key from the configured secret.
     * Used both for signing and verifying tokens.
     *
     * @return SecretKey for HS256 algorithm
     */
    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
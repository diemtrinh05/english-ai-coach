package com.example.englishaicoach.auth;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findByAuthProviderAndProviderUserId(AuthProvider authProvider,
            String providerUserId);
}

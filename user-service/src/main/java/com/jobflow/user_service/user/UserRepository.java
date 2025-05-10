package com.jobflow.user_service.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLogin(String login);

    Optional<User> findByAuthProviderAndAuthProviderId(AuthProvider provider, String authProviderId);

    boolean existsByLogin(String login);

}

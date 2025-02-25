package app.user.repository;

import app.user.model.*;
import org.springframework.stereotype.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);
    //List<User> findByEmail(String email);
    Optional<User> findByEmail(String email);
}
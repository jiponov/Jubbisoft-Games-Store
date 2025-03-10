package app.loyalty.repository;

import app.loyalty.model.*;
import app.user.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;


@Repository
public interface LoyaltyRepository extends JpaRepository<Loyalty, UUID> {

    // Търси по "member" на (User)
    Optional<Loyalty> findByMemberId(UUID memberId);
}
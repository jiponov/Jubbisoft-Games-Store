package app.game.repository;

import app.game.model.*;
import app.user.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;


@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {

    Optional<Game> findByTitle(String title);

    List<Game> findAllByPublisherIdOrderByReleaseDateDesc(UUID publisherId);

    List<Game> findAllByIsAvailableTrueOrderByReleaseDateDesc();

    List<Game> findAllByPurchasedByUsersOrderByReleaseDateDesc(User user);
}
package app.treasury.repository;

import app.treasury.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;


@Repository
public interface TreasuryRepository extends JpaRepository<Treasury, UUID> {

    Optional<Treasury> findByName(String name);
}
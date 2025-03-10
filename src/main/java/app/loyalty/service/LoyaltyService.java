package app.loyalty.service;

import app.loyalty.model.*;
import app.loyalty.repository.*;
import app.shared.exception.*;
import app.user.model.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.util.*;


@Slf4j
@Service
public class LoyaltyService {

    private static final int PREMIUM_THRESHOLD = 2;         // След 2 покупки става PREMIUM
    private static final double DISCOUNT_PERCENTAGE = 0.3;  // 30% отстъпка за всяка следваща покупка

    private final LoyaltyRepository loyaltyRepository;


    @Autowired
    public LoyaltyService(LoyaltyRepository loyaltyRepository) {
        this.loyaltyRepository = loyaltyRepository;
    }


    public Loyalty getById(UUID id) {

        Loyalty loyalty = loyaltyRepository
                .findById(id)
                .orElseThrow(() -> new DomainException("Loyalty with id [%s] does not exist.".formatted(id)));

        return loyalty;
    }


    // CREATE
    // Създаване на Loyalty за нов потребител
    public Loyalty createLoyalty(User member) {

        Loyalty loyalty = Loyalty.builder()
                .member(member)
                .type(LoyaltyType.DEFAULT)
                .gamesPurchased(0)
                .build();

        return loyaltyRepository.save(loyalty);
    }


    // UPDATE
    // ОБНОВЯВАНЕ на Loyalty след покупка на игра
    public void updateLoyaltyAfterPurchase(User member) {

        Loyalty loyalty = getLoyaltyByUserId(member.getId());

        // Увеличаваме броя на покупките
        loyalty.setGamesPurchased(loyalty.getGamesPurchased() + 1);

        // Проверяваме дали трябва да стане PREMIUM
        if (loyalty.getGamesPurchased() >= PREMIUM_THRESHOLD) {
            loyalty.setType(LoyaltyType.PREMIUM);
        }

        loyaltyRepository.save(loyalty);
    }


    // GET
    // get-ваме Loyalty по userId
    public Loyalty getLoyaltyByUserId(UUID userId) {

        Loyalty loyalty = loyaltyRepository
                .findByMemberId(userId)
                .orElseThrow(() -> new DomainException("Loyalty record not found for user with id: " + userId));

        return loyalty;
    }


    // Проверка дали потребителят е:  PREMIUM member вече  ->  true/false
    public boolean hasPremiumDiscount(UUID userId) {

        Loyalty loyalty = getLoyaltyByUserId(userId);

        boolean isPremium = loyalty.getType() == LoyaltyType.PREMIUM;

        return isPremium;
    }


    // връща правилната отстъпка (0% или 30%)
    public double getDiscountPercentage(UUID userId) {

        if (hasPremiumDiscount(userId)) {
            return DISCOUNT_PERCENTAGE;     // DISCOUNT_PERCENTAGE = 0.3  ->  30% отстъпка за всяка следваща game
        } else {
            return 0.0;    // 0 процента отстъпка ако е DEFAULT LOYALTY
        }

        // alternative:
        // return hasPremiumDiscount(userId) ? DISCOUNT_PERCENTAGE : 0.0;
    }


}
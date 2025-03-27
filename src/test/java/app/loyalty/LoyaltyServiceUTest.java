package app.loyalty;

import app.loyalty.model.*;
import app.loyalty.repository.*;
import app.loyalty.service.*;
import app.shared.exception.*;
import app.user.model.*;
import app.user.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.loyalty.model.Loyalty;
import app.loyalty.model.LoyaltyType;
import app.loyalty.repository.LoyaltyRepository;
import app.loyalty.service.LoyaltyService;
import app.shared.exception.DomainException;
import app.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class LoyaltyServiceUTest {

    private static final int PREMIUM_THRESHOLD = 2;         // След 2 покупки става PREMIUM
    private static final double DISCOUNT_PERCENTAGE = 0.3;  // 30% отстъпка за всяка следваща покупка
    @Mock
    private LoyaltyRepository loyaltyRepository;

    @InjectMocks
    private LoyaltyService loyaltyService;


    // getById() - LoyaltyService
    //  Намира се Loyalty	=>   Връща обекта
    // Съществуващ id → връща обект
    @Test
    void givenExistingLoyaltyId_whenGetById_thenReturnLoyalty() {
        // Given
        UUID loyaltyId = UUID.randomUUID();
        Loyalty loyalty = Loyalty.builder().id(loyaltyId).build();

        when(loyaltyRepository.findById(loyaltyId)).thenReturn(Optional.of(loyalty));

        // When
        Loyalty result = loyaltyService.getById(loyaltyId);

        // Then
        assertNotNull(result);
        assertEquals(loyaltyId, result.getId());
        verify(loyaltyRepository).findById(loyaltyId);
    }


    // getById() - LoyaltyService
    //  Не се намира	->    Хвърля DomainException
    // Несъществуващ id → хвърля грешка
    @Test
    void givenNonExistingLoyaltyId_whenGetById_thenThrowException() {
        // Given
        UUID loyaltyId = UUID.randomUUID();
        when(loyaltyRepository.findById(loyaltyId)).thenReturn(Optional.empty());

        // When & Then
        DomainException exception = assertThrows(DomainException.class, () -> loyaltyService.getById(loyaltyId));
        assertTrue(exception.getMessage().contains("Loyalty with id"));
    }


    // createLoyalty() - LoyaltyService
    //  Входен потребител → връща запазен Loyalty	-  Полетата са коректни
    // Repository метод се извиква с очаквания обект	=>  Да се провери с ArgumentCaptor или .matches(...)


    @Test
    void givenUser_whenCreateLoyalty_thenSaveDefaultLoyalty() {
        // Given
        User user = User.builder().id(UUID.randomUUID()).username("lubo").build();

        Loyalty savedLoyalty = Loyalty.builder()
                .id(UUID.randomUUID())
                .member(user)
                .type(LoyaltyType.DEFAULT)
                .gamesPurchased(0)
                .build();

        when(loyaltyRepository.save(any(Loyalty.class))).thenReturn(savedLoyalty);

        // When
        Loyalty result = loyaltyService.createLoyalty(user);

        // Then
        assertNotNull(result);
        assertEquals(user, result.getMember());
        assertEquals(LoyaltyType.DEFAULT, result.getType());
        assertEquals(0, result.getGamesPurchased());

        ArgumentCaptor<Loyalty> loyaltyCaptor = ArgumentCaptor.forClass(Loyalty.class);
        verify(loyaltyRepository).save(loyaltyCaptor.capture());

        Loyalty captured = loyaltyCaptor.getValue();
        assertEquals(user, captured.getMember());
        assertEquals(LoyaltyType.DEFAULT, captured.getType());
        assertEquals(0, captured.getGamesPurchased());
    }


    // updateLoyaltyAfterPurchase() - LoyaltyService
    // < Threshold → увеличава броя, тип остава DEFAULT
    @Test
    void givenDefaultUser_whenUpdateLoyalty_thenIncrementGamesOnly() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();

        Loyalty loyalty = Loyalty.builder()
                .member(user)
                .gamesPurchased(1)
                .type(LoyaltyType.DEFAULT)
                .build();

        when(loyaltyRepository.findByMemberId(userId)).thenReturn(Optional.of(loyalty));

        // When
        loyaltyService.updateLoyaltyAfterPurchase(user);

        // Then
        assertEquals(2, loyalty.getGamesPurchased());
        assertEquals(LoyaltyType.PREMIUM, loyalty.getType()); // 2 == threshold
        verify(loyaltyRepository).save(loyalty);
    }


    // updateLoyaltyAfterPurchase() - LoyaltyService
    // >= Threshold → става PREMIUM
    @Test
    void givenUserAlreadyPremium_whenUpdateLoyalty_thenKeepPremiumStatus() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();

        Loyalty loyalty = Loyalty.builder()
                .member(user)
                .gamesPurchased(5)
                .type(LoyaltyType.PREMIUM)
                .build();

        when(loyaltyRepository.findByMemberId(userId)).thenReturn(Optional.of(loyalty));

        // When
        loyaltyService.updateLoyaltyAfterPurchase(user);

        // Then
        assertEquals(6, loyalty.getGamesPurchased());
        assertEquals(LoyaltyType.PREMIUM, loyalty.getType());
        verify(loyaltyRepository).save(loyalty);
    }


    // updateLoyaltyAfterPurchase() - LoyaltyService
    // Loyalty липсва → хвърля грешка
    @Test
    void givenMissingLoyalty_whenUpdate_thenThrowException() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();

        when(loyaltyRepository.findByMemberId(userId)).thenReturn(Optional.empty());

        // Then
        DomainException exception = assertThrows(DomainException.class,
                () -> loyaltyService.updateLoyaltyAfterPurchase(user));

        assertTrue(exception.getMessage().contains("Loyalty record not found"));
    }


    // getLoyaltyByUserId() - LoyaltyService
    // Лоялност съществува → връща я
    @Test
    void givenExistingUser_whenGetLoyaltyByUserId_thenReturnLoyalty() {
        // Given
        UUID userId = UUID.randomUUID();
        Loyalty loyalty = Loyalty.builder()
                .id(UUID.randomUUID())
                .gamesPurchased(2)
                .type(LoyaltyType.PREMIUM)
                .build();

        when(loyaltyRepository.findByMemberId(userId)).thenReturn(Optional.of(loyalty));

        // When
        Loyalty result = loyaltyService.getLoyaltyByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getGamesPurchased());
        assertEquals(LoyaltyType.PREMIUM, result.getType());
        verify(loyaltyRepository).findByMemberId(userId);
    }


    // getLoyaltyByUserId() - LoyaltyService
    // Лоялност липсва → хвърля DomainException
    @Test
    void givenMissingUserLoyalty_whenGetLoyaltyByUserId_thenThrowException() {
        // Given
        UUID userId = UUID.randomUUID();
        when(loyaltyRepository.findByMemberId(userId)).thenReturn(Optional.empty());

        // When & Then
        DomainException exception = assertThrows(DomainException.class,
                () -> loyaltyService.getLoyaltyByUserId(userId));

        assertTrue(exception.getMessage().contains("Loyalty record not found"));
    }


    // hasPremiumDiscount() - LoyaltyService
    // Loyalty  Типът е PREMIUM → връща true
    @Test
    void givenPremiumUser_whenHasPremiumDiscount_thenReturnTrue() {
        // Given
        UUID userId = UUID.randomUUID();
        Loyalty loyalty = Loyalty.builder()
                .type(LoyaltyType.PREMIUM)
                .build();

        when(loyaltyRepository.findByMemberId(userId)).thenReturn(Optional.of(loyalty));

        // When
        boolean result = loyaltyService.hasPremiumDiscount(userId);

        // Then
        assertTrue(result);
    }


    // hasPremiumDiscount() - LoyaltyService
    // Loyalty  Типът е DEFAULT → връща false
    @Test
    void givenDefaultUser_whenHasPremiumDiscount_thenReturnFalse() {
        // Given
        UUID userId = UUID.randomUUID();
        Loyalty loyalty = Loyalty.builder()
                .type(LoyaltyType.DEFAULT)
                .build();

        when(loyaltyRepository.findByMemberId(userId)).thenReturn(Optional.of(loyalty));

        // When
        boolean result = loyaltyService.hasPremiumDiscount(userId);

        // Then
        assertFalse(result);
    }


    // getDiscountPercentage() - LoyaltyService
    // PREMIUM потребител : PREMIUM → 0.3
    @Test
    void givenPremiumUser_whenGetDiscountPercentage_thenReturn30Percent() {
        // Given
        UUID userId = UUID.randomUUID();
        Loyalty loyalty = Loyalty.builder().type(LoyaltyType.PREMIUM).build();

        when(loyaltyRepository.findByMemberId(userId)).thenReturn(Optional.of(loyalty));

        // When
        double discount = loyaltyService.getDiscountPercentage(userId);

        // Then
        assertEquals(0.3, discount, 0.001);
    }


    // getDiscountPercentage() - LoyaltyService
    // DEFAULT потребител:	DEFAULT → 0.0
    @Test
    void givenDefaultUser_whenGetDiscountPercentage_thenReturnZero() {
        // Given
        UUID userId = UUID.randomUUID();
        Loyalty loyalty = Loyalty.builder().type(LoyaltyType.DEFAULT).build();

        when(loyaltyRepository.findByMemberId(userId)).thenReturn(Optional.of(loyalty));

        // When
        double discount = loyaltyService.getDiscountPercentage(userId);

        // Then
        assertEquals(0.0, discount, 0.001);
    }

}
package app.treasury;

import app.shared.exception.*;
import app.transaction.service.*;
import app.treasury.repository.*;
import app.treasury.service.*;
import app.user.service.*;
import app.wallet.service.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import app.treasury.model.Treasury;
import app.treasury.repository.TreasuryRepository;
import app.treasury.service.TreasuryService;
import app.user.model.User;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.wallet.service.WalletService;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import app.treasury.model.Treasury;
import app.shared.exception.DomainException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class TreasuryServiceUTest {

    @Mock
    private TreasuryRepository treasuryRepository;
    @Mock
    private UserService userService;
    @Mock
    private WalletService walletService;
    @Mock
    private TransactionService transactionService;


    @Spy
    @InjectMocks
    private TreasuryService treasuryService;


    // processTransaction()  -  TreasuryService
    // Успешна транзакция
    @Test
    void givenSufficientTreasuryAndActiveWallet_whenProcessTransaction_thenReturnsTrue() {
        // Given
        UUID userId = UUID.randomUUID();
        Treasury treasury = Treasury.builder()
                .name("Treasury vault")
                .balance(new BigDecimal("500.00"))
                .build();

        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .balance(new BigDecimal("50.00"))
                .currency(Currency.getInstance("EUR"))
                .status(app.wallet.model.WalletStatus.ACTIVE)
                .build();

        User user = User.builder()
                .id(userId)
                .wallet(wallet)
                .username("user123")
                .build();

        Transaction transaction = Transaction.builder()
                .status(TransactionStatus.APPROVED)
                .build();

        when(treasuryRepository.findByName("Treasury vault")).thenReturn(Optional.of(treasury));
        when(userService.getById(userId)).thenReturn(user);
        when(walletService.addFunds(wallet.getId(), new BigDecimal("100.00"))).thenReturn(transaction);

        // When
        boolean result = treasuryService.processTransaction(userId);

        // Then
        assertTrue(result);
        verify(treasuryRepository).save(treasury);
    }

    // processTransaction()  -  TreasuryService
    // Недостатъчен баланс в Treasury
    @Test
    void givenInsufficientTreasuryBalance_whenProcessTransaction_thenReturnsFalse() {
        // Given
        UUID userId = UUID.randomUUID();
        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.ZERO)
                .currency(Currency.getInstance("EUR"))
                .build();

        User user = User.builder()
                .id(userId)
                .wallet(wallet)
                .username("user123")
                .build();

        // 👇 ВАЖНО: свържи обратно
        wallet.setOwner(user);

        Treasury treasury = Treasury.builder()
                .name("Treasury vault")
                .balance(new BigDecimal("10.00")) // по-малко от 100
                .build();

        when(treasuryRepository.findByName("Treasury vault")).thenReturn(Optional.of(treasury));
        when(userService.getById(userId)).thenReturn(user);
        when(transactionService.createNewTransaction(
                eq(user),
                anyString(),
                anyString(),
                any(),
                any(),
                any(),
                eq(TransactionType.DEPOSIT),
                eq(TransactionStatus.FAILED),
                any(),
                any()
        )).thenReturn(Transaction.builder().status(TransactionStatus.FAILED).build());

        // When
        boolean result = treasuryService.processTransaction(userId);

        // Then
        assertFalse(result);
        verify(transactionService, times(1)).createNewTransaction(
                eq(user),
                anyString(),
                anyString(),
                any(),
                any(),
                any(),
                eq(TransactionType.DEPOSIT),
                eq(TransactionStatus.FAILED),
                any(),
                any()
        );
    }


    // processTransaction()  -  TreasuryService
    // Wallet е INACTIVE → транзакцията е failed
    @Test
    void givenInactiveWalletTransaction_whenProcessTransaction_thenReturnsFalse() {
        // Given
        UUID userId = UUID.randomUUID();
        Treasury treasury = Treasury.builder()
                .name("Treasury vault")
                .balance(new BigDecimal("500.00"))
                .build();

        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .balance(new BigDecimal("0"))
                .currency(Currency.getInstance("EUR"))
                .build();

        User user = User.builder()
                .id(userId)
                .wallet(wallet)
                .username("user123")
                .build();

        Transaction failedTransaction = Transaction.builder()
                .status(TransactionStatus.FAILED)
                .build();

        when(treasuryRepository.findByName("Treasury vault")).thenReturn(Optional.of(treasury));
        when(userService.getById(userId)).thenReturn(user);
        when(walletService.addFunds(wallet.getId(), new BigDecimal("100.00"))).thenReturn(failedTransaction);

        // When
        boolean result = treasuryService.processTransaction(userId);

        // Then
        assertFalse(result);
    }


    // giveMoneyToUser()  -  TreasuryService
    // Успешна транзакция → не хвърля грешка
    @Test
    void givenSuccessfulTransaction_whenGiveMoneyToUser_thenNoExceptionThrown() {
        // Given
        UUID userId = UUID.randomUUID();
        doReturn(true).when(treasuryService).processTransaction(userId);

        // When / Then
        assertDoesNotThrow(() -> treasuryService.giveMoneyToUser(userId));

        // Verify interaction
        verify(treasuryService, times(1)).processTransaction(userId);
    }


    // giveMoneyToUser()  -  TreasuryService
    // Неуспешна транзакция → хвърля TreasuryIsEmptyException
    @Test
    void givenFailedTransaction_whenGiveMoneyToUser_thenThrowsTreasuryIsEmptyException() {
        UUID userId = UUID.randomUUID();

        doReturn(false).when(treasuryService).processTransaction(userId);

        TreasuryIsEmptyException ex = assertThrows(TreasuryIsEmptyException.class,
                () -> treasuryService.giveMoneyToUser(userId));

        assertEquals("Treasury does not have enough funds or Inactive wallet", ex.getMessage());
        verify(treasuryService).processTransaction(userId);
    }



    // getByName()  -  TreasuryService
    // Успешно намиране на Treasury
    @Test
    void givenExistingTreasury_whenGetByName_thenReturnsTreasury() {
        // Given
        String name = "Treasury vault";
        Treasury mockTreasury = Treasury.builder()
                .name(name)
                .balance(BigDecimal.valueOf(500.00))
                .currency(Currency.getInstance("EUR"))
                .build();

        when(treasuryRepository.findByName(name)).thenReturn(Optional.of(mockTreasury));

        // When
        Treasury result = treasuryService.getByName(name);

        // Then
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(BigDecimal.valueOf(500.00), result.getBalance());
        verify(treasuryRepository).findByName(name);
    }


    // getByName()  -  TreasuryService
    // Treasury не съществува → хвърля се DomainException
    @Test
    void givenNonExistentTreasury_whenGetByName_thenThrowsDomainException() {
        // Given
        String name = "Treasury vault";
        when(treasuryRepository.findByName(name)).thenReturn(Optional.empty());

        // When / Then
        DomainException ex = assertThrows(DomainException.class, () -> treasuryService.getByName(name));
        assertTrue(ex.getMessage().contains("Treasury with this name [Treasury vault] does not exist."));
        verify(treasuryRepository).findByName(name);
    }

    // initializeTreasury()  -  TreasuryService
    // Treasury все още не съществува → инициализира се нова
    @Test
    void givenTreasuryDoesNotExist_whenInitializeTreasury_thenTreasuryIsSaved() {
        // Given
        when(treasuryRepository.findByName("Treasury vault")).thenReturn(Optional.empty());

        // When
        treasuryService.initializeTreasury();

        // Then
        verify(treasuryRepository).save(argThat(t ->
                t.getName().equals("Treasury vault") &&
                        t.getBalance().compareTo(BigDecimal.valueOf(1000.00)) == 0 &&
                        t.getCurrency().equals(Currency.getInstance("EUR"))
        ));
    }



    // initializeTreasury()  -  TreasuryService
    // Treasury вече съществува → не се прави нищо
    @Test
    void givenTreasuryAlreadyExists_whenInitializeTreasury_thenTreasuryIsNotSavedAgain() {
        // Given
        Treasury existingTreasury = new Treasury();
        existingTreasury.setName("Treasury vault");

        when(treasuryRepository.findByName("Treasury vault")).thenReturn(Optional.of(existingTreasury));

        // When
        treasuryService.initializeTreasury();

        // Then
        verify(treasuryRepository, never()).save(any());
    }


}
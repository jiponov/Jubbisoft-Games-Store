package app.wallet;

import app.shared.exception.*;
import app.transaction.model.*;
import app.transaction.service.*;
import app.user.model.*;
import app.wallet.model.*;
import app.wallet.repository.*;
import app.wallet.service.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;

import java.util.*;

import app.transaction.service.TransactionService;
import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import app.wallet.repository.WalletRepository;
import app.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class WalletServiceUTest {


    private static final String JUBBISOFT_LTD = "Jubbisoft Ltd.";

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private TransactionService transactionService;


    @InjectMocks
    private WalletService walletService;


    // createNewWallet()  -  WalletService
    // Валиден потребител ->  Създава се и се връща wallet, и се save-ва правилно
    @Test
    void givenValidUser_whenCreateNewWallet_thenWalletIsCreatedAndSaved() {
        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("Lub123")
                .build();

        Wallet walletToSave = Wallet.builder()
                .owner(user)
                .status(WalletStatus.ACTIVE)
                .balance(new BigDecimal("100.00"))
                .currency(Currency.getInstance("EUR"))
                .build();

        Wallet savedWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .status(WalletStatus.ACTIVE)
                .balance(new BigDecimal("100.00"))
                .currency(Currency.getInstance("EUR"))
                .build();

        when(walletRepository.save(any(Wallet.class))).thenReturn(savedWallet);

        // When
        Wallet result = walletService.createNewWallet(user);

        // Then
        assertNotNull(result);
        assertEquals(savedWallet.getId(), result.getId());
        assertEquals(user, result.getOwner());
        assertEquals(WalletStatus.ACTIVE, result.getStatus());
        assertEquals(new BigDecimal("100.00"), result.getBalance());

        verify(walletRepository, times(1)).save(any(Wallet.class));
    }


    // addFunds()  -  WalletService
    // 1. Wallet е ACTIVE -	Успешно добавяне на средства, wallet е обновен, връща APPROVED
    // Активен портфейл – средства се добавят успешно
    @Test
    void givenActiveWallet_whenAddFunds_thenTransactionIsApprovedAndWalletUpdated() {
        // Given
        UUID walletId = UUID.randomUUID();
        BigDecimal initialBalance = new BigDecimal("100.00");
        BigDecimal amountToAdd = new BigDecimal("50.00");

        Wallet wallet = Wallet.builder()
                .id(walletId)
                .status(WalletStatus.ACTIVE)
                .balance(initialBalance)
                .currency(Currency.getInstance("EUR"))
                .owner(User.builder().id(UUID.randomUUID()).build())
                .build();

        Transaction mockTransaction = new Transaction(); // or build a mock

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionService.createNewTransaction(
                eq(wallet.getOwner()),
                eq("Jubbisoft Ltd."),
                eq(walletId.toString()),
                eq(amountToAdd),
                any(BigDecimal.class),
                eq(wallet.getCurrency()),
                eq(TransactionType.DEPOSIT),
                eq(TransactionStatus.APPROVED),
                anyString(),
                isNull()
        )).thenReturn(mockTransaction);

        // When
        Transaction result = walletService.addFunds(walletId, amountToAdd);

        // Then
        assertEquals(mockTransaction, result);
        assertEquals(new BigDecimal("150.00"), wallet.getBalance()); // balance is updated
        verify(walletRepository, times(1)).save(wallet);
    }


    // addFunds()  -  WalletService
    // 2. Wallet е INACTIVE - Нищо не се добавя, транзакцията е FAILED
    // Неактивен портфейл – транзакцията се проваля
    @Test
    void givenInactiveWallet_whenAddFunds_thenTransactionIsFailed() {
        // Given
        UUID walletId = UUID.randomUUID();
        BigDecimal amountToAdd = new BigDecimal("50.00");

        Wallet wallet = Wallet.builder()
                .id(walletId)
                .status(WalletStatus.INACTIVE)
                .balance(new BigDecimal("200.00"))
                .currency(Currency.getInstance("EUR"))
                .owner(User.builder().id(UUID.randomUUID()).build())
                .build();

        Transaction failedTransaction = new Transaction(); // or build a mock

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionService.createNewTransaction(
                eq(wallet.getOwner()),
                eq("Jubbisoft Ltd."),
                eq(walletId.toString()),
                eq(amountToAdd),
                eq(wallet.getBalance()),
                eq(wallet.getCurrency()),
                eq(TransactionType.DEPOSIT),
                eq(TransactionStatus.FAILED),
                anyString(),
                eq("Inactive wallet")
        )).thenReturn(failedTransaction);

        // When
        Transaction result = walletService.addFunds(walletId, amountToAdd);

        // Then
        assertEquals(failedTransaction, result);
        verify(walletRepository, never()).save(any());
    }


    // addFunds()  -  WalletService
    // 3. Wallet не съществува - Хвърля се DomainException от getWalletById() (по избор)
    // Несъществуващ портфейл – хвърля се изключение
    @Test
    void givenInvalidWalletId_whenAddFunds_thenThrowsDomainException() {
        // Given
        UUID walletId = UUID.randomUUID();
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DomainException.class, () -> walletService.addFunds(walletId, BigDecimal.TEN));
        verify(walletRepository, never()).save(any());
        verify(transactionService, never()).createNewTransaction(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }


    // charge()  -  WalletService
    // 1. Активен wallet с достатъчно баланс -> Успешно теглене (APPROVED)
    @Test
    void givenActiveWalletWithEnoughBalance_whenCharge_thenTransactionIsApproved() {
        // Given
        UUID walletId = UUID.randomUUID();
        BigDecimal amountToCharge = new BigDecimal("50.00");

        User user = User.builder().id(UUID.randomUUID()).username("Lub123").build();

        Wallet wallet = Wallet.builder()
                .id(walletId)
                .status(WalletStatus.ACTIVE)
                .balance(new BigDecimal("100.00"))
                .currency(Currency.getInstance("EUR"))
                .owner(user)
                .build();

        Transaction approvedTransaction = new Transaction();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionService.createNewTransaction(
                eq(user),
                eq(walletId.toString()),
                eq("Jubbisoft Ltd."),
                eq(amountToCharge),
                eq(new BigDecimal("50.00")),
                eq(wallet.getCurrency()),
                eq(TransactionType.WITHDRAWAL),
                eq(TransactionStatus.APPROVED),
                anyString(),
                isNull()
        )).thenReturn(approvedTransaction);

        // When
        Transaction result = walletService.charge(user, walletId, amountToCharge, "Buy game");

        // Then
        assertEquals(approvedTransaction, result);
        assertEquals(new BigDecimal("50.00"), wallet.getBalance());
        verify(walletRepository, times(1)).save(wallet);
    }


    // charge()  -  WalletService
    // 2. Активен wallet с недостатъчен баланс - FAILED
    // FAILED – недостатъчен баланс
    @Test
    void givenActiveWalletWithInsufficientBalance_whenCharge_thenTransactionIsFailed() {
        // Given
        UUID walletId = UUID.randomUUID();
        BigDecimal amountToCharge = new BigDecimal("150.00");

        User user = User.builder().id(UUID.randomUUID()).build();

        Wallet wallet = Wallet.builder()
                .id(walletId)
                .status(WalletStatus.ACTIVE)
                .balance(new BigDecimal("100.00"))
                .currency(Currency.getInstance("EUR"))
                .owner(user)
                .build();

        Transaction failedTransaction = new Transaction();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionService.createNewTransaction(
                eq(user),
                eq(walletId.toString()),
                eq("Jubbisoft Ltd."),
                eq(amountToCharge),
                eq(wallet.getBalance()),
                eq(wallet.getCurrency()),
                eq(TransactionType.WITHDRAWAL),
                eq(TransactionStatus.FAILED),
                anyString(),
                eq("Not enough balance to purchase this game!")
        )).thenReturn(failedTransaction);

        // When
        Transaction result = walletService.charge(user, walletId, amountToCharge, "Buy expensive game");

        // Then
        assertEquals(failedTransaction, result);
        verify(walletRepository, never()).save(any());
    }


    // charge()  -  WalletService
    // 3. Wallet е INACTIVE, независимо от баланса - FAILED
    // FAILED – wallet INACTIVE
    @Test
    void givenInactiveWallet_whenCharge_thenTransactionIsFailed() {
        // Given
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("30.00");

        User user = User.builder().id(UUID.randomUUID()).build();

        Wallet wallet = Wallet.builder()
                .id(walletId)
                .status(WalletStatus.INACTIVE)
                .balance(new BigDecimal("100.00"))
                .currency(Currency.getInstance("EUR"))
                .owner(user)
                .build();

        Transaction failedTransaction = new Transaction();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionService.createNewTransaction(
                eq(user),
                eq(walletId.toString()),
                eq("Jubbisoft Ltd."),
                eq(amount),
                eq(wallet.getBalance()),
                eq(wallet.getCurrency()),
                eq(TransactionType.WITHDRAWAL),
                eq(TransactionStatus.FAILED),
                anyString(),
                eq("Inactive wallet status")
        )).thenReturn(failedTransaction);

        // When
        Transaction result = walletService.charge(user, walletId, amount, "Try to charge inactive wallet");

        // Then
        assertEquals(failedTransaction, result);
        verify(walletRepository, never()).save(any());
    }


    // charge()  -  WalletService
    // 4. Wallet не съществува - DomainException (по избор) хвърля се изключение
    @Test
    void givenInvalidWalletId_whenCharge_thenThrowsDomainException() {
        // Given
        UUID walletId = UUID.randomUUID();
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DomainException.class, () ->
                walletService.charge(new User(), walletId, BigDecimal.TEN, "Invalid test"));

        verify(transactionService, never()).createNewTransaction(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }


    // saveWallet()  -  WalletService
    // Валиден Wallet обект	 =>  Методът извиква save(), и връща това, което save() връща
    // Валиден wallet – проверява save() и връщане
    @Test
    void givenValidWallet_whenSaveWallet_thenWalletIsSavedAndReturned() {
        // Given
        Wallet walletToSave = Wallet.builder()
                .id(UUID.randomUUID())
                .balance(new BigDecimal("200.00"))
                .currency(Currency.getInstance("EUR"))
                .status(WalletStatus.ACTIVE)
                .build();

        Wallet savedWallet = Wallet.builder()
                .id(walletToSave.getId())
                .balance(walletToSave.getBalance())
                .currency(walletToSave.getCurrency())
                .status(walletToSave.getStatus())
                .build();

        when(walletRepository.save(walletToSave)).thenReturn(savedWallet);

        // When
        Wallet result = walletService.saveWallet(walletToSave);

        // Then
        assertNotNull(result);
        assertEquals(savedWallet.getId(), result.getId());
        assertEquals(savedWallet.getBalance(), result.getBalance());
        assertEquals(savedWallet.getCurrency(), result.getCurrency());

        verify(walletRepository, times(1)).save(walletToSave);
    }



}
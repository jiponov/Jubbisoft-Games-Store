package app.transaction;

import app.transaction.model.*;
import app.transaction.repository.*;
import app.transaction.service.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import app.shared.exception.DomainException;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import app.transaction.repository.TransactionRepository;
import app.transaction.service.TransactionService;
import app.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TransactionServiceUTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;


    // createNewTransaction()  -  TransactionService
    // Подадени коректни параметри → връща нов Transaction
    @Test
    void givenValidInput_whenCreateNewTransaction_thenTransactionIsSaved() {
        // Given
        User user = User.builder().id(UUID.randomUUID()).build();
        String sender = "Wallet123";
        String receiver = "Jubbisoft";
        BigDecimal amount = new BigDecimal("25.00");
        BigDecimal balanceLeft = new BigDecimal("75.00");
        Currency currency = Currency.getInstance("EUR");
        TransactionType type = TransactionType.WITHDRAWAL;
        TransactionStatus status = TransactionStatus.APPROVED;
        String description = "Purchase of game";
        String failureReason = null;

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        // When
        Transaction result = transactionService.createNewTransaction(
                user, sender, receiver, amount, balanceLeft, currency, type, status, description, failureReason
        );

        // Then
        verify(transactionRepository).save(captor.capture());
        Transaction saved = captor.getValue();

        assertEquals(user, saved.getOwner());
        assertEquals(sender, saved.getSender());
        assertEquals(receiver, saved.getReceiver());
        assertEquals(amount, saved.getAmount());
        assertEquals(balanceLeft, saved.getBalanceLeft());
        assertEquals(currency, saved.getCurrency());
        assertEquals(type, saved.getType());
        assertEquals(status, saved.getStatus());
        assertEquals(description, saved.getDescription());
        assertNull(saved.getFailureReason());
        assertNotNull(saved.getCreatedOn());

        assertEquals(saved, result);      // трябва да върне точно същия обект
    }

    // getAllByOwnerId()  -  TransactionService
    // Съществуват транзакции за даден потребител → връща списък
    @Test
    void givenTransactionsExist_whenGetAllByOwnerId_thenReturnList() {
        // Given
        UUID ownerId = UUID.randomUUID();
        List<Transaction> transactions = List.of(
                Transaction.builder().id(UUID.randomUUID()).build(),
                Transaction.builder().id(UUID.randomUUID()).build()
        );

        when(transactionRepository.findAllByOwnerIdOrderByCreatedOnDesc(ownerId)).thenReturn(transactions);

        // When
        List<Transaction> result = transactionService.getAllByOwnerId(ownerId);

        // Then
        assertEquals(2, result.size());
        verify(transactionRepository, times(1)).findAllByOwnerIdOrderByCreatedOnDesc(ownerId);
    }

    // getAllByOwnerId()  -  TransactionService
    // Няма транзакции → връща празен списък
    @Test
    void givenNoTransactions_whenGetAllByOwnerId_thenReturnEmptyList() {
        // Given
        UUID ownerId = UUID.randomUUID();
        when(transactionRepository.findAllByOwnerIdOrderByCreatedOnDesc(ownerId)).thenReturn(List.of());

        // When
        List<Transaction> result = transactionService.getAllByOwnerId(ownerId);

        // Then
        assertTrue(result.isEmpty());
        verify(transactionRepository, times(1)).findAllByOwnerIdOrderByCreatedOnDesc(ownerId);
    }

    // getById()  -  TransactionService
    // Транзакция съществува → връща я
    @Test
    void givenExistingTransaction_whenGetById_thenReturnTransaction() {
        // Given
        UUID transactionId = UUID.randomUUID();
        Transaction transaction = Transaction.builder()
                .id(transactionId)
                .description("Game purchase")
                .build();

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        // When
        Transaction result = transactionService.getById(transactionId);

        // Then
        assertNotNull(result);
        assertEquals(transactionId, result.getId());
        assertEquals("Game purchase", result.getDescription());
        verify(transactionRepository).findById(transactionId);
    }

    // getById()  -  TransactionService
    // Не съществува → хвърля DomainException
    @Test
    void givenMissingTransaction_whenGetById_thenThrowException() {
        // Given
        UUID transactionId = UUID.randomUUID();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        // When & Then
        DomainException exception = assertThrows(DomainException.class, () -> transactionService.getById(transactionId));
        assertTrue(exception.getMessage().contains("Transaction with id"));
    }



}
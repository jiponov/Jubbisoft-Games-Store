package app.transaction.service;

import app.shared.exception.*;
import app.transaction.model.*;
import app.transaction.repository.*;
import app.user.model.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.math.*;
import java.time.*;
import java.util.*;


@Slf4j
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;


    @Autowired
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }


    // CREATE new Transaction
    public Transaction createNewTransaction(User owner, String sender, String receiver, BigDecimal transactionAmount, BigDecimal balanceLeft, Currency currency, TransactionType type, TransactionStatus status, String transactionDescription, String failureReason) {

        Transaction transaction = Transaction.builder()
                .owner(owner)
                .sender(sender)
                .receiver(receiver)
                .amount(transactionAmount)
                .balanceLeft(balanceLeft)
                .currency(currency)
                .type(type)
                .status(status)
                .description(transactionDescription)
                .failureReason(failureReason)
                .createdOn(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        return transaction;
    }


    public List<Transaction> getAllByOwnerId(UUID ownerId) {

        List<Transaction> transactions = transactionRepository.findAllByOwnerIdOrderByCreatedOnDesc(ownerId);

        return transactions;
    }


    public Transaction getById(UUID id) {

        Transaction transaction = transactionRepository
                .findById(id)
                .orElseThrow(() -> new DomainException("Transaction with id [%s] does not exist.".formatted(id)));

        return transaction;
    }
}
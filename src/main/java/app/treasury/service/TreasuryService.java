package app.treasury.service;

import app.transaction.model.*;
import app.transaction.model.Transaction;
import app.treasury.model.*;
import app.treasury.repository.*;
import app.shared.exception.*;
import app.transaction.service.*;
import app.user.model.*;
import app.user.service.*;
import app.wallet.model.*;
import app.wallet.service.*;

import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.*;
import java.time.*;
import java.util.*;


@Slf4j
@Service
public class TreasuryService {

    private final TreasuryRepository treasuryRepository;

    private final UserService userService;
    private final WalletService walletService;
    private final TransactionService transactionService;


    @Autowired
    public TreasuryService(TreasuryRepository treasuryRepository, UserService userService, WalletService walletService, TransactionService transactionService) {
        this.treasuryRepository = treasuryRepository;
        this.userService = userService;
        this.walletService = walletService;
        this.transactionService = transactionService;
    }


    // Методът обработва транзакцията и връща true ако е успешна, false ако е неуспешна
    @Transactional
    public boolean processTransaction(UUID userId) {

        // взимаме Treasury (единственото) в проекта ни
        Treasury treasury = treasuryRepository
                .findByName("Treasury vault")
                .orElseThrow(() -> new IllegalStateException("Treasury not initialized!"));

        BigDecimal newAmount = new BigDecimal("100.00");

        User user = userService.getById(userId);
        Wallet wallet = user.getWallet();

        String JUBBISOFT_LTD = "Jubbisoft Ltd.";

        if (treasury.getBalance().compareTo(newAmount) < 0) {
            log.error("FAILED TRANSACTION: Treasury does not have enough funds! User: {}, Wallet: {}", user.getUsername(), wallet.getId());

            String transactionFailedDescription = "Attempt to add funds: %.2f EUR".formatted(newAmount.doubleValue());

            // Създаване на FAILED Transaction
            Transaction failedTransaction = transactionService.createNewTransaction(
                    wallet.getOwner(),
                    JUBBISOFT_LTD,
                    wallet.getId().toString(),
                    newAmount,
                    wallet.getBalance(),
                    wallet.getCurrency(),
                    TransactionType.DEPOSIT,
                    TransactionStatus.FAILED,
                    transactionFailedDescription,
                    "Treasury does not have enough funds"
            );

            // Връща false, ако средствата не достигат
            return false;
        }


        // BigDecimal.valueOf(100)
        // BigDecimal newAmount = new BigDecimal("100.00");
        // Ако има достатъчно средства: Добавяме пари към Wallet-а и създаваме Transaction
        Transaction transaction = walletService.addFunds(wallet.getId(), newAmount);

        if (transaction.getStatus() == TransactionStatus.FAILED) {
            log.error("FAILED TRANSACTION: Wallet is INACTIVE! User: {}, Wallet: {}", user.getUsername(), wallet.getId());
            return false;
        }


        treasury.setBalance(treasury.getBalance().subtract(newAmount));
        treasuryRepository.save(treasury);

        // Връща true, ако транзакцията е успешна
        log.info("SUCCESS TRANSACTION: {} EUR added to user {} (Wallet: {})", newAmount, user.getUsername(), wallet.getId());

        return true;
    }


    // Този метод вика processTransaction и хвърля грешка само ако тя е FAILED
    public void giveMoneyToUser(UUID userId) {

        boolean isSuccess = processTransaction(userId);

        if (!isSuccess) {
            throw new TreasuryIsEmptyException("Treasury does not have enough funds or Inactive wallet");
        }
    }


    public Treasury getByName(String name) {

        Treasury treasury = treasuryRepository
                .findByName("Treasury vault")
                .orElseThrow(() -> new DomainException("Treasury with this name [%s] does not exist.".formatted("Treasury vault")));

        log.info("Retrieved Treasury: {} (Balance: {})", treasury.getName(), treasury.getBalance());

        return treasury;
    }


    @Transactional
    public void initializeTreasury() {

        if (treasuryRepository.findByName("Treasury vault").isEmpty()) {

            Treasury treasury = Treasury.builder()
                    .name("Treasury vault")
                    .balance(new BigDecimal("1000.00"))
                    .currency(Currency.getInstance("EUR"))
                    .createdOn(LocalDateTime.now())
                    .updatedOn(LocalDateTime.now())
                    .build();

            treasuryRepository.save(treasury);
            log.info("Treasury initialized with balance 1000 EUR.");

        } else {
            log.info("Treasury already exists. Skipping initialization.");
        }

    }

}

/*
public Treasury getByName(String name) {
        return treasuryRepository.findByName(name)
                .orElseThrow(() -> new DomainException("Treasury with name [%s] does not exist.".formatted(name)));
    }
*/
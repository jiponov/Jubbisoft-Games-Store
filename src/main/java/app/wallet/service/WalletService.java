package app.wallet.service;

import app.shared.exception.*;
import app.transaction.model.*;
import app.transaction.service.*;
import app.user.model.*;
import app.wallet.model.*;
import app.wallet.repository.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.math.*;
import java.time.*;
import java.util.*;


@Slf4j
@Service
public class WalletService {

    private static final String JUBBISOFT_LTD = "Jubbisoft Ltd.";

    private final WalletRepository walletRepository;

    private final TransactionService transactionService;


    @Autowired
    public WalletService(WalletRepository walletRepository, TransactionService transactionService) {

        this.walletRepository = walletRepository;
        this.transactionService = transactionService;
    }


    public Wallet createNewWallet(User user) {

        Wallet wallet = walletRepository.save(initializeWallet(user));
        log.info("Successfully create new wallet with id [%s] and bonus available balance [%.2f].".formatted(wallet.getId(), wallet.getBalance()));

        return wallet;
    }


    private Wallet initializeWallet(User user) {

        Wallet wallet = Wallet.builder()
                .owner(user)
                .status(WalletStatus.ACTIVE)
                .balance(new BigDecimal("100.00"))
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        return wallet;
    }


    @Transactional
    public Transaction addFunds(UUID walletId, BigDecimal amount) {

        Wallet wallet = getWalletById(walletId);

        String transactionDescription = "Added funds %.2f EUR".formatted(amount.doubleValue());


        if (wallet.getStatus() == WalletStatus.INACTIVE) {
            log.error("FAILED TRANSACTION: Wallet is INACTIVE! Wallet ID: %s".formatted(walletId));

            Transaction transaction = transactionService.createNewTransaction(
                    wallet.getOwner(),
                    JUBBISOFT_LTD,
                    walletId.toString(),
                    amount,
                    wallet.getBalance(),
                    wallet.getCurrency(),
                    TransactionType.DEPOSIT,
                    TransactionStatus.FAILED,
                    transactionDescription,
                    "Inactive wallet");

            return transaction;
        }


        // WalletStatus.ACTIVE  >>
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedOn(LocalDateTime.now());

        walletRepository.save(wallet);
        log.info("SUCCESS TRANSACTION: %.2f EUR added to Wallet %s (New Balance: %.2f)".formatted(amount.doubleValue(), walletId, wallet.getBalance()));

        Transaction transaction = transactionService.createNewTransaction(
                wallet.getOwner(),
                JUBBISOFT_LTD,
                walletId.toString(),
                amount,
                wallet.getBalance(),
                wallet.getCurrency(),
                TransactionType.DEPOSIT,
                TransactionStatus.APPROVED,
                transactionDescription,
                null);

        return transaction;
    }


    private Wallet getWalletById(UUID walletId) {

        return walletRepository
                .findById(walletId)
                .orElseThrow(() -> new DomainException("Wallet with id [%s] does not exist.".formatted(walletId)));
    }


    @Transactional
    public Transaction charge(User user, UUID walletId, BigDecimal amount, String description) {

        Wallet wallet = getWalletById(walletId);
        String failureReason = null;
        boolean isFailedTransaction = false;

        if (wallet.getStatus() == WalletStatus.INACTIVE) {
            failureReason = "Inactive wallet status";
            isFailedTransaction = true;
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            failureReason = "Not enough balance to purchase this game!";
            isFailedTransaction = true;
        }

        if (isFailedTransaction) {
            Transaction transaction = transactionService.createNewTransaction(
                    user,
                    wallet.getId().toString(),
                    JUBBISOFT_LTD,
                    amount,
                    wallet.getBalance(),
                    wallet.getCurrency(),
                    TransactionType.WITHDRAWAL,
                    TransactionStatus.FAILED,
                    description,
                    failureReason);

            return transaction;
        }


        // if success:
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedOn(LocalDateTime.now());

        walletRepository.save(wallet);

        Transaction transaction = transactionService.createNewTransaction(
                user,
                wallet.getId().toString(),
                JUBBISOFT_LTD,
                amount,
                wallet.getBalance(),
                wallet.getCurrency(),
                TransactionType.WITHDRAWAL,
                TransactionStatus.APPROVED,
                description,
                null);

        return transaction;
    }

    @Transactional
    public Wallet saveWallet(Wallet wallet) {
        Wallet savedWallet = walletRepository.save(wallet);
        return savedWallet;
    }
}
package app.user.service;

import app.user.model.*;
import app.user.repository.*;
import app.wallet.model.*;
import app.wallet.repository.*;
import app.web.dto.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.*;
import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.*;

import java.math.*;
import java.time.*;
import java.util.*;


// КОГАТО СТАРТИРА нашия APP ще се стартира и UserInit понеже ползва CommandLineRunner с run
@Component
public class UserInitialize implements CommandLineRunner {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletRepository walletRepository;


    @Autowired
    public UserInitialize(UserService userService, PasswordEncoder passwordEncoder, WalletRepository walletRepository, UserRepository userRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }


    @Override
    public void run(String... args) throws Exception {

        // save check че няма да регистрираме 2 пъти един и същи потребител при стартиране на APP
        if (!userService.getAllUsers().isEmpty()) {
            return;
        }

        // Създаваме Wallet
        Wallet wallet1 = Wallet.builder()
                .balance(new BigDecimal("100.00"))
                .status(WalletStatus.ACTIVE)
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        wallet1 = walletRepository.save(wallet1); // Запазваме Wallet, за да има ID

        // Създаваме User с вече създаден Wallet
        User admin1 = User.builder()
                .username("Lub123")
                .password(passwordEncoder.encode("123123"))
                .role(UserRole.ADMIN)
                .isActive(true)
                .country(Country.BULGARIA)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .wallet(wallet1)       // Свързваме User с вече създаден Wallet
                .build();

        userRepository.save(admin1);


        Wallet wallet2 = Wallet.builder()
                .balance(new BigDecimal("100.00"))
                .status(WalletStatus.ACTIVE)
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        wallet2 = walletRepository.save(wallet2); // Запазваме Wallet, за да има ID

        // Създаваме User с вече създаден Wallet
        User user1 = User.builder()
                .username("Jin123")
                .password(passwordEncoder.encode("123123"))
                .role(UserRole.USER)
                .isActive(true)
                .country(Country.BULGARIA)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .wallet(wallet2)       // Свързваме User с вече създаден Wallet
                .build();

        userRepository.save(user1);


        Wallet wallet3 = Wallet.builder()
                .balance(new BigDecimal("100.00"))
                .status(WalletStatus.ACTIVE)
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        wallet3 = walletRepository.save(wallet3); // Запазваме Wallet, за да има ID

        // Създаваме User с вече създаден Wallet
        User user2 = User.builder()
                .username("Ivan123")
                .password(passwordEncoder.encode("123123"))
                .role(UserRole.USER)
                .isActive(true)
                .country(Country.BULGARIA)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .wallet(wallet3)       // Свързваме User с вече създаден Wallet
                .build();

        userRepository.save(user2);

    }

}



/* 2 начин:

        RegisterRequest registerRequest = new RegisterRequest();
                registerRequest.setUsername("Vik123");
                registerRequest.setPassword("123123");
                registerRequest.setCountry(Country.BULGARIA);

        userService.register(registerRequest);

        */
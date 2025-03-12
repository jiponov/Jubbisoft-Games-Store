package app.user.service;

import app.loyalty.service.*;
import app.security.*;
import app.shared.exception.*;
import app.user.model.*;
import app.user.model.User;
import app.user.repository.*;
import app.wallet.model.*;
import app.wallet.service.*;
import app.web.dto.*;
import jakarta.persistence.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.math.*;
import java.time.*;
import java.util.*;


@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;
    private final LoyaltyService loyaltyService;


    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, WalletService walletService, LoyaltyService loyaltyService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletService = walletService;
        this.loyaltyService = loyaltyService;
    }


    //@CacheEvict(value = "users", allEntries = true)
    @Transactional
    public User register(RegisterRequest registerRequest) {

        Optional<User> optionalUser = userRepository.findByUsername(registerRequest.getUsername());

        if (optionalUser.isPresent()) {
            throw new DomainException("Username [%s] already exist.".formatted(registerRequest.getUsername()));
        }

        // 1. Създавам USER
        User user = initializeUser(registerRequest);

        // 2. Записвам User в DB:
        user = userRepository.save(user);

        // ДОБАВИ други ДЕФОЛТНИ състояния АКО има: >>

        // 3. Създавам Wallet и го свързваме с USERA
        Wallet myWallet = walletService.createNewWallet(user);
        user.setWallet(myWallet);
        userRepository.save(user);     // Запазвам отново, за да запази Wallet-а

        // 4. Сега вече имам USER с ID и МОГА да му дам НОВО Loyalty
        // При регистрация, User автоматично получава DEFAULT Loyalty , той е member
        loyaltyService.createLoyalty(user);

        log.info("Successfully create new user account for username [%s] and id [%s]".formatted(user.getUsername(), user.getId()));

        return user;
    }


    private User initializeUser(RegisterRequest registerRequest) {
        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.USER)
                .isActive(true)
                .country(registerRequest.getCountry())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        return user;
    }


    //@CacheEvict(value = "users", allEntries = true)
    public void editUserDetails(UUID userId, UserEditRequest userEditRequest) {

        User user = getById(userId);

        // Получаваме email и премахваме излишни интервали
        String email = userEditRequest.getEmail();

        if (email != null) {
            email = email.trim();
        }

        // someone else - existingEmailUser
        // Проверка дали email е въведен или е изтрит
        if (email != null && !email.isEmpty()) {
            Optional<User> existingEmailUser = userRepository.findByEmail(email);

            if (existingEmailUser.isPresent() && !existingEmailUser.get().getId().equals(user.getId())) {
                throw new DomainException("Email is already in use! Choose another email.");
            }

            user.setEmail(email);
            // Запази email-a само ако е валиден

        } else {
            user.setEmail(null);
            // Запази  `null`,  Ако е празен или премахнат, запазва NULL в базата
        }


        // Обновяване на останалите данни на потребителя
        user.setFirstName(userEditRequest.getFirstName());
        user.setLastName(userEditRequest.getLastName());
        user.setProfilePicture(userEditRequest.getProfilePicture());

        user.setUpdatedOn(LocalDateTime.now());

        userRepository.save(user);
    }


    // В началото се изпълнява веднъж този метод и резултата се пази в кеш.
    // Всяко следващо извикване на този метод ще се чете резултата от кеша
    // и НЯМА да се извиква четенето от базата
    //@Cacheable("users")
    public List<User> getAllUsers() {

        List<User> users = userRepository.findAll();

        return users;
    }


    public User getById(UUID id) {

        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new DomainException("User with id [%s] does not exist.".formatted(id)));

        return user;
    }


    //@CacheEvict(value = "users", allEntries = true)
    public void switchStatus(UUID userId) {

        User user = getById(userId);

        if (user.isActive()) {
            user.setActive(false);
        } else {
            user.setActive(true);
        }

        userRepository.save(user);
    }


    //@CacheEvict(value = "users", allEntries = true)
    public void switchRole(UUID userId) {

        User user = getById(userId);

        if (user.getRole() == UserRole.USER) {
            user.setRole(UserRole.ADMIN);
        } else {
            user.setRole(UserRole.USER);
        }

        userRepository.save(user);
    }


    // Всеки път, когато потребител се логва, Spring Security ще извиква този метод, за да вземе детайлите на потребителя с този username
    // метод за login() към Spring Security
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // потребителя ни от БАЗА ДАННИ взимаме тук
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new DomainException("User with this username does not exist."));

        // ПРОВЕРКА за username към user, дали съвпадат външния вкаран параметър USERNAME с този от БАЗА ДАННИ USER !  >>
        return new AuthenticationMetadata(user.getId(), username, user.getPassword(), user.getRole(), user.isActive());
    }


    // Използване на userService за запазване на потребителя, а не на UserRepository директно
    // SAVE User in DB
    @Transactional
    public User saveUser(User user) {
        User savedUser = userRepository.save(user);

        return savedUser;
    }

}
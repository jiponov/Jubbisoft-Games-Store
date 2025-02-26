package app.user.service;

import app.shared.exception.*;
import app.user.model.*;
import app.user.repository.*;
import app.wallet.model.*;
import app.wallet.service.*;
import app.web.dto.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.math.*;
import java.time.*;
import java.util.*;


@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;


    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, WalletService walletService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletService = walletService;
    }


    public User login(LoginRequest loginRequest) {

        Optional<User> optionalUser = userRepository.findByUsername(loginRequest.getUsername());

        if (optionalUser.isEmpty()) {
            throw new DomainException("Username or password are incorrect.");
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new DomainException("Username or password are incorrect.");
        }

        return user;
    }


    //@CacheEvict(value = "users", allEntries = true)
    @Transactional
    public User register(RegisterRequest registerRequest) {

        Optional<User> optionalUser = userRepository.findByUsername(registerRequest.getUsername());

        if (optionalUser.isPresent()) {
            throw new DomainException("Username [%s] already exist.".formatted(registerRequest.getUsername()));
        }

        User user = initializeUser(registerRequest);

        Wallet myWallet = walletService.createNewWallet(user);
        user.setWallet(myWallet);

        // ДОБАВИ други ДЕФОЛТНИ състояния АКО има      >>     . . . . . . .

        user = userRepository.save(user);
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

}


/*


//@CacheEvict(value = "users", allEntries = true)
    public void editUserDetails(UUID userId, UserEditRequest userEditRequest) {

        User user = getById(userId);

        // Получаваме email и премахваме излишни интервали
        String email = userEditRequest.getEmail();

        if (email != null) {
            email = email.trim();
        }

        // someone else - existingEmailUser
        // Проверка за уникален email
        if (userEditRequest.getEmail() != null && !userEditRequest.getEmail().trim().isEmpty()) {
            Optional<User> existingEmailUser = userRepository.findByEmail(userEditRequest.getEmail());

            if (existingEmailUser.isPresent() && !existingEmailUser.get().getId().equals(user.getId())) {
                throw new DomainException("Email is already in use! Choose another email.");
            }

            user.setEmail(userEditRequest.getEmail().trim());
            // Запази email-a само ако е валиден

        } else {
            user.setEmail(null);
            // Запази  `null`, ако не е въведен email или е празен String  ""
        }

        user.setFirstName(userEditRequest.getFirstName());
        user.setLastName(userEditRequest.getLastName());
        user.setEmail(userEditRequest.getEmail());
        user.setProfilePicture(userEditRequest.getProfilePicture());

        userRepository.save(user);
    }


// see here:
        // Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        // if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
        //     throw new IllegalArgumentException("Username is already taken!");
        // }




//@CacheEvict(value = "users", allEntries = true)
    public void editUserDetails(UUID userId, UserEditRequest userEditRequest) {

        // see here:
        // Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        // if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
        //     throw new IllegalArgumentException("Username is already taken!");
        // }

        User user = getById(userId);
        // someone else - existingEmailUser
        // Проверка дали email е празен или null преди да търсим в базата
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            List<User> existingEmailUsers = userRepository.findByEmail(user.getEmail());

            if (!existingEmailUsers.isEmpty()) {
                // Проверяваме дали има потребител с този имейл, който не е текущия
                boolean emailTaken = existingEmailUsers.stream()
                        .anyMatch(existingUser -> !existingUser.getId().equals(user.getId()));

                if (emailTaken) {
                    throw new DomainException("Email is already in use! Change your with new email");
                }
            }
        }

        // User user = getById(userId);

        user.setFirstName(userEditRequest.getFirstName());
        user.setLastName(userEditRequest.getLastName());
        user.setEmail(userEditRequest.getEmail());
        user.setProfilePicture(userEditRequest.getProfilePicture());

        userRepository.save(user);
    }





        //@CacheEvict(value = "users", allEntries = true)
    public void editUserDetails(UUID userId, UserEditRequest userEditRequest) {

        // see here:
        // Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        // if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
        //     throw new IllegalArgumentException("Username is already taken!");
        // }
        User user = getById(userId);
        // someone else - existingEmailUser
        Optional<User> existingEmailUser = userRepository.findByEmail(user.getEmail());

        if (existingEmailUser.isPresent() && !existingEmailUser.get().getId().equals(user.getId())) {
            throw new DomainException("Email is already in use! Change your with new email");
        }

        //User user = getById(userId);

        user.setFirstName(userEditRequest.getFirstName());
        user.setLastName(userEditRequest.getLastName());
        user.setEmail(userEditRequest.getEmail());
        user.setProfilePicture(userEditRequest.getProfilePicture());

        userRepository.save(user);
    }

    */
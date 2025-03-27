package app.user;

import app.loyalty.service.*;
import app.security.*;
import app.shared.exception.*;
import app.user.model.*;
import app.user.model.User;
import app.user.repository.*;
import app.user.service.*;
import app.wallet.model.*;
import app.wallet.service.*;
import app.web.dto.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private WalletService walletService;
    @Mock
    private LoyaltyService loyaltyService;

    @InjectMocks
    private UserService userService;


    // register()  -  UserService
    // When user exist with this username -> exception is thrown
    // existing username - обработка на дублиран потребител
    @Test
    void givenExistingUsername_whenRegister_thenExceptionIsThrown() {
        // Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("Lub123")
                .password("123123")
                .country(Country.BULGARIA)
                .build();

        when(userRepository.findByUsername(registerRequest.getUsername()))
                .thenReturn(Optional.of(new User()));

        // When & Then
        assertThrows(UsernameAlreadyExistException.class, () -> userService.register(registerRequest));

        // Проверка дали не се извикват нежелани методи
        verify(userRepository, never()).save(any());
        verify(walletService, never()).createNewWallet(any());
        verify(loyaltyService, never()).createLoyalty(any());
    }


    // register()  -  UserService
    // Happy path Registration
    @Test
    void givenHappyPath_whenRegister() {

        // Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("Lub123")
                .password("123123")
                .country(Country.BULGARIA)
                .build();

        User userToSave = User.builder()
                .username(registerRequest.getUsername())
                .password("encoded_password")
                .role(UserRole.USER)
                .isActive(true)
                .country(registerRequest.getCountry())
                .build();

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .username("Lub123")
                .password("encoded_password")
                .role(UserRole.USER)
                .isActive(true)
                .country(Country.BULGARIA)
                .build();

        Wallet wallet = new Wallet();

        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);    // първо запазване
        when(walletService.createNewWallet(any(User.class))).thenReturn(wallet);

        // When
        User registeredUser = userService.register(registerRequest);

        // Then
        assertNotNull(registeredUser);
        assertEquals(registerRequest.getUsername(), registeredUser.getUsername());
        assertEquals("encoded_password", registeredUser.getPassword());
        assertNotNull(registeredUser.getId());

        verify(userRepository, times(2)).save(any(User.class));    // Записва се два пъти
        verify(walletService, times(1)).createNewWallet(any(User.class));
        verify(loyaltyService, times(1)).createLoyalty(any(User.class));
    }


    // register()  -  UserService
    // ако walletService.createNewWallet() хвърли грешка – трябва регистрацията да се прекрати
    // Wallet fail - грешка в създаване на WALLET
    @Test
    void givenWalletCreationFails_whenRegister_thenExceptionPropagates() {
        // Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("Lub123")
                .password("123123")
                .country(Country.BULGARIA)
                .build();

        User user = User.builder()
                .username("Lub123")
                .password("encoded_pass")
                .role(UserRole.USER)
                .isActive(true)
                .country(Country.BULGARIA)
                .build();

        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(walletService.createNewWallet(any(User.class)))
                .thenThrow(new RuntimeException("Wallet service failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.register(registerRequest));
        assertEquals("Wallet service failed", exception.getMessage());

        // Увери се, че loyaltyService не се вика след грешка
        verify(loyaltyService, never()).createLoyalty(any());
    }


    // register()  -  UserService
    // енкодването на паролата - Password encoded	(правилно кодиране)
    @Test
    void givenValidRequest_whenRegister_thenUserIsSavedWithCorrectEncodedPassword() {
        // Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("Lub123")
                .password("plainPassword")
                .country(Country.BULGARIA)
                .build();

        String encodedPassword = "encoded123";

        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plainPassword")).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(walletService.createNewWallet(any(User.class))).thenReturn(new Wallet());

        // When
        User registeredUser = userService.register(registerRequest);

        // Then
        assertEquals(encodedPassword, registeredUser.getPassword());
        verify(passwordEncoder, times(1)).encode("plainPassword");
    }


    // register()  -  UserService
    // Null username - Edge case валидация
    @Test
    void givenNullUsername_whenRegister_thenThrowsException() {
        // Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username(null)
                .password("123123")
                .country(Country.BULGARIA)
                .build();

        when(userRepository.findByUsername(null)).thenThrow(new IllegalArgumentException("Username is null"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.register(registerRequest));
    }


    // loadUserByUsername()  -  UserService
    // When User does not exist - then throws exception
    @Test
    void givenMissingUserFromDatabase_whenLoadUserByUsername_thenExceptionIsThrown() {

        // Given
        String username = "Lub123";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DomainException.class, () -> userService.loadUserByUsername(username));
    }


    // loadUserByUsername()  -  UserService
    // When user exist - then return new AuthenticationMetadata
    @Test
    void givenExistingUser_whenLoadUserByUsername_thenReturnCorrectAuthenticationMetadata() {

        // Given
        String username = "Lub123";
        User user = User.builder()
                .id(UUID.randomUUID())
                .isActive(true)
                .password("123123")
                .role(UserRole.ADMIN)
                .build();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        UserDetails authenticationMetadata = userService.loadUserByUsername(username);

        // Then
        assertInstanceOf(AuthenticationMetadata.class, authenticationMetadata);
        AuthenticationMetadata result = (AuthenticationMetadata) authenticationMetadata;
        assertEquals(user.getId(), result.getUserId());
        assertEquals(username, result.getUsername());
        assertEquals(user.getPassword(), result.getPassword());
        assertEquals(user.isActive(), result.isActive());
        assertEquals(user.getRole(), result.getRole());
        assertThat(result.getAuthorities()).hasSize(1);
        assertEquals("ROLE_ADMIN", result.getAuthorities().iterator().next().getAuthority());
    }


    // editUserDetails()  -  UserService
    // SUCCESS Edit с валиден нов email
    @Test
    void givenValidEmailChange_whenEditUserDetails_thenUserIsUpdated() {

        // Given
        UUID userId = UUID.randomUUID();
        User existingUser = User.builder()
                .id(userId)
                .email("old@email.com")
                .build();

        UserEditRequest editRequest = UserEditRequest.builder()
                .email("new@email.com")
                .firstName("Ivan")
                .lastName("Ivanov")
                .profilePicture("www.pic.jpg")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("new@email.com")).thenReturn(Optional.empty());

        // When
        userService.editUserDetails(userId, editRequest);

        // Then
        assertEquals("new@email.com", existingUser.getEmail());
        assertEquals("Ivan", existingUser.getFirstName());
        assertEquals("Ivanov", existingUser.getLastName());
        assertEquals("www.pic.jpg", existingUser.getProfilePicture());
        verify(userRepository).save(existingUser);
    }


    // editUserDetails()  -  UserService
    // Email е празен -> трябва да се запази като null
    @Test
    void givenEmptyEmail_whenEditUserDetails_thenEmailIsSetToNull() {
        UUID userId = UUID.randomUUID();
        User existingUser = User.builder()
                .id(userId)
                .email("old@email.com")
                .build();

        UserEditRequest editRequest = UserEditRequest.builder()
                .email("   ")                // само празни спейсове
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        userService.editUserDetails(userId, editRequest);

        assertNull(existingUser.getEmail());
        verify(userRepository).save(existingUser);
    }


    // editUserDetails()  -  UserService
    // Email вече е зает от друг потребител -> Exception се хвърля EmailAlreadyExistException
    @Test
    void givenEmailTakenByAnotherUser_whenEditUserDetails_thenThrowsException() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        User currentUser = User.builder()
                .id(userId)
                .email("old@email.com")
                .build();

        User otherUser = User.builder()
                .id(otherUserId)
                .email("taken@email.com")
                .build();

        UserEditRequest editRequest = UserEditRequest.builder()
                .email("taken@email.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByEmail("taken@email.com")).thenReturn(Optional.of(otherUser));

        assertThrows(EmailAlreadyExistException.class, () -> userService.editUserDetails(userId, editRequest));
        verify(userRepository, never()).save(any());
    }


    // editUserDetails()  -  UserService
    // Email е същият, но принадлежи на същия user -> ок позволено
    @Test
    void givenSameEmailBelongsToUser_whenEditUserDetails_thenUpdateIsAllowed() {

        UUID userId = UUID.randomUUID();

        User currentUser = User.builder()
                .id(userId)
                .email("lubo@email.com")
                .build();

        UserEditRequest editRequest = UserEditRequest.builder()
                .email("lubo@email.com")
                .firstName("Lubo")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByEmail("lubo@email.com")).thenReturn(Optional.of(currentUser));

        userService.editUserDetails(userId, editRequest);

        assertEquals("lubo@email.com", currentUser.getEmail());
        assertEquals("Lubo", currentUser.getFirstName());
        verify(userRepository).save(currentUser);
    }


    // editUserDetails()  -  UserService
    // промяна само на име/снимка, без да пипаме email
    @Test
    void givenNoEmailChange_whenEditUserDetails_thenOtherFieldsAreUpdated() {
        UUID userId = UUID.randomUUID();

        User currentUser = User.builder()
                .id(userId)
                .email("unchanged@email.com")
                .build();

        UserEditRequest editRequest = UserEditRequest.builder()
                .email("unchanged@email.com") // 🔧 ТРЯБВА да се подаде, иначе ще стане null
                .firstName("Lubo")
                .lastName("Jiponov")
                .profilePicture("profile.jpg")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByEmail("unchanged@email.com")).thenReturn(Optional.of(currentUser));

        userService.editUserDetails(userId, editRequest);

        assertEquals("unchanged@email.com", currentUser.getEmail());
        assertEquals("Lubo", currentUser.getFirstName());
        assertEquals("Jiponov", currentUser.getLastName());
        assertEquals("profile.jpg", currentUser.getProfilePicture());
        verify(userRepository).save(currentUser);
    }


    // editUserDetails()  -  UserService
    // валидно изтриване на email:  празен стринг (" ") или null  ->  това води до  user.setEmail(null).
    // Email е празен (или само интервали)  ->   email става null
    @Test
    void givenEmptyEmail_whenEditUserDetails_thenEmailIsRemoved() {
        // Given
        UUID userId = UUID.randomUUID();

        User currentUser = User.builder()
                .id(userId)
                .email("some@email.com")
                .build();

        UserEditRequest editRequest = UserEditRequest.builder()
                .email("   ")  // празен стринг с интервали
                .firstName("Lubo")
                .lastName("Jiponov")
                .profilePicture("profile.jpg")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));

        // When
        userService.editUserDetails(userId, editRequest);

        // Then
        assertNull(currentUser.getEmail()); // Email трябва да е null
        assertEquals("Lubo", currentUser.getFirstName());
        assertEquals("Jiponov", currentUser.getLastName());
        assertEquals("profile.jpg", currentUser.getProfilePicture());

        verify(userRepository).save(currentUser);
    }


    // editUserDetails()  -  UserService
    // валидно изтриване на email:  null  ->  това води до  user.setEmail(null).
    // Email е null  ->   email става null
    @Test
    void givenNullEmail_whenEditUserDetails_thenEmailIsRemoved() {
        UUID userId = UUID.randomUUID();

        User currentUser = User.builder()
                .id(userId)
                .email("old@email.com")
                .build();

        UserEditRequest editRequest = UserEditRequest.builder()
                .email(null) // директно null
                .firstName("Null")
                .lastName("Case")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));

        userService.editUserDetails(userId, editRequest);

        assertNull(currentUser.getEmail());
        assertEquals("Null", currentUser.getFirstName());
        assertEquals("Case", currentUser.getLastName());

        verify(userRepository).save(currentUser);
    }


    // editUserDetails()  -  UserService
    // несъществуващ userId   ->  хвърля се DomainException (от getById())
    @Test
    void givenInvalidUserId_whenEditUserDetails_thenThrowsDomainException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserEditRequest editRequest = UserEditRequest.builder()
                .firstName("Lubo")
                .build();

        assertThrows(DomainException.class, () -> userService.editUserDetails(userId, editRequest));
        verify(userRepository, never()).save(any());
    }


    // getAllUsers  -  UserService
    // провери, че userRepository.findAll() се извиква и е ок
    @Test
    void givenExistingUsersInDatabase_whenGetAllUsers_thenReturnThemAll() {

        // Give
        List<User> userList = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(userList);

        // When
        List<User> users = userService.getAllUsers();

        // Then
        assertThat(users).hasSize(2);
    }


    // getAllUsers (алтернатива на горния)  -  UserService
    // провери, че userRepository.findAll() се извиква и е ок
    // Има потребители в db
    @Test
    void givenUsersInRepository_whenGetAllUsers_thenReturnList() {
        // Given
        List<User> mockUsers = List.of(
                User.builder().id(UUID.randomUUID()).username("user1").build(),
                User.builder().id(UUID.randomUUID()).username("user2").build()
        );

        when(userRepository.findAll()).thenReturn(mockUsers);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());

        verify(userRepository, times(1)).findAll();
    }


    // getAllUsers   -  UserService
    // Няма потребители в db
    @Test
    void givenNoUsersInRepository_whenGetAllUsers_thenReturnEmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAll();
    }


    // Switch status method  -  UserService
    // Потребител е active -> става inactive
    @Test
    void givenUserWithStatusActive_whenSwitchStatus_thenUserStatusBecomeInactive() {

        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .isActive(true)
                .build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // When
        userService.switchStatus(user.getId());

        // Then
        assertFalse(user.isActive());
        verify(userRepository, times(1)).save(user);
    }


    // Switch status method  -  UserService
    // Потребител е inactive -> става active
    @Test
    void givenUserWithStatusInactive_whenSwitchStatus_thenUserStatusBecomeActive() {

        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .isActive(false)
                .build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // When
        userService.switchStatus(user.getId());

        // Then
        assertTrue(user.isActive());
        verify(userRepository, times(1)).save(user);
    }


    // Switch status method  -  UserService
    // несъществуващ потребител ->  хвърля DomainException
    @Test
    void givenNonExistingUserId_whenSwitchStatus_thenThrowsDomainException() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DomainException.class, () -> userService.switchStatus(userId));
        verify(userRepository, never()).save(any());
    }


    // switchRole()  -  UserService
    @Test
    void givenUserWithRoleAdmin_whenSwitchRole_thenUserReceivesUserRole() {

        // Given
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .role(UserRole.ADMIN)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.switchRole(userId);

        // Then
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
    }


    // switchRole()  -  UserService
    @Test
    void givenUserWithRoleUser_whenSwitchRole_thenUserReceivesAdminRole() {

        // Given
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .role(UserRole.USER)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.switchRole(userId);

        // Then
        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
    }


    // getById()  -  UserService
    // User съществува в базата  ->	 Връща правилния user
    @Test
    void givenExistingUserId_whenGetById_thenReturnUser() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("user123")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        User result = userService.getById(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("user123", result.getUsername());
        verify(userRepository, times(1)).findById(userId);
    }


    // getById()  -  UserService
    // User не съществува  ->  Хвърля DomainException
    @Test
    void givenNonExistingUserId_whenGetById_thenThrowsDomainException() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        DomainException exception = assertThrows(DomainException.class, () -> userService.getById(userId));
        assertTrue(exception.getMessage().contains("User with id"));
        verify(userRepository, times(1)).findById(userId);
    }


    // saveUser()  -  UserService
    // Подаден валиден потребител -> Методът връща същия резултат, който save() връща
    // Проверка дали userRepository.save() се извиква точно веднъж
    @Test
    void givenValidUser_whenSaveUser_thenUserIsSavedAndReturned() {
        // Given
        User userToSave = User.builder()
                .username("Lub123")
                .email("abv@email.com")
                .build();

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .username("Lub123")
                .email("abv@email.com")
                .build();

        when(userRepository.save(userToSave)).thenReturn(savedUser);

        // When
        User result = userService.saveUser(userToSave);

        // Then
        assertNotNull(result);
        assertEquals(savedUser.getId(), result.getId());
        assertEquals(savedUser.getUsername(), result.getUsername());
        verify(userRepository, times(1)).save(userToSave);
    }


    // SCHEDULER  -  UserService
    // getInactiveUsersByLastActivity()
    // Има активни и неактивни потребители – филтрират се правилно
    @Test
    void givenUsersWithDifferentActivity_whenGetInactiveUsers_thenReturnFilteredList() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        User activeOldUser = User.builder()
                .id(UUID.randomUUID())
                .isActive(true)
                .updatedOn(now.minusDays(10))
                .build();

        User activeRecentUser = User.builder()
                .id(UUID.randomUUID())
                .isActive(true)
                .updatedOn(now.minusDays(1))
                .build();

        User inactiveUser = User.builder()
                .id(UUID.randomUUID())
                .isActive(false)
                .updatedOn(now.minusDays(20))
                .build();

        List<User> allUsers = List.of(activeOldUser, activeRecentUser, inactiveUser);

        when(userRepository.findAll()).thenReturn(allUsers);

        // When
        List<User> result = userService.getInactiveUsersByLastActivity(7);

        // Then
        assertEquals(1, result.size());
        assertTrue(result.contains(activeOldUser));
        assertFalse(result.contains(activeRecentUser));
        assertFalse(result.contains(inactiveUser));
    }


    // SCHEDULER  -  UserService
    // getInactiveUsersByLastActivity()
    // Няма потребители в db
    @Test
    void givenNoUsersInRepository_whenGetInactiveUsers_thenReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<User> result = userService.getInactiveUsersByLastActivity(7);

        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAll();
    }


    // SCHEDULER  -  UserService
    // getInactiveUsersByLastActivity()
    // Всички потребители са активни, но са скоро обновени – не се връщат
    @Test
    void givenAllUsersRecentlyActive_whenGetInactiveUsers_thenReturnEmptyList() {
        LocalDateTime now = LocalDateTime.now();

        List<User> users = List.of(
                User.builder().isActive(true).updatedOn(now.minusDays(1)).build(),
                User.builder().isActive(true).updatedOn(now).build()
        );

        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getInactiveUsersByLastActivity(5);

        assertTrue(result.isEmpty());
    }


    // SCHEDULER  -  UserService
    // deactivateUsers()
    // Подаваме списък с активни потребители  =>   всички стават неактивни и се записват
    @Test
    void givenActiveUsers_whenDeactivateUsers_thenAllBecomeInactiveAndSaved() {
        // Given
        User user1 = User.builder().id(UUID.randomUUID()).isActive(true).build();
        User user2 = User.builder().id(UUID.randomUUID()).isActive(true).build();
        List<User> users = List.of(user1, user2);

        // When
        userService.deactivateUsers(users);

        // Then
        assertFalse(user1.isActive());
        assertFalse(user2.isActive());

        verify(userRepository, times(1)).saveAll(users);
    }


    // SCHEDULER  -  UserService
    // deactivateUsers()
    // Подаваме празен списък =>   saveAll() пак се вика, но с празно
    @Test
    void givenEmptyUserList_whenDeactivateUsers_thenSaveAllCalledWithEmptyList() {
        // Given
        List<User> emptyList = Collections.emptyList();

        // When
        userService.deactivateUsers(emptyList);

        // Then
        verify(userRepository, times(1)).saveAll(emptyList);
    }

    // SCHEDULER  -  UserService
    // countActiveUsers()
    // Комбинация от активни и неактивни потребители ->	Връща правилния брой активни
    @Test
    void givenUsersWithMixedStatus_whenCountActiveUsers_thenReturnCorrectCount() {
        // Given
        List<User> users = List.of(
                User.builder().isActive(true).build(),
                User.builder().isActive(false).build(),
                User.builder().isActive(true).build()
        );
        when(userRepository.findAll()).thenReturn(users);

        // When
        long result = userService.countActiveUsers();

        // Then
        assertEquals(2, result);
        verify(userRepository, times(1)).findAll();
    }

    // SCHEDULER  -  UserService
    // countActiveUsers()
    // Всички потребители са неактивни
    @Test
    void givenAllUsersInactive_whenCountActiveUsers_thenReturnZero() {
        List<User> users = List.of(
                User.builder().isActive(false).build(),
                User.builder().isActive(false).build()
        );
        when(userRepository.findAll()).thenReturn(users);

        long result = userService.countActiveUsers();

        assertEquals(0, result);
    }


    // SCHEDULER  -  UserService
    // countActiveUsers()
    // Списъкът от базата е празен
    @Test
    void givenNoUsersInRepository_whenCountActiveUsers_thenReturnZero() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        long result = userService.countActiveUsers();

        assertEquals(0, result);
    }


    // SCHEDULER  -  UserService
    // countInactiveUsers()
    // Има комбинация от активни и неактивни потребители - Проверява се дали връща броя на точно неактивните
    @Test
    void givenUsersWithMixedStatus_whenCountInactiveUsers_thenReturnCorrectCount() {
        // Given
        List<User> users = List.of(
                User.builder().isActive(true).build(),
                User.builder().isActive(false).build(),
                User.builder().isActive(false).build()
        );
        when(userRepository.findAll()).thenReturn(users);

        // When
        long result = userService.countInactiveUsers();

        // Then
        assertEquals(2, result);
        verify(userRepository, times(1)).findAll();
    }

    // SCHEDULER  -  UserService
    // countInactiveUsers()
    // Всички потребители са активни - Методът трябва да върне 0
    @Test
    void givenAllUsersActive_whenCountInactiveUsers_thenReturnZero() {
        // Given
        List<User> users = List.of(
                User.builder().isActive(true).build(),
                User.builder().isActive(true).build()
        );
        when(userRepository.findAll()).thenReturn(users);

        // When
        long result = userService.countInactiveUsers();

        // Then
        assertEquals(0, result);
    }


    // SCHEDULER  -  UserService
    // countInactiveUsers()
    // Няма потребители в базата - Методът трябва да върне 0 без грешка
    @Test
    void givenNoUsersInRepository_whenCountInactiveUsers_thenReturnZero() {
        // Given
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        long result = userService.countInactiveUsers();

        // Then
        assertEquals(0, result);
    }


}
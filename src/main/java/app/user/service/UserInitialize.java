package app.user.service;

import app.game.model.*;
import app.game.repository.*;
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
    private final GameRepository gameRepository;


    @Autowired
    public UserInitialize(UserService userService, PasswordEncoder passwordEncoder, WalletRepository walletRepository, UserRepository userRepository, GameRepository gameRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }


    @Override
    public void run(String... args) throws Exception {

        // save check че няма да регистрираме 2 пъти един и същи потребител при стартиране на APP
        if (!userService.getAllUsers().isEmpty()) {
            return;
        }

        // Създаваме Wallet
        Wallet walletXbox = Wallet.builder()
                .balance(new BigDecimal("1000.00"))
                .status(WalletStatus.ACTIVE)
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        walletXbox = walletRepository.save(walletXbox); // Запазваме Wallet, за да има ID

        // Създаваме User с вече създаден Wallet
        User XBOX = User.builder()
                .username("Xbox123")
                .password(passwordEncoder.encode("123123"))
                .role(UserRole.ADMIN)
                .isActive(true)
                .country(Country.GERMANY)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .wallet(walletXbox)       // Свързваме User с вече създаден Wallet
                .build();


        // Запазваме потребителя в базата ПРЕДИ да създадем игри
        XBOX = userRepository.save(XBOX);


        // Създаваме игрите със записания вече в базата XBOX като publisher
        Game gameXbox1 = Game.builder()
                .publisher(XBOX)    // Администраторът се записва като publisher, Тук вече е записан в DB
                .title("Cyber Wars: Uprising")
                .description("A fast-paced sci-fi shooter where you lead a resistance against a rogue AI empire. Intense combat, futuristic weapons, and high-speed chases await!")
                .price(new BigDecimal("29.99"))
                .genre(Genre.ACTION)
                .isAvailable(true)    // Играта е достъпна
                .imageCoverUrl("https://eapi.pcloud.com/getpubthumb?code=XZHbhlZYpsLLpEaFzJ1EfaqYH3p5HKlubFk&size=800x800&format=png")
                .releaseDate(LocalDate.now())    // Датата на пускане е днес
                .build();

        gameXbox1 = gameRepository.save(gameXbox1);

        Game gameXbox2 = Game.builder()
                .publisher(XBOX)    // Администраторът се записва като publisher
                .title("Mystic Quest: Shadows of Time")
                .description("An epic RPG adventure where you must restore balance to the realms by unlocking forgotten magic and battling dark forces in an enchanted world.")
                .price(new BigDecimal("49.99"))
                .genre(Genre.RPG)
                .isAvailable(true)    // Играта е достъпна
                .imageCoverUrl("https://eapi.pcloud.com/getpubthumb?code=XZIShlZaM7vVYwTxRQBIno6tM4AKkA5FC3k&size=800x800&format=png")
                .releaseDate(LocalDate.now())    // Датата на пускане е днес
                .build();

        gameXbox2 = gameRepository.save(gameXbox2);

        Game gameXbox3 = Game.builder()
                .publisher(XBOX)    // Администраторът се записва като publisher
                .title("Ultimate Tactics: Kingdoms at War")
                .description("A deep strategy game where you build, defend, and expand your medieval empire through diplomacy, warfare, and resource management.")
                .price(new BigDecimal("19.99"))
                .genre(Genre.STRATEGY)
                .isAvailable(true)    // Играта е достъпна
                .imageCoverUrl("https://eapi.pcloud.com/getpubthumb?code=XZpjhlZQUh21UNvuaLkI4JxLNOwhYjeEfuX&size=800x800&format=png")
                .releaseDate(LocalDate.now())    // Датата на пускане е днес
                .build();

        gameXbox3 = gameRepository.save(gameXbox3);

        Game gameXbox4 = Game.builder()
                .publisher(XBOX)    // Администраторът се записва като publisher
                .title("Ultimate Soccer Showdown")
                .description("Experience the thrill of competitive football with stunning graphics, real-time physics, and immersive gameplay. Build your dream team, strategize your formations, and conquer tournaments worldwide.")
                .price(new BigDecimal("9.99"))
                .genre(Genre.SPORTS)
                .isAvailable(true)    // Играта е достъпна
                .imageCoverUrl("https://eapi.pcloud.com/getpubthumb?code=XZijhlZxsrNNvJdkGS3mXcUpRcrXRBRSCr7&size=800x800&format=png")
                .releaseDate(LocalDate.now())    // Датата на пускане е днес
                .build();

        gameXbox4 = gameRepository.save(gameXbox4);

        Game gameXbox5 = Game.builder()
                .publisher(XBOX)    // Администраторът се записва като publisher
                .title("Lost in the Enchanted Jungle")
                .description("Embark on a breathtaking journey through mystical jungles, solve ancient puzzles, and uncover lost treasures while surviving nature’s wildest challenges.")
                .price(new BigDecimal("39.99"))
                .genre(Genre.ADVENTURE)
                .isAvailable(true)    // Играта е достъпна
                .imageCoverUrl("https://eapi.pcloud.com/getpubthumb?code=XZ2uhlZYsOUxEAno5Hp070QYWL8gQKkUjc7&size=800x800&format=png")
                .releaseDate(LocalDate.now())    // Датата на пускане е днес
                .build();

        gameXbox5 = gameRepository.save(gameXbox5);


        userRepository.save(XBOX);



        // Създаваме Wallet
        Wallet walletPlayStation = Wallet.builder()
                .balance(new BigDecimal("1000.00"))
                .status(WalletStatus.ACTIVE)
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        walletPlayStation = walletRepository.save(walletPlayStation); // Запазваме Wallet, за да има ID

        // Създаваме User с вече създаден Wallet
        User PlayStation = User.builder()
                .username("PlayStation")
                .password(passwordEncoder.encode("123123"))
                .role(UserRole.ADMIN)
                .isActive(true)
                .country(Country.ITALY)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .wallet(walletPlayStation)       // Свързваме User с вече създаден Wallet
                .build();

        userRepository.save(PlayStation);


        Wallet walletUser1 = Wallet.builder()
                .balance(new BigDecimal("100.00"))
                .status(WalletStatus.ACTIVE)
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        walletUser1 = walletRepository.save(walletUser1); // Запазваме Wallet, за да има ID

        // Създаваме User с вече създаден Wallet
        User user1 = User.builder()
                .username("Lub123")
                .password(passwordEncoder.encode("123123"))
                .role(UserRole.USER)
                .isActive(true)
                .country(Country.BULGARIA)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .wallet(walletUser1)       // Свързваме User с вече създаден Wallet
                .build();

        userRepository.save(user1);


        Wallet walletUser2 = Wallet.builder()
                .balance(new BigDecimal("100.00"))
                .status(WalletStatus.ACTIVE)
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        walletUser2 = walletRepository.save(walletUser2); // Запазваме Wallet, за да има ID

        // Създаваме User с вече създаден Wallet
        User user2 = User.builder()
                .username("Jin123")
                .password(passwordEncoder.encode("123123"))
                .role(UserRole.USER)
                .isActive(true)
                .country(Country.FRANCE)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .wallet(walletUser2)       // Свързваме User с вече създаден Wallet
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
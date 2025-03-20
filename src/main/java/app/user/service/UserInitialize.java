package app.user.service;

import app.game.service.*;
import app.treasury.model.*;
import app.treasury.repository.*;
import app.game.model.*;
import app.game.repository.*;
import app.loyalty.service.*;
import app.user.model.*;
import app.user.repository.*;
import app.wallet.model.*;
import app.wallet.repository.*;
import app.wallet.service.*;
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
    private final PasswordEncoder passwordEncoder;

    private final WalletService walletService;
    private final GameService gameService;
    private final LoyaltyService loyaltyService;


    @Autowired
    public UserInitialize(UserService userService, PasswordEncoder passwordEncoder, WalletService walletService, GameService gameService, LoyaltyService loyaltyService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.walletService = walletService;
        this.gameService = gameService;
        this.loyaltyService = loyaltyService;
    }


    @Override
    public void run(String... args) throws Exception {

        // save check че няма да регистрираме 2 пъти един и същи потребител при стартиране на APP
        if (!userService.getAllUsers().isEmpty()) {
            return;
        }

        // 1. ADMIN
        // Създаваме Wallet
        Wallet walletPlayaDeepCorporation = Wallet.builder()
                .balance(new BigDecimal("250.00"))
                .status(WalletStatus.ACTIVE)
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        // Запазваме Wallet, за да има ID
        walletPlayaDeepCorporation = walletService.saveWallet(walletPlayaDeepCorporation);

        // Създаваме User с вече създаден Wallet
        User playaDeepCorporation = User.builder()
                .username("PlayaDeepCorporation")
                .password(passwordEncoder.encode("123123"))
                .role(UserRole.ADMIN)
                .isActive(true)
                .country(Country.GERMANY)
                .profilePicture("https://eapi.pcloud.com/getpubthumb?code=XZ0UtlZXkPAa70ESGjjwTacOAs1wVoc8wQk&size=800x800&format=jpg")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .wallet(walletPlayaDeepCorporation)       // Свързваме User с вече създаден Wallet
                .build();


        // Запазваме потребителя в базата ПРЕДИ да създадем игри
        playaDeepCorporation = userService.saveUser(playaDeepCorporation);

        // Създаваме DEFAULT Loyalty за PlayaDeepCorporation
        loyaltyService.createLoyalty(playaDeepCorporation);


        // Създаваме игрите със записания вече в базата PlayaDeepCorporation като publisher
        Game gamePlayaDeepCorporation1 = Game.builder()
                .publisher(playaDeepCorporation)    // Администраторът се записва като publisher, Тук вече е записан в DB
                .title("Cyber Wars: Uprising")
                .description("A fast-paced sci-fi shooter where you lead a resistance against a rogue AI empire. Intense combat, futuristic weapons, and high-speed chases await!")
                .price(new BigDecimal("160.00"))
                .genre(Genre.ACTION)
                .isAvailable(true)    // Играта е достъпна
                .imageCoverUrl("https://eapi.pcloud.com/getpubthumb?code=XZHbhlZYpsLLpEaFzJ1EfaqYH3p5HKlubFk&size=800x800&format=png")
                .releaseDate(LocalDateTime.now())    // Датата на пускане е днес
                .updatedOn(LocalDateTime.now())
                .build();

        gamePlayaDeepCorporation1 = gameService.saveGame(gamePlayaDeepCorporation1);


        Game gamePlayaDeepCorporation2 = Game.builder()
                .publisher(playaDeepCorporation)    // Администраторът се записва като publisher
                .title("Mystic Quest: Shadows of Time")
                .description("An epic RPG adventure where you must restore balance to the realms by unlocking forgotten magic and battling dark forces in an enchanted world.")
                .price(new BigDecimal("49.99"))
                .genre(Genre.RPG)
                .isAvailable(true)    // Играта е достъпна
                .imageCoverUrl("https://eapi.pcloud.com/getpubthumb?code=XZIShlZaM7vVYwTxRQBIno6tM4AKkA5FC3k&size=800x800&format=png")
                .releaseDate(LocalDateTime.now())    // Датата на пускане е днес
                .updatedOn(LocalDateTime.now())
                .build();

        gamePlayaDeepCorporation2 = gameService.saveGame(gamePlayaDeepCorporation2);


        Game gamePlayaDeepCorporation3 = Game.builder()
                .publisher(playaDeepCorporation)    // Администраторът се записва като publisher
                .title("Ultimate Tactics: Kingdoms at War")
                .description("A deep strategy game where you build, defend, and expand your medieval empire through diplomacy, warfare, and resource management.")
                .price(new BigDecimal("19.99"))
                .genre(Genre.STRATEGY)
                .isAvailable(true)    // Играта е достъпна
                .imageCoverUrl("https://eapi.pcloud.com/getpubthumb?code=XZpjhlZQUh21UNvuaLkI4JxLNOwhYjeEfuX&size=800x800&format=png")
                .releaseDate(LocalDateTime.now())    // Датата на пускане е днес
                .updatedOn(LocalDateTime.now())
                .build();

        gamePlayaDeepCorporation3 = gameService.saveGame(gamePlayaDeepCorporation3);


        Game gamePlayaDeepCorporation4 = Game.builder()
                .publisher(playaDeepCorporation)    // Администраторът се записва като publisher
                .title("Ultimate Soccer Showdown")
                .description("Experience the thrill of competitive football with stunning graphics, real-time physics, and immersive gameplay. Build your dream team, strategize your formations, and conquer tournaments worldwide.")
                .price(new BigDecimal("9.99"))
                .genre(Genre.SPORTS)
                .isAvailable(true)    // Играта е достъпна
                .imageCoverUrl("https://eapi.pcloud.com/getpubthumb?code=XZijhlZxsrNNvJdkGS3mXcUpRcrXRBRSCr7&size=800x800&format=png")
                .releaseDate(LocalDateTime.now())    // Датата на пускане е днес
                .updatedOn(LocalDateTime.now())
                .build();

        gamePlayaDeepCorporation4 = gameService.saveGame(gamePlayaDeepCorporation4);


        Game gamePlayaDeepCorporation5 = Game.builder()
                .publisher(playaDeepCorporation)    // Администраторът се записва като publisher
                .title("Lost in the Enchanted Jungle")
                .description("Embark on a breathtaking journey through mystical jungles, solve ancient puzzles, and uncover lost treasures while surviving nature’s wildest challenges.")
                .price(new BigDecimal("39.99"))
                .genre(Genre.ADVENTURE)
                .isAvailable(true)    // Играта е достъпна
                .imageCoverUrl("https://eapi.pcloud.com/getpubthumb?code=XZ2uhlZYsOUxEAno5Hp070QYWL8gQKkUjc7&size=800x800&format=png")
                .releaseDate(LocalDateTime.now())    // Датата на пускане е днес
                .updatedOn(LocalDateTime.now())
                .build();

        gamePlayaDeepCorporation5 = gameService.saveGame(gamePlayaDeepCorporation5);


        userService.saveUser(playaDeepCorporation);


        // 2. ADMIN
        // Създаваме Wallet
        Wallet walletJintenddoCorporation = Wallet.builder()
                .balance(new BigDecimal("250.00"))
                .status(WalletStatus.ACTIVE)
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        // Запазваме Wallet, за да има ID
        walletJintenddoCorporation = walletService.saveWallet(walletJintenddoCorporation);

        // Създаваме User с вече създаден Wallet
        User jintenddoCorporation = User.builder()
                .username("JintenddoCorporation")
                .password(passwordEncoder.encode("123123"))
                .role(UserRole.ADMIN)
                .isActive(true)
                .country(Country.ITALY)
                .profilePicture("https://eapi.pcloud.com/getpubthumb?code=XZeOtlZXDVQl5bW4KJnGzRpf64VP75AmwA7&size=800x800&format=jpg")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .wallet(walletJintenddoCorporation)       // Свързваме User с вече създаден Wallet
                .build();


        // Запазваме потребителя в базата ПРЕДИ да създадем игри
        jintenddoCorporation = userService.saveUser(jintenddoCorporation);

        // Създаваме DEFAULT Loyalty за PlayStation
        loyaltyService.createLoyalty(jintenddoCorporation);


        // Създаваме игрите със записания вече в базата JintenddoCorporation като publisher
        Game gameJintenddoCorporation1 = Game.builder()
                .publisher(jintenddoCorporation)    // Администраторът се записва като publisher
                .title("Shadow Reckoning")
                .description("Step into the shoes of a former mercenary returning to a war-torn city where secret organizations wage a relentless battle. With high-tech weapons and acrobatic combat skills, you must survive.")
                .price(new BigDecimal("49.99"))
                .genre(Genre.ACTION)
                .isAvailable(true)    // Играта е достъпна
                .imageCoverUrl("https://eapi.pcloud.com/getpubthumb?code=XZkEtlZoRLmSY3RWdVYJHiEfSWsDS5ppYfy&size=800x800&format=png")
                .releaseDate(LocalDateTime.now())    // Датата на пускане е днес
                .updatedOn(LocalDateTime.now())
                .build();

        gameJintenddoCorporation1 = gameService.saveGame(gameJintenddoCorporation1);


        Game gameJintenddoCorporation2 = Game.builder()
                .publisher(jintenddoCorporation)    // Администраторът се записва като publisher
                .title("Echoes of Eldoria")
                .description("Uncover the ancient mysteries of the lost civilization of Eldoria. As a young archaeologist, you will journey through jungles, deserts, and icy peaks, solving intricate puzzles, unlocking hidden realms, and facing mysterious guardians of time.")
                .price(new BigDecimal("99.99"))
                .genre(Genre.ADVENTURE)
                .isAvailable(true)    // Играта е достъпна
                .imageCoverUrl("https://eapi.pcloud.com/getpubthumb?code=XZPUtlZR8ErWX24kq7WySfmxYGoyh3xrsEk&size=800x800&format=png")
                .releaseDate(LocalDateTime.now())    // Датата на пускане е днес
                .updatedOn(LocalDateTime.now())
                .build();

        gameJintenddoCorporation2 = gameService.saveGame(gameJintenddoCorporation2);


        Game gameJintenddoCorporation3 = Game.builder()
                .publisher(jintenddoCorporation)    // Администраторът се записва като publisher
                .title("Turbo Blitz")
                .description("Feel the rush, embrace the speed! Turbo Blitz puts you in the heart of the ultimate sprinting challenge. Precision, endurance, and sheer willpower — do you have what it takes to become the fastest on the field?")
                .price(new BigDecimal("120.00"))
                .genre(Genre.SPORTS)
                .isAvailable(true)    // Играта е достъпна
                .imageCoverUrl("https://eapi.pcloud.com/getpubthumb?code=XZUUtlZc6o3Iv1smJRJxEh3J1EpHbM8RiyV&size=800x800&format=png")
                .releaseDate(LocalDateTime.now())    // Датата на пускане е днес
                .updatedOn(LocalDateTime.now())
                .build();

        gameJintenddoCorporation3 = gameService.saveGame(gameJintenddoCorporation3);


        Game gameJintenddoCorporation4 = Game.builder()
                .publisher(jintenddoCorporation)    // Администраторът се записва като publisher
                .title("Dominion: Rise of Empires")
                .description("Build your empire from the ground up and transform it into a global superpower! Manage resources, lead armies, and forge alliances in this real-time strategy game where every decision shapes history.")
                .price(new BigDecimal("62.00"))
                .genre(Genre.STRATEGY)
                .isAvailable(true)    // Играта е достъпна
                .imageCoverUrl("https://eapi.pcloud.com/getpubthumb?code=XZoUtlZHxNT9fHEbYXAoEajxsHWykiDIvCX&size=800x800&format=png")
                .releaseDate(LocalDateTime.now())    // Датата на пускане е днес
                .updatedOn(LocalDateTime.now())
                .build();

        gameJintenddoCorporation4 = gameService.saveGame(gameJintenddoCorporation4);


        Game gameJintenddoCorporation5 = Game.builder()
                .publisher(jintenddoCorporation)    // Администраторът се записва като publisher
                .title("Arcane Chronicles")
                .description("In a world where magic and technology coexist, you are the Chosen One—the only one who can prevent an ancient evil from awakening. Collect powerful artifacts, master new spells, and shape your own destiny in an epic role-playing adventure.")
                .price(new BigDecimal("85.00"))
                .genre(Genre.RPG)
                .isAvailable(true)    // Играта е достъпна
                .imageCoverUrl("https://eapi.pcloud.com/getpubthumb?code=XZ4ItlZFBWbfOjOqlJmGD0uQYUEryLdfqWy&size=800x800&format=png")
                .releaseDate(LocalDateTime.now())    // Датата на пускане е днес
                .updatedOn(LocalDateTime.now())
                .build();

        gameJintenddoCorporation5 = gameService.saveGame(gameJintenddoCorporation5);


        userService.saveUser(jintenddoCorporation);


        // 3. ADMIN
        // Създаваме Wallet
        Wallet walletXlocksCorporation = Wallet.builder()
                .balance(new BigDecimal("250.00"))
                .status(WalletStatus.ACTIVE)
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        // Запазваме Wallet, за да има ID
        walletXlocksCorporation = walletService.saveWallet(walletXlocksCorporation);


        // Създаваме User с вече създаден Wallet
        User xlocksCorporation = User.builder()
                .username("XlocksCorporation")
                .password(passwordEncoder.encode("123123"))
                .role(UserRole.ADMIN)
                .isActive(true)
                .country(Country.GERMANY)
                .profilePicture("https://eapi.pcloud.com/getpubthumb?code=XZpotlZfJiddR2OETH1ep8r2Pt7NmL4xgOX&size=800x800&format=jpg")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .wallet(walletXlocksCorporation)       // Свързваме User с вече създаден Wallet
                .build();


        // Запазваме потребителя в базата ПРЕДИ да създадем игри
        xlocksCorporation = userService.saveUser(xlocksCorporation);


        // Създаваме DEFAULT Loyalty за PlayaDeepCorporation
        loyaltyService.createLoyalty(xlocksCorporation);


        // Създаваме игрите със записания вече в базата XlocksCorporation като publisher
        Game gameXlocksCorporation1 = Game.builder()
                .publisher(xlocksCorporation)    // Администраторът се записва като publisher, Тук вече е записан в DB
                .title("Shadowstrike: Cyber Ops")
                .description("In a dystopian cyberpunk future, elite hacker-assassins wage war in the digital shadows. As a rogue agent, infiltrate megacorporations, outmaneuver security drones, and engage in high-speed parkour combat to uncover a conspiracy that threatens the world.")
                .price(new BigDecimal("47.50"))
                .genre(Genre.ACTION)
                .isAvailable(true)    // Играта е достъпна
                .imageCoverUrl("https://eapi.pcloud.com/getpubthumb?code=XZIotlZ6iT7e37igFhB2nwG7NsAnbOUILqV&size=800x800&format=png")
                .releaseDate(LocalDateTime.now())    // Датата на пускане е днес
                .updatedOn(LocalDateTime.now())
                .build();

        gameXlocksCorporation1 = gameService.saveGame(gameXlocksCorporation1);


        Game gameXlocksCorporation2 = Game.builder()
                .publisher(xlocksCorporation)    // Администраторът се записва като publisher, Тук вече е записан в DB
                .title("Echoes of Deep Eroween")
                .description("A young archaeologist stumbles upon an ancient artifact linked to a lost civilization. Travel through mystical ruins, solve cryptic puzzles, and unlock the secrets of time itself in this breathtaking open-world adventure.")
                .price(new BigDecimal("105.00"))
                .genre(Genre.ADVENTURE)
                .isAvailable(true)    // Играта е достъпна
                .imageCoverUrl("https://eapi.pcloud.com/getpubthumb?code=XZ5y6lZR90FcAYxI7kfL7E3nzT72LOFgYD7&size=800x800&format=png")
                .releaseDate(LocalDateTime.now())    // Датата на пускане е днес
                .updatedOn(LocalDateTime.now())
                .build();

        gameXlocksCorporation2 = gameService.saveGame(gameXlocksCorporation2);


        Game gameXlocksCorporation3 = Game.builder()
                .publisher(xlocksCorporation)    // Администраторът се записва като publisher, Тук вече е записан в DB
                .title("Hyper Xtreme")
                .description("Feel the adrenaline as you explode off the starting blocks, race against fierce competitors, and chase the ultimate glory on the grandest stadiums. Precision, power, and pure velocity — do you have what it takes to become the fastest of all time?")
                .price(new BigDecimal("70.00"))
                .genre(Genre.SPORTS)
                .isAvailable(true)    // Играта е достъпна
                .imageCoverUrl("https://eapi.pcloud.com/getpubthumb?code=XZWy6lZtNxbpw6j77SCSxHrTCnVvRF8Fe97&size=800x800&format=png")
                .releaseDate(LocalDateTime.now())    // Датата на пускане е днес
                .updatedOn(LocalDateTime.now())
                .build();

        gameXlocksCorporation3 = gameService.saveGame(gameXlocksCorporation3);


        Game gameXlocksCorporation4 = Game.builder()
                .publisher(xlocksCorporation)    // Администраторът се записва като publisher, Тук вече е записан в DB
                .title("Kingdoms at War: The Siege Begins")
                .description("Build an unbreakable fortress, command mighty armies, and forge powerful alliances in this epic medieval strategy game. Will your empire thrive, or will it fall to the tides of war? The battle for dominance begins now!")
                .price(new BigDecimal("8.80"))
                .genre(Genre.STRATEGY)
                .isAvailable(true)    // Играта е достъпна
                .imageCoverUrl("https://eapi.pcloud.com/getpubthumb?code=XZgy6lZ3UDIQT5BTbX1pVl27NxCAzk0GwkX&size=800x800&format=png")
                .releaseDate(LocalDateTime.now())    // Датата на пускане е днес
                .updatedOn(LocalDateTime.now())
                .build();

        gameXlocksCorporation4 = gameService.saveGame(gameXlocksCorporation4);


        Game gameXlocksCorporation5 = Game.builder()
                .publisher(xlocksCorporation)    // Администраторът се записва като publisher, Тук вече е записан в DB
                .title("Veilborn: Legacy of the Ancients")
                .description("In a world torn by war, you are a Veilborn – a warrior with forbidden magic. Choose your path, shape the fate of kingdoms, and uncover the dark forces behind the destruction of your homeland in this epic open-world RPG.")
                .price(new BigDecimal("16.20"))
                .genre(Genre.RPG)
                .isAvailable(true)    // Играта е достъпна
                .imageCoverUrl("https://eapi.pcloud.com/getpubthumb?code=XZIy6lZDxxB5rSoy2fP9I4YoWyyxQWAQeAX&size=800x800&format=png")
                .releaseDate(LocalDateTime.now())    // Датата на пускане е днес
                .updatedOn(LocalDateTime.now())
                .build();


        gameXlocksCorporation5 = gameService.saveGame(gameXlocksCorporation5);

        userService.saveUser(xlocksCorporation);


        // ----------- Създаване на обикновени потребители : USERS -----------


        // 4. USER

        Wallet walletUser1 = Wallet.builder()
                .balance(new BigDecimal("100.00"))
                .status(WalletStatus.ACTIVE)
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        // Запазваме Wallet, за да има ID
        walletUser1 = walletService.saveWallet(walletUser1);


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

        userService.saveUser(user1);


        // Създаваме DEFAULT Loyalty за user1
        loyaltyService.createLoyalty(user1);


        // 5. USER

        Wallet walletUser2 = Wallet.builder()
                .balance(new BigDecimal("100.00"))
                .status(WalletStatus.ACTIVE)
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        walletUser2 = walletService.saveWallet(walletUser2);


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

        userService.saveUser(user2);

        // Създаваме DEFAULT Loyalty за user2
        loyaltyService.createLoyalty(user2);

    }

}
package app;

import app.user.model.*;
import app.web.dto.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.annotation.*;
import org.springframework.test.context.*;

import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.wallet.repository.WalletRepository;
import app.loyalty.model.Loyalty;
import app.loyalty.repository.LoyaltyRepository;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class UserRegisterFullFlowITest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private LoyaltyRepository loyaltyRepository;

    @Test
    void testRegisterUserCreatesUserWalletAndLoyalty() {
        // given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("integration_user");
        request.setPassword("secret123");
        request.setCountry(Country.BULGARIA);

        // when
        User user = userService.register(request);

        // then

        assertThat(user).isNotNull();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getUsername()).isEqualTo("integration_user");
        assertThat(user.getCountry()).isEqualTo(Country.BULGARIA);

        Optional<Wallet> wallet = walletRepository.findByOwner(user);
        assertThat(wallet).isPresent();
        assertThat(wallet.get().getOwner().getId()).isEqualTo(user.getId());

        Optional<Loyalty> loyalty = loyaltyRepository.findByMemberId(user.getId());
        assertThat(loyalty).isPresent();
        assertThat(loyalty.get().getMember().getId()).isEqualTo(user.getId());
    }
}
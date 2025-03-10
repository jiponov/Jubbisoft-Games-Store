package app.shared.config;

import app.security.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.security.servlet.*;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.web.*;
import org.springframework.security.web.util.matcher.*;
import org.springframework.web.servlet.config.annotation.*;


@Configuration
@EnableMethodSecurity
public class WebMvcConfiguration implements WebMvcConfigurer {

    // SecurityFilterChain - начин, по който Spring Security разбира как да се прилага за нашето приложение
    // SecurityFilterChain defines security rules for the application
    // Uses HttpSecurity  to  configure authentication & authorization
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // authorizeHttpRequests - конфиг. за група от ендпойнти
        // requestMatchers - достъп до даден ендпойнт
        // .permitAll() - всеки може да достъпи този ендпойнт
        // .anyRequest() - всички заявки, които не съм изброил
        // .authenticated() - за да имаш достъп, трябва да си аутентикиран
        http
                .authorizeHttpRequests(matchers -> matchers
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/", "/register", "/contact", "/games/explore", "/games/{gameId}/explore").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        //                        .usernameParameter("username")
                        //                        .passwordParameter("password")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/")
                );

        return http.build();
    }
}
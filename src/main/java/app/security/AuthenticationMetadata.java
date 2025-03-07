package app.security;

import app.user.model.*;
import lombok.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.core.userdetails.*;

import java.util.*;


// ТОЗИ КЛАС ПАЗИ ДАННИТЕ НА ЛОГНАТИЯ ПОТРЕБИТЕЛ
@Data
@Getter
@AllArgsConstructor
public class AuthenticationMetadata implements UserDetails {

    private UUID userId;
    private String username;
    private String password;
    private UserRole role;
    private boolean isActive;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // hasRole("ADMIN")       -> "ROLE_ADMIN"
        // hasAuthority("ADMIN")  -> "ADMIN"

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.name());

        return List.of(authority);
    }


    @Override
    public String getPassword() {
        return this.password;
    }


    @Override
    public String getUsername() {
        return this.username;
    }


    @Override
    public boolean isAccountNonExpired() {
        return isActive;
    }


    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }


    @Override
    public boolean isCredentialsNonExpired() {
        return isActive;
    }


    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
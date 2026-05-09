package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.repositories.BanRepository;
import ru.vlsu.myng.repositories.UserRepository;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(login)
                .orElse(null);

        if (user == null) {
            user = userRepository.findByUsername(login)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "Пользователь не найден с логином: " + login));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name().toUpperCase())));
    }
}
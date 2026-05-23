package ru.vlsu.myng.services;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.entities.User.Role;
import ru.vlsu.myng.repositories.UserRepository;

import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityLoginTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void loginWithEmail_Success() throws Exception {
        User dummyUser = new User();
        dummyUser.setEmail("test@example.com");
        dummyUser.setUsername("testuser");
        dummyUser.setPasswordHash(passwordEncoder.encode("secret123"));
        dummyUser.setRole(Role.user);

        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(dummyUser));

        mockMvc.perform(formLogin("/auth/login")
                .user("username", "test@example.com")
                .password("password", "secret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("test@example.com"));
    }

    @Test
    public void loginWithUsername_Success() throws Exception {
        User dummyUser = new User();
        dummyUser.setEmail("test@example.com");
        dummyUser.setUsername("testuser");
        dummyUser.setPasswordHash(passwordEncoder.encode("secret123"));
        dummyUser.setRole(Role.user);

        Mockito.when(userRepository.findByEmail("testuser")).thenReturn(Optional.empty());
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(dummyUser));

        mockMvc.perform(formLogin("/auth/login")
                .user("username", "testuser")
                .password("password", "secret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("test@example.com"));
    }

    @Test
    public void login_WrongPassword_Failure() throws Exception {
        User dummyUser = new User();
        dummyUser.setEmail("test@example.com");
        dummyUser.setPasswordHash(passwordEncoder.encode("secret123"));
        dummyUser.setRole(Role.user);

        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(dummyUser));

        mockMvc.perform(formLogin("/auth/login")
                .user("username", "test@example.com")
                .password("password", "wrong_password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth?error=true"))
                .andExpect(unauthenticated());
    }

    @Test
    public void login_NonExistentUser_Failure() throws Exception {
        Mockito.when(userRepository.findByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());
        Mockito.when(userRepository.findByUsername("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(formLogin("/auth/login")
                .user("username", "nonexistent@example.com")
                .password("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth?error=true"))
                .andExpect(unauthenticated());
    }
}
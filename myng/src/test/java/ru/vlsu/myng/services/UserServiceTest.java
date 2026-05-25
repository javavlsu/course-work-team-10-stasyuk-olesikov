package ru.vlsu.myng.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.vlsu.myng.dto.UserRegistrationDto;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.repositories.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @BeforeEach()
    void setUp() {
        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityContextHolder.close();
    }

    // --- ТЕСТЫ РЕГИСТРАЦИИ ---

    @Test
    void registerNewUser_Success() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("player1");
        dto.setEmail("player1@mail.ru");
        dto.setPassword("qwerty123_password");
        dto.setConfirmPassword("qwerty123_password");

        when(userRepository.existsByUsername("player1")).thenReturn(false);
        when(userRepository.existsByEmail("player1@mail.ru")).thenReturn(false);
        when(passwordEncoder.encode("qwerty123_password")).thenReturn("hashed_password");

        User savedUser = new User();
        savedUser.setId(1);
        savedUser.setUsername("player1");
        savedUser.setRole(User.Role.user);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.registerNewUser(dto);

        assertNotNull(result);
        assertEquals("player1", result.getUsername());
        assertEquals(User.Role.user, result.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerNewUser_ThrowsException_WhenPasswordsDoNotMatch() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("player1");
        dto.setPassword("password123");
        dto.setConfirmPassword("different_password");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerNewUser(dto);
        });
        assertEquals("Пароли не совпадают", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerNewUser_ThrowsException_WhenUsernameTaken() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("existing_user");
        dto.setPassword("password123");
        dto.setConfirmPassword("password123");

        when(userRepository.existsByUsername("existing_user")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerNewUser(dto);
        });
        assertEquals("Имя пользователя уже занято", exception.getMessage());
    }

    // --- ТЕСТ СЛУЖЕБНОГО МЕТОДА GET_CURRENT_USER ---

    @Test
    void getCurrentUser_Success() {
        String userEmail = "active_player@mail.ru";

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(userEmail);

        User expectedUser = new User();
        expectedUser.setEmail(userEmail);
        expectedUser.setUsername("GamerX");
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(expectedUser));

        User currentUser = userService.getCurrentUser();

        assertNotNull(currentUser);
        assertEquals(userEmail, currentUser.getEmail());
        assertEquals("GamerX", currentUser.getUsername());
    }
}
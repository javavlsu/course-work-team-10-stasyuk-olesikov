package ru.vlsu.myng.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.repositories.BanRepository;
import ru.vlsu.myng.repositories.UserRepository;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class BanFilter extends OncePerRequestFilter
{

    private final UserRepository userRepository;
    private final BanRepository banRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException
    {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth != null
                && auth.isAuthenticated()
                && auth.getPrincipal() instanceof UserDetails details) {

            String email = details.getUsername();

            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null) {

                boolean banned =
                        banRepository
                                .findFirstByUserAndEndTimeAfterOrderByEndTimeDesc(
                                        user,
                                        Instant.now()
                                ).isPresent();

                String uri = request.getRequestURI();

                boolean allowed =
                        uri.startsWith("/banned")
                                || uri.startsWith("/profile/logout")
                                || uri.startsWith("/js")
                                || uri.startsWith("/images")
                                || uri.startsWith("/css");

                if (banned && !allowed) {
                    response.sendRedirect("/banned");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}

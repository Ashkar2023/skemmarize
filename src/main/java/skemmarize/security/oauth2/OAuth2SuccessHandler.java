package skemmarize.security.oauth2;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import skemmarize.exception.NotFoundException;
import skemmarize.security.JwtTokenService;
import skemmarize.service.UserService;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserService userService;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
        String email = oauth2User.getEmail();
        String username = oauth2User.getAttribute("login"); // GitHub username attribute

        try {
            userService.getUserByEmail(email);
        } catch (NotFoundException e) {
            // User doesn't exist, create them
            userService.createUser(email, username);
        }

        String accessToken = jwtTokenService.generateAccessToken(oauth2User);
        String refreshToken = jwtTokenService.generateRefreshToken(oauth2User);

        // Set access token cookie
        Cookie accessTokenCookie = new Cookie("ajwt", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(15 * 60);
        response.addCookie(accessTokenCookie);

        // Set refresh token cookie
        Cookie refreshTokenCookie = new Cookie("rjwt", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 86400);
        response.addCookie(refreshTokenCookie);

        // Redirect to frontend
        getRedirectStrategy().sendRedirect(request, response, frontendUrl);
    }
}


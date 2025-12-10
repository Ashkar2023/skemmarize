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
import skemmarize.model.User;
import skemmarize.security.JwtTokenService;
import skemmarize.service.UserService;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserService userService;

    @Value("${frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
        String email = oauth2User.getEmail();
        String username = oauth2User.getAttribute("login"); // GitHub username attribute

        // The problem is that if userService.createUser throws an exception,
        // the variable 'user' will not be set, so when you get to the JWT generation lines,
        // 'user' may not be initialized. This is what the linter is warning about.

        User user = null;

        try {
            user = userService.getUserByEmail(email);
        } catch (NotFoundException nfe) {
            // User doesn't exist, create them
            String avatarUrl = this.userService.downloadAndUploadAvatar(oauth2User.getAttribute("avatar_url"), username);
            user = userService.createUser(email, username, avatarUrl);
        }

        if (user == null) {
            throw new RuntimeException("User could not be loaded or created during OAuth2 login.");
        }

        String accessToken = jwtTokenService.generateAccessToken(oauth2User, String.valueOf(user.getId()));
        String refreshToken = jwtTokenService.generateRefreshToken(oauth2User, String.valueOf(user.getId()));

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

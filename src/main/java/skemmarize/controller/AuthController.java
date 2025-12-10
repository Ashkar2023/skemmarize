package skemmarize.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jwt.JWTClaimsSet;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import skemmarize.exception.JwtValidationException;
import skemmarize.external.ImageProcessor;
import skemmarize.model.User;
import skemmarize.security.JwtTokenService;
import skemmarize.service.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private ImageProcessor imageProcessor;

    @GetMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshJwtToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        // Extract refresh token from cookie
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("rjwt".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        // Check if refresh token exists
        if (refreshToken == null || refreshToken.isEmpty()) {
            Map<String, Object> body = new HashMap<>();
            body.put("message", null);
            body.put("error", "Refresh token not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }

        // Validate refresh token
        JWTClaimsSet claims = jwtTokenService.validateToken(refreshToken, true);

        if (claims == null) {
            Map<String, Object> body = new HashMap<>();
            body.put("message", null);
            body.put("error", "Invalid or expired refresh token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }

        // Extract email and generate new access token
        String userId = claims.getSubject();
        if (userId == null) {
            Map<String, Object> body = new HashMap<>();
            body.put("message", null);
            body.put("error", "Invalid token: missing subject");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }

        String email = (String) claims.getClaim("email");
        if (email == null) {
            Map<String, Object> body = new HashMap<>();
            body.put("message", null);
            body.put("error", "Invalid token: missing email");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }

        String newAccessToken = jwtTokenService.generateAccessTokenFromEmail(email, userId);

        // Set new access token cookie
        Cookie accessTokenCookie = new Cookie("ajwt", newAccessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(15 * 60); // 15 minutes
        response.addCookie(accessTokenCookie);

        // Return success response
        Map<String, Object> body = new HashMap<>();
        body.put("message", "access token refreshed");
        body.put("error", null);

        return ResponseEntity.ok(body);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> confirmOnLaunch(
            HttpServletRequest request,
            HttpServletResponse response) {

        Cookie[] cookies = request.getCookies();

        Cookie ajwt = Arrays.stream(cookies)
                .filter(c -> "ajwt".equals(c.getName()))
                .findFirst()
                .orElse(null);

        if (ajwt == null) {
            throw new JwtValidationException("access token not found");
        }

        JWTClaimsSet claims = jwtTokenService.validateToken(ajwt.getValue(), false);

        if (claims == null) {
            throw new JwtValidationException("unauthorized");
        }

        User dbUser = this.userService.getUserByEmail((String) claims.getClaim("email"));

        dbUser.setAvatar(this.imageProcessor.generatePresignedUrl(dbUser.getAvatar()));

        Map<String, Object> body = new HashMap<>();
        body.put("user", dbUser);
        body.put("message", "confirmed");
        body.put("error", null);

        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @GetMapping("/logout")
    public ResponseEntity<Map<String, Object>> logoutAndCleanup(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }

        Map<String, Object> body = new HashMap<>();

        body.put("message", "logout success");
        body.put("error", null);

        return ResponseEntity.ok(body);
    }
}

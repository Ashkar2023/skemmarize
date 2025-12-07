package skemmarize.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;

import com.nimbusds.jwt.JWTClaimsSet;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import skemmarize.security.JwtTokenService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private JwtTokenService jwtTokenService;

    @GetMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshJwtToken(
            HttpServletRequest request,
            HttpServletResponse response){

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
        String email = claims.getSubject();
        if (email == null) {
            Map<String, Object> body = new HashMap<>();
            body.put("message", null);
            body.put("error", "Invalid token: missing email");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }

        String newAccessToken = jwtTokenService.generateAccessTokenFromEmail(email);

        // Set new access token cookie
        Cookie accessTokenCookie = new Cookie("ajwt", newAccessToken);
        accessTokenCookie.setHttpOnly(true);
        // accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(15 * 60); // 15 minutes
        response.addCookie(accessTokenCookie);
        
        // Return success response
        Map<String, Object> body = new HashMap<>();
        body.put("message", "access token refreshed");
        body.put("error", null);

        return ResponseEntity.ok(body);
    }
}

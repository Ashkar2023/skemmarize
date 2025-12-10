package skemmarize.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nimbusds.jwt.JWTClaimsSet;

import skemmarize.exception.JwtValidationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenService jwtTokenService;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final Cookie[] cookies = request.getCookies();

        String accessToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("ajwt")) {
                    accessToken = cookie.getValue();
                }
            }
        }

        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        JWTClaimsSet claimsSet = jwtTokenService.validateToken(accessToken, false);

        if (claimsSet == null) {
            throw new JwtValidationException("Invalid or expired JWT token");
        }

        String email = (String) claimsSet.getClaim("email");
        String userId = claimsSet.getSubject();

        JwtPrincipal jwtPrincipal = new JwtPrincipal(Long.parseLong(userId), email); 
        
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(jwtPrincipal, null, Collections.emptyList());

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }   


}

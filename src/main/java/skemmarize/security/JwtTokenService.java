package skemmarize.security;

import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import skemmarize.security.oauth2.CustomOAuth2User;

@Service
public class JwtTokenService {
    @Value("${app.jwt.secret}")
    private String JwtSecret;

    // 15 minutes for Access Token
    @Value("${jwt.expiration.access}")
    private long jwtExpirationAccess;

    // 7 days for Refresh Token
    @Value("${jwt.expiration.refresh}")
    private long jwtExpirationRefresh;

    private static final JWSAlgorithm JWT_SIGN_ALGORITHM = JWSAlgorithm.HS256;

    private byte[] getSigningKeyBytes(String secret) {
        return Base64.getDecoder().decode(secret);
    }

    private String generateToken(CustomOAuth2User user, Boolean isRefresh) {
        long expirationMs = isRefresh ? this.jwtExpirationRefresh : this.jwtExpirationAccess;

        Map<String, Object> claims = new HashMap<>();
        System.out.println("JWT Claims: "+claims);

        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationMs);

        try {
            JWTClaimsSet claimSet = new JWTClaimsSet.Builder()
            .subject(user.getEmail())
            .issueTime(now)
            .expirationTime(expirationDate)
            .build();

            JWSHeader header = new JWSHeader.Builder(JWT_SIGN_ALGORITHM).build();

            SignedJWT jwt = new SignedJWT(header, claimSet);

            MACSigner signer = new MACSigner(getSigningKeyBytes(this.JwtSecret));

            jwt.sign(signer);

            return jwt.serialize();

        } catch (Exception e) {
            throw new RuntimeException("Error generating JWT: " + e.getMessage(), e);
        }        
    }

    public String generateAccessToken(CustomOAuth2User user){
        return this.generateToken(user, false);
    }

    public String generateRefreshToken(CustomOAuth2User user){
        return this.generateToken(user, true);
    }

    public JWTClaimsSet validateToken(String token, boolean isRefresh) {
        byte[] keyBytes = getSigningKeyBytes(this.JwtSecret);

        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            JWSVerifier verifier = new MACVerifier(keyBytes);
            if (!signedJWT.verify(verifier)) {
                System.err.println("JWT Validation Error: Signature check failed.");
                return null;
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            Date exp = claims.getExpirationTime();
            if (exp == null) {
                System.err.println("JWT Validation Error: Missing expiration claim.");
                return null;
            }

            Date now = new Date();
            if (exp.before(now)) {
                System.err.println("JWT Validation Error: Token expired.");
                return null;
            }

            return claims;
        } catch (ParseException | JOSEException e) {
            System.err.println("JWT Validation Error: Token parsing or cryptographic error: " + e.getMessage());
            return null;
        }
    }

}

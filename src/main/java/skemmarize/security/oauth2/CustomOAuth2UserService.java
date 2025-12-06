package skemmarize.security.oauth2;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private static final String GITHUB_EMAIL_ENDPOINT = "https://api.github.com/user/emails";

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User defaultOAuth2User = super.loadUser(userRequest);
        
        String accessToken = userRequest.getAccessToken().getTokenValue();

        List<Map<String,Object>> emails = fetchGithubEmails(accessToken);

        String primaryEmail = emails.stream()
                            .filter(emailsMap-> (Boolean) emailsMap.get("verified") && (Boolean) emailsMap.get("primary"))
                            .map(emailMap-> (String) emailMap.get("email"))
                            .findFirst()
                            .orElse(null);

        Map<String,Object> attributes = new LinkedHashMap<>(defaultOAuth2User.getAttributes());
        
        if(primaryEmail != null){
            attributes.put("email", primaryEmail);
        }else{
            attributes.put("email", attributes.get("email"));
        }

        return new CustomOAuth2User(
            defaultOAuth2User.getAuthorities(),
            attributes,
            "email"
        );
    }

    private List<Map<String, Object>> fetchGithubEmails(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(accessToken);
        HttpEntity<?> httpEntity = new HttpEntity<>(httpHeaders);

        ParameterizedTypeReference<List<Map<String, Object>>> typeRef = new ParameterizedTypeReference<List<Map<String, Object>>>() {
        };

        return restTemplate.exchange(
                GITHUB_EMAIL_ENDPOINT,
                HttpMethod.GET,
                httpEntity,
                typeRef).getBody();
    }
}

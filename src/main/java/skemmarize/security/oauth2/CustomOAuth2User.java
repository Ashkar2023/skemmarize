package skemmarize.security.oauth2;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User extends DefaultOAuth2User {

    public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities, 
                            Map<String, Object> attributes, String nameAttributeKey) {
        super(authorities, attributes, nameAttributeKey);
    }
    
    // You can add a convenience method to easily get the email
    public String getEmail() {
        return getAttribute("email");
    }
}
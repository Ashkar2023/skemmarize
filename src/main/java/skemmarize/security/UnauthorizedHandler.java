package skemmarize.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class UnauthorizedHandler implements AuthenticationEntryPoint{
    
    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException{

        try {
            HandlerExecutionChain handler = handlerMapping.getHandler(request);
            System.out.println(handler);

            if(handler == null){
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("application/json");
                response.getWriter().write(
                    "{ \"error\": \"Not Found\", \"message\": \"The requested resource was not found.\" }"  
                );
                return;
            }else{
                
            }
        } catch (Exception e) {
            // If we can't determine handler, fall through to 401
        }
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{ \"error\": \"Unauthorized\", \"message\": \"Authentication is required to access this resource.\" }");
    }
}

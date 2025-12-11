package skemmarize.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.genai.Client;

import jakarta.annotation.PreDestroy;

@Configuration
public class GeminiConfig {

    private Client client;
    
    @Bean
    public Client geminiClient(){
        this.client = Client.builder().apiKey(System.getProperty("GOOGLE_API_KEY")).build();
        return this.client;
    }

    @PreDestroy
    public void cleanup() {
        if(this.client!=null){
            this.client.close();
            System.out.println("Gemini Ai connection closed!");
        }
    }
}
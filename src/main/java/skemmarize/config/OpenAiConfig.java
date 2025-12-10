// package skemmarize.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// import com.openai.client.OpenAIClient;
// import com.openai.client.okhttp.OpenAIOkHttpClient;

// import jakarta.annotation.PreDestroy;

// @Configuration
// public class OpenAiConfig {

//     private OpenAIClient openAIClient;

//     @Bean
//     public OpenAIClient openAiClient() {
//         openAIClient = OpenAIOkHttpClient.builder().apiKey(System.getProperty("OPENAI_API_KEY")).build();
//         return openAIClient;
//     }

//     @PreDestroy
//     public void close() {
//         openAIClient.close();
//     }
// }

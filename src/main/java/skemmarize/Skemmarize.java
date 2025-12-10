package skemmarize;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class Skemmarize {

    public static void main(String[] args) {

        Dotenv env = Dotenv.load();
        
        // Load all .env variables as system properties
        env.entries().forEach(e -> 
            System.setProperty(e.getKey(), e.getValue())
        );
        
        String awsAccessKey = env.get("AWS_ACCESS_KEY_ID");
        String awsSecretKey = env.get("AWS_SECRET_ACCESS_KEY");
        System.setProperty("OPENAI_API_KEY", env.get("OPENAI_API_KEY"));

        if (awsAccessKey != null) {
            System.setProperty("aws.accessKeyId", awsAccessKey);
        }
        if (awsSecretKey != null) {
            System.setProperty("aws.secretAccessKey", awsSecretKey);
        }

        SpringApplication.run(Skemmarize.class, args);
    }

}

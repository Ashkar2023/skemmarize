package skemmarize;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class Skemmarize {

    public static void main(String[] args) {

        Dotenv env = Dotenv.load();
        env.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));

        SpringApplication.run(Skemmarize.class, args);
    }

}

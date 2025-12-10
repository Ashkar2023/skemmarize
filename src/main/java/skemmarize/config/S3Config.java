package skemmarize.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    private S3Client s3;

    @Bean
    public S3Client s3Client(){
        this.s3 = S3Client.builder()
                .region(Region.AP_SOUTH_1)
                .credentialsProvider(SystemPropertyCredentialsProvider.create())
                .build();

        return this.s3;
    }

    @PreDestroy
    public void cleanup() {
        if(this.s3!=null){
            this.s3.close();
            System.out.println("aws S3 connection closed!");
        }
    }
}

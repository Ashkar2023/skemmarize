package skemmarize.external;

import java.io.IOException;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Component
public class ImageProcessor {
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;
    
    public ImageProcessor(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String generatePresignedUrl(String objectKey) {
        Long expirySeconds = 60L * 60L;

        S3Presigner presigner = S3Presigner.builder()
                .region(s3Client.serviceClientConfiguration().region())
                .credentialsProvider(s3Client.serviceClientConfiguration().credentialsProvider())
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(expirySeconds))
                .getObjectRequest(
                        GetObjectRequest.builder()
                                .bucket(bucket)
                                .key(objectKey)
                                .build())
                .build();

        String url = presigner.presignGetObject(presignRequest).url().toString();
        presigner.close();
        
        return url;
    }

    public static String encodeImageToBase64(MultipartFile image) throws IOException {
        byte[] imageBytes = image.getBytes();

        return java.util.Base64.getEncoder().encodeToString(imageBytes);
    }

    public static String encodeImageToBase64(byte[] imageBytes) throws IOException {
        return java.util.Base64.getEncoder().encodeToString(imageBytes);
    }
}

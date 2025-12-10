package skemmarize.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
public class ImageUploader {

    @Value("${aws.s3.bucket}")
    private String bucket;

    private final S3Client s3Client;

    public ImageUploader(S3Client s3) {
        this.s3Client = s3;
    }

    public String upload(String key, String folder, byte[] data) {

        String filename = createKey(key);
        String fullPath = folder + "/" + filename;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(this.bucket)
                        .key(fullPath)
                        .contentType("image/jpeg")
                        .build(),
                RequestBody.fromBytes(data));

        return fullPath;
    }

    private String createKey(String key) {

        if (key == null || key.isBlank()) {
            key = "file-";
        }

        String cleanedKey = key.replaceAll("[^a-zA-Z0-9-_]", "-").replaceAll("-{2,}", "-").replaceAll("^-|-$", "");

        String timestampString = String.valueOf(System.currentTimeMillis());
        return (timestampString + cleanedKey).toLowerCase();
    }
}
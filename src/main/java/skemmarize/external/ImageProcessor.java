package skemmarize.external;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ImageProcessor {
    
    public static String encodeImageToBase64(MultipartFile image) throws IOException {
        byte[] imageBytes = image.getBytes();

        return java.util.Base64.getEncoder().encodeToString(imageBytes);
    }
}

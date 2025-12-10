package skemmarize.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import skemmarize.exception.NotFoundException;
import skemmarize.external.ImageUploader;
import skemmarize.model.User;
import skemmarize.repository.UserRepository;

@Service
public class UserService {

    private final String AVATAR_IMAGE_FOLDER = "avatars";

    private final UserRepository userRepository;
    private final ImageUploader imageUploader;

    public UserService(UserRepository userRepository, ImageUploader iu) {
        this.userRepository = userRepository;
        this.imageUploader = iu;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User createUser(String email, String username, String avatarUrl) {
        // Check if user already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists with email: " + email);
        }

        try {
            return userRepository.save(email, username, (avatarUrl != null ? avatarUrl : "default"));
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("User already exists with this email", e);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("User already exists with this email", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }

    public String downloadAndUploadAvatar(String url, String username) {
        System.out.println("DAUA: " + url + " " + username);
        try {
            HttpClient httpClient = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            byte[] bytes = response.body();
            Optional<String> contentType = response.headers().firstValue("content-type");

            String ext = contentType.isPresent() ? "." + contentType.get().split("/")[1] : "";

            return this.imageUploader.upload(username + ext, AVATAR_IMAGE_FOLDER, bytes);
        } catch (Exception e) {
            throw new RuntimeException("Avatar upload failed: " + e.getMessage());
        }
    }

}

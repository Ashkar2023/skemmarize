package skemmarize.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import skemmarize.exception.NotFoundException;
import skemmarize.model.User;
import skemmarize.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User createUser(String email, String username) {
        // Check if user already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists with email: " + email);
        }

        try {
            return userRepository.save(email, username);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("User already exists with this email", e);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("User already exists with this email", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }


}

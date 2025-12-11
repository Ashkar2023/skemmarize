package skemmarize.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import skemmarize.service.UserService;
import skemmarize.model.User;

@RestController
@RequestMapping("users")
public class UserController {

    private UserService userService;

    public UserController(UserService us){
        this.userService = us;
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {

        User user = this.userService.getUserByEmail(email);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}

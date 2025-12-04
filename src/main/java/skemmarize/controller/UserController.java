package skemmarize.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class UserController {

    @GetMapping
    public ResponseEntity<String> helloWorld() {
        return ResponseEntity.ok("hello world");
    }

}

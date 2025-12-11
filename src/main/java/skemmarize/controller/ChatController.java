package skemmarize.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import skemmarize.exception.BadRequestException;
import skemmarize.model.Chat;
import skemmarize.security.JwtPrincipal;
import skemmarize.service.ChatService;

@RestController
@RequestMapping("chats")
public class ChatController {

    private ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(value = "/summarize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Chat> summarize(
            @RequestPart("image") MultipartFile image,
            @AuthenticationPrincipal JwtPrincipal principal) {

        if (image.isEmpty()) {
            System.out.println("--- image empty");
            throw new BadRequestException("image file is missing");
        }

        Chat chat = chatService.generateSummary(principal.getUserId(), image);

        return ResponseEntity.status(HttpStatus.OK).body(chat);
    }

    @GetMapping("/history")
    public ResponseEntity<List<Chat>> getChatHistory(
            @AuthenticationPrincipal JwtPrincipal principal) {

        List<Chat> chats = chatService.getChatHistory(principal.getUserId());

        return ResponseEntity.status(HttpStatus.OK).body(chats);
    }
}

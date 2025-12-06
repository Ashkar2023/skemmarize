package skemmarize.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import skemmarize.external.AiSummarizer;
import skemmarize.model.Chat;
import skemmarize.repository.ChatRepository;

@Service
public class ChatService {

    private ChatRepository chatRepository;
    private AiSummarizer aiSummarizer;

    public ChatService(ChatRepository chatRepository, AiSummarizer aiSummarizer) {
        this.chatRepository = chatRepository;
        this.aiSummarizer = aiSummarizer;
    }
    
    public Chat generateSummary(MultipartFile image) {
        try {
            String summary = aiSummarizer.generate(image.getBytes(), null);
            
            // TODO: Create and save Chat in DB
            Chat chat = chatRepository.save(1L, "blank", summary);
            
            return chat;
        } catch (IOException e) {
            throw new RuntimeException("Image processing failed: "+e.getMessage(), e);
        }
    }

}

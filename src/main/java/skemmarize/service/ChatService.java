package skemmarize.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import skemmarize.external.AiSummarizer;
import skemmarize.external.ImageProcessor;
import skemmarize.external.ImageUploader;
import skemmarize.model.Chat;
import skemmarize.repository.ChatRepository;

@Service
public class ChatService {

    private ChatRepository chatRepository;
    private AiSummarizer aiSummarizer;
    private ImageUploader imageUploader;
    private ImageProcessor imageProcessor;

    private final String CHAT_IMAGE_FOLDER = "images";

    public ChatService(
            ChatRepository chatRepository, AiSummarizer aiSummarizer,
            ImageUploader imageUploader, ImageProcessor imageProcessor) {
        this.chatRepository = chatRepository;
        this.aiSummarizer = aiSummarizer;
        this.imageUploader = imageUploader;
        this.imageProcessor = imageProcessor;
    }

    public Chat generateSummary(Long userId, MultipartFile image) {
        try {
            byte[] imageBytes = image.getBytes();
            String filename = this.imageUploader.upload(image.getOriginalFilename(), CHAT_IMAGE_FOLDER, imageBytes);

            String summary = aiSummarizer.generate(imageBytes, null);

            Chat chat = chatRepository.save(userId, filename, summary);

            return chat;
        } catch (IOException e) {
            throw new RuntimeException("Image processing failed: " + e.getMessage(), e);
        }
    }

    private Chat mapPresignedUrls(Chat chat){
        String presignedImageUrl = this.imageProcessor.generatePresignedUrl(chat.getImageUrl());
        chat.setImageUrl(presignedImageUrl);

        return chat;
    }

    public List<Chat> getChatHistory(Long userId) {
        List<Chat> chats = chatRepository.findChatsByUserId(userId);

        return chats.stream().map(this::mapPresignedUrls).toList();
    }

}

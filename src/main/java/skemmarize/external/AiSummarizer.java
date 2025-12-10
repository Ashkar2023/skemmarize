package skemmarize.external;


import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.stereotype.Component;

@Component
public class AiSummarizer{
    private final Client client;
    private static final String MODEL_NAME = "gemini-2.5-flash";
    private final String defaultPrompt = "Summarize this image strictly in no more than 100 words. The response should be in plain text.";

    public AiSummarizer(Client gClient){       
        this.client = gClient;
    }

    public String generate(byte[] imageBytes, String promptOverride){
        if (promptOverride == null) {
            promptOverride = this.defaultPrompt;
        }
        // byte[] imageBytes = Base64.getDecoder().decode(base64Image);

        Content content = Content.fromParts(
            Part.fromText(promptOverride),
            Part.fromBytes(imageBytes, "image/jpeg")
        );
        
        GenerateContentResponse response = this.client.models.generateContent(MODEL_NAME, content, null);
        return response.text();
    }
}

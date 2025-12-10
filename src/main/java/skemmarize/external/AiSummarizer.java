package skemmarize.external;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class AiSummarizer {
    // private final Client client;
    // private static final String MODEL_NAME = "gemini-2.5-flash";

    private static final String MODEL_NAME = "gpt-5-nano";

    private final String defaultPrompt = "Summarize this image strictly in no more than 100 words. The response should be in plain text.";

    private final String uri = "https://api.openai.com/v1/responses";

    // public AiSummarizer(OpenAIClient openAIClient) {
    // this.client = openAIClient;
    // }

    // public String generate(byte[] imageBytes, String prompt) {
    // if (prompt == null) {
    // prompt = this.defaultPrompt;
    // }
    // // byte[] imageBytes = Base64.getDecoder().decode(base64Image);

    // Content content = Content.fromParts(
    // Part.fromText(prompt),
    // Part.fromBytes(imageBytes, "image/jpeg"));

    // GenerateContentResponse response =
    // this.client.models.generateContent(MODEL_NAME, content, null);
    // return response.text();
    // }

    public String generate(byte[] imageBytes, String promptOverride) {
        if (promptOverride == null) {
            promptOverride = this.defaultPrompt;
        }

        String imageBase64;

        try {
            imageBase64 = ImageProcessor.encodeImageToBase64(imageBytes);
        } catch (Exception e) {
            throw new RuntimeException("image encoding failed: " + e.getMessage());
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Map<String, Object> requestBody = buildBody(imageBase64, promptOverride);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("Authorization", "Bearer " + System.getProperty("OPENAI_API_KEY"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Step 6: Check response status
            if (response.statusCode() != 200) {
                throw new RuntimeException("API request failed with status: " + response.statusCode() +
                        ", body: " + response.body());
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> output = (List<Map<String, Object>>) responseMap.get("output");

            if (output != null && !output.isEmpty()) {
                Map<String, Object> message = output.stream()
                        .filter(obj -> "message".equals(obj.get("type")))
                        .findFirst()
                        .orElse(null);

                if (!message.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> content = (List<Map<String, Object>>) message.get("content");
                    if (content != null && !content.isEmpty()) {
                        return (String) content.get(0).get("text");
                    }
                }
            }

            throw new RuntimeException("No content in response");

        } catch (IOException |

                InterruptedException e) {
            throw new RuntimeException("API request failed: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> buildBody(String base64Image, String prompt) {
        Map<String, Object> body = new HashMap<>();

        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "input_text");
        textContent.put("text", prompt);

        Map<String, Object> imageContent = new HashMap<>();
        imageContent.put("type", "input_image");
        imageContent.put("image_url", "data:image/jpeg;base64," + base64Image);

        Map<String, Object> inputMap = new HashMap<>();

        inputMap.put("role", "user");
        inputMap.put("content", List.of(textContent, imageContent));

        body.put("model", MODEL_NAME);
        body.put("input", List.of(inputMap));

        return body;
    }
}

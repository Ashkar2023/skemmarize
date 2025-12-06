package skemmarize.model;

public class Chat {
    private Long id;    
    private Long userId;
    private String imageUrl;
    private String response;

    
    public Chat(Long id, Long userId, String imageUrl, String response) {
        this.id = id;
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.response = response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

}

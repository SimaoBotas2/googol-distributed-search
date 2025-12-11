package external_api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Cliente para Hacker News API
 * Fetch top stories e filter por query
 */
@Component
public class HackerNewsClient {

    private static final String HN_API_URL = "https://hacker-news.firebaseio.com/v0";
    private static final Gson gson = new Gson();
    private final HttpClient httpClient;

    public HackerNewsClient() {
        this.httpClient = HttpClients.createDefault();
    }

    /**
     * Obter top 30 stories de Hacker News
     */
    public List<HackerNewsStory> getTopStories() throws Exception {
        List<HackerNewsStory> stories = new ArrayList<>();

        try {
            // 1. Fetch IDs dos top stories
            String topStoriesUrl = HN_API_URL + "/topstories.json";
            String idsJson = fetchUrl(topStoriesUrl);

            if (idsJson == null || idsJson.isEmpty()) {
                return stories;
            }

            JsonArray ids = gson.fromJson(idsJson, JsonArray.class);
            int count = 0;

            // 2. Para cada ID, fetch detalhes da story
            for (JsonElement element : ids) {
                if (count >= 10) break; // Máximo 10 stories

                int storyId = element.getAsInt();
                try {
                    HackerNewsStory story = getStory(storyId);
                    if (story != null && story.getUrl() != null && !story.getUrl().isEmpty()) {
                        stories.add(story);
                        count++;
                    }
                } catch (Exception e) {
                    // Skip se houver erro ao fetch story individual
                    System.err.println("Erro ao fetch story " + storyId + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Erro ao fetch top stories: " + e.getMessage());
        }


        return stories;
    }

    /**
     * Obter detalhes de uma story por ID
     */
    private HackerNewsStory getStory(int id) throws Exception {
        String storyUrl = HN_API_URL + "/item/" + id + ".json";
        String json = fetchUrl(storyUrl);

        if (json == null || json.isEmpty()) {
            return null;
        }

        JsonObject obj = gson.fromJson(json, JsonObject.class);

        HackerNewsStory story = new HackerNewsStory();
        story.setId(obj.has("id") ? obj.get("id").getAsInt() : 0);
        story.setTitle(obj.has("title") ? obj.get("title").getAsString() : "");
        story.setUrl(obj.has("url") ? obj.get("url").getAsString() : "");
        story.setScore(obj.has("score") ? obj.get("score").getAsInt() : 0);
        story.setBy(obj.has("by") ? obj.get("by").getAsString() : "");

        return story;
    }

    /**
     * Fazer HTTP GET request e retornar response como String
     */
    private String fetchUrl(String url) throws Exception {
        ClassicHttpRequest request = ClassicRequestBuilder.get(url).build();

        return httpClient.execute(request, response -> {
            if (response.getCode() == 200) {
                HttpEntity entity = response.getEntity();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(entity.getContent())
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                return sb.toString();
            }
            return null;
        });
    }

    /**
     * DTO para Hacker News Story
     */
    public static class HackerNewsStory {
        private int id;
        private String title;
        private String url;
        private int score;
        private String by;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public String getBy() {
            return by;
        }

        public void setBy(String by) {
            this.by = by;
        }
    }
}

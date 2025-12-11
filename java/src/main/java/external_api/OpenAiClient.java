package external_api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Cliente para OpenAI API
 * Gera snippets contextualizados com GPT-4 
 */
@Component
public class OpenAiClient {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/responses";
    private static final Gson gson = new Gson();
    private final HttpClient httpClient;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final java.util.concurrent.ConcurrentHashMap<String, String> snippets = new java.util.concurrent.ConcurrentHashMap<>();

    @Value("${openai.api.key:}")
    private String apiKey;

    public OpenAiClient() {
        this.httpClient = HttpClients.createDefault();
    }

    public String getSnippet(String url) {
        return snippets.getOrDefault(url, null);
    }

    public String generateSnippet(String url, String content) {
        // Se não há API key configurada, retornar placeholder
        if (apiKey == null || apiKey.isEmpty()) {
            return "Snippet Indisponível";
        }

        // Chamar OpenAI em background (não-bloqueante)
        executor.submit(() -> {
            try {
                String truncatedContent = content;
                if (content.length() > 500) {
                    truncatedContent = content.substring(0, 500) + "...";
                }

                String prompt = "Gera um snippet de máximo 150 caracteres em português para o seguinte conteúdo de página web:\n\n" +
                        truncatedContent;

                callOpenAiAPI(url, prompt);
            } catch (Exception e) {
                System.err.println("[OpenAiClient] Erro ao gerar snippet em background: " + e.getMessage());
            }
        });

        // Retornar imediatamente sem esperar
        return "Snippet Indisponível, openAI erro";
    }

    /**
     * Chamar OpenAI API (executa em background)
     */
    private void callOpenAiAPI(String url, String prompt) throws Exception {
        try {
            // Build request body
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", "gpt-4.1");
            requestBody.addProperty("input", prompt);
            requestBody.addProperty("max_output_tokens", 150);

            String jsonBody = gson.toJson(requestBody);

            System.out.println("[OpenAiClient] Enviando request para OpenAI...");
            System.out.println("[OpenAiClient] API Key presente: " + (apiKey != null && !apiKey.isEmpty()));
            System.out.println("[OpenAiClient] Payload size: " + jsonBody.length() + " bytes");

            // Build HTTP request
            ClassicHttpRequest request = ClassicRequestBuilder.post(OPENAI_API_URL)
                    .setHeader("Authorization", "Bearer " + apiKey)
                    .setHeader("Content-Type", "application/json")
                    .setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON))
                    .build();

            // Execute request
            httpClient.execute(request, response -> {
                try {
                    int statusCode = response.getCode();
                    System.out.println("[OpenAiClient] Response status: " + statusCode);

                    if (statusCode == 200) {
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

                        // Parse JSON response
                        String responseBody = sb.toString();
                        JsonObject responseObj = gson.fromJson(responseBody, JsonObject.class);

                    if (responseObj.has("output_text")) {
                        String content = responseObj.getAsJsonArray("output_text")
                                .get(0).getAsString();

                        // Guardar snippet em memória
                        snippets.put(url, content);

                        System.out.println("[OpenAiClient] Snippet guardado para: " + url);
                        System.out.println("[OpenAiClient] Snippet: " + content.substring(0, Math.min(50, content.length())) + "...");
                    }

                        return null;
                    } else if (statusCode == 429) {
                        System.err.println("[OpenAiClient] Rate limited (429)");
                        return null;
                    } else if (statusCode == 401) {
                        System.err.println("[OpenAiClient] Unauthorized (401) - Verificar API key!");
                        return null;
                    } else {
                        System.err.println("[OpenAiClient] Erro HTTP " + statusCode);
                        // Ler resposta de erro
                        HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(entity.getContent())
                            );
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                sb.append(line);
                            }
                            reader.close();
                            System.err.println("[OpenAiClient] Erro response: " + sb.toString());
                        }
                        return null;
                    }
                } catch (Exception e) {
                    System.err.println("[OpenAiClient] Erro ao processar resposta: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            });

        } catch (Exception e) {
            System.err.println("[OpenAiClient] Erro ao chamar OpenAI: " + e.getMessage());
        }
    }
}

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
 * Cliente para Ollama API
 * Gera snippets contextualizados com modelos locais via Ollama
 */
@Component
public class OllamaClient {

    private static final String OLLAMA_MODEL = "gemma3:270m"; // ou "llama2", "mistral", etc.
    private static final Gson gson = new Gson();
    private final HttpClient httpClient;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final java.util.concurrent.ConcurrentHashMap<String, String> snippets = new java.util.concurrent.ConcurrentHashMap<>();

    @Value("${ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    public OllamaClient() {
        this.httpClient = HttpClients.createDefault();
    }

    public String getSnippet(String url) {
        return snippets.getOrDefault(url, null);
    }

    public String generateSnippet(String url, String content) {
        // Chamar Ollama em background (não-bloqueante)
        executor.submit(() -> {
            try {
                String truncatedContent = content;
                if (content.length() > 500) {
                    truncatedContent = content.substring(0, 500) + "...";
                }   

                String prompt = "Gera um snippet de máximo 150 caracteres em português para o seguinte conteúdo de página web:\n\n" +
                        truncatedContent;

                callOllamaAPI(url, prompt);
            } catch (Exception e) {
                System.err.println("[OllamaClient] Erro ao gerar snippet em background: " + e.getMessage());
            }
        });

        // Retornar imediatamente sem esperar
        return "Snippet Indisponível, Ollama processando";
    }

    /**
     * Chamar Ollama API (executa em background)
     */
    private void callOllamaAPI(String url, String prompt) throws Exception {
        try {
            // Build request body para Ollama
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", OLLAMA_MODEL);
            requestBody.addProperty("prompt", prompt);
            requestBody.addProperty("stream", false);

            String jsonBody = gson.toJson(requestBody);

            System.out.println("[OllamaClient] Enviando request para Ollama...");
            System.out.println("[OllamaClient] URL: " + ollamaUrl + "/api/generate");
            System.out.println("[OllamaClient] Payload size: " + jsonBody.length() + " bytes");

            // Build HTTP request
            ClassicHttpRequest request = ClassicRequestBuilder.post(ollamaUrl + "/api/generate")
                    .setHeader("Content-Type", "application/json")
                    .setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON))
                    .build();

            // Execute request
            httpClient.execute(request, response -> {
                try {
                    int statusCode = response.getCode();
                    System.out.println("[OllamaClient] Response status: " + statusCode);

                    if (statusCode == 200) {
                        HttpEntity entity = response.getEntity();
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(entity.getContent())
                        );
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            // Se a linha não está vazia, é um JSON válido
                            if (!line.trim().isEmpty()) {
                                sb.append(line);
                            }
                        }
                        reader.close();

                        // Parse JSON response from Ollama
                        String responseBody = sb.toString();
                        System.out.println("[OllamaClient] Raw response: " + responseBody);
                        
                        JsonObject responseObj = gson.fromJson(responseBody, JsonObject.class);

                        if (responseObj.has("response")) {
                            String content = responseObj.get("response").getAsString();

                            // Guardar snippet em memória
                            snippets.put(url, content);

                            System.out.println("[OllamaClient] Snippet guardado para: " + url);
                            System.out.println("[OllamaClient] Snippet: " + content.substring(0, Math.min(50, content.length())) + "...");
                        }

                        return null;
                    } else {
                        System.err.println("[OllamaClient] Erro HTTP " + statusCode);
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
                            System.err.println("[OllamaClient] Erro response: " + sb.toString());
                        }
                        return null;
                    }
                } catch (Exception e) {
                    System.err.println("[OllamaClient] Erro ao processar resposta: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            });

        } catch (Exception e) {
            System.err.println("[OllamaClient] Erro ao chamar Ollama: " + e.getMessage());
        }
    }
}

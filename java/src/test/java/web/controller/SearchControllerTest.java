package web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes para SearchController
 * Valida todos os 6 endpoints principais
 */
@SpringBootTest
@AutoConfigureMockMvc
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Teste GET / - Homepage
     */
    @Test
    void testHomeEndpoint() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    /**
     * Teste GET /index - Página de indexação
     */
    @Test
    void testIndexPage() throws Exception {
        mockMvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("index-url"));
    }

    /**
     * Teste POST /search - Pesquisa com query
     */
    @Test
    void testSearchEndpoint() throws Exception {
        mockMvc.perform(post("/search")
                .param("query", "test")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("results"))
                .andExpect(model().attributeExists("query"))
                .andExpect(model().attributeExists("results"))
                .andExpect(model().attributeExists("responseTime"));
    }

    /**
     * Teste POST /search - Pesquisa sem parâmetros (use defaults)
     */
    @Test
    void testSearchEndpointWithDefaults() throws Exception {
        mockMvc.perform(post("/search")
                .param("query", "exemplo"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("page", 1))
                .andExpect(model().attribute("size", 10));
    }

    /**
     * Teste GET /stats - Estatísticas do sistema
     */
    @Test
    void testStatsEndpoint() throws Exception {
        mockMvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andExpect(view().name("stats"));
    }

    /**
     * Teste GET /links - Páginas com ligações para URL
     */
    @Test
    void testLinksEndpoint() throws Exception {
        mockMvc.perform(get("/links")
                .param("url", "http://example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("links"))
                .andExpect(model().attributeExists("url"));
    }

    /**
     * Teste GET /hacker-news - Top stories de Hacker News
     */
    @Test
    void testHackerNewsEndpoint() throws Exception {
        mockMvc.perform(get("/hacker-news"))
                .andExpect(status().isOk())
                .andExpect(view().name("hacker-news"))
                .andExpect(model().attributeExists("stories"));
    }

    /**
     * Teste POST /index - Indexar novo URL
     */
    @Test
    void testIndexURLEndpoint() throws Exception {
        mockMvc.perform(post("/index")
                .param("url", "http://example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("index-url"));
    }
}

package web.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import gateway.SystemStats;
import gateway.SearchResult;
import web.service.RMIClientService;

@Controller
public class SearchController {

    @Autowired
    private RMIClientService rmiService;

    @GetMapping("/")
    public String home(Model model) {
        return "index";
    }

    @PostMapping("/search")
    public String search(@RequestParam String query,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size,
                         Model model) {

        System.out.println("[SearchController] ===== PESQUISA INICIADA =====");
        System.out.println("[SearchController] Query: " + query + ", Page: " + page + ", Size: " + size);

        // Inicializar com lista vazia por defeito
        List<ResultItem> results = new ArrayList<>();
        boolean hasNext = false;
        boolean hasPrev = page > 1;
        int totalMatches = 0;

        try {
            // Calcular offset da página (página começa em 1, mas offset começa em 0)
            int offset = (page - 1) * size;
            System.out.println("[SearchController] Offset calculado: " + offset);
            
            // BACKEND devolve SearchResult com URLs já paginados E total de matches
            System.out.println("[SearchController] A chamar rmiService.searchPaginated()...");
            SearchResult searchResult = rmiService.searchPaginated(query, size, offset);
            System.out.println("[SearchController] rmiService.searchPaginated() retornou " + searchResult.getResults().size() + " URLs de " + searchResult.getTotalMatches() + " total");

            totalMatches = searchResult.getTotalMatches();
            List<String> urls = searchResult.getResults();

            // Converter para objetos visíveis no HTML
            for (String url : urls) {
                ResultItem item = new ResultItem();
                item.url = url;
                item.title = "Título ainda não disponível"; 
                item.snippet = "Snippet será gerado na integração OpenAI";
                results.add(item);
            }

            // Atributos para paginação no template
            hasNext = (offset + size) < totalMatches;  // Há próxima página se não chegámos ao fim
            System.out.println("[SearchController] Pesquisa completada com " + results.size() + " resultados, hasNext=" + hasNext);

        } catch (Exception e) {
            System.err.println("[SearchController] ❌ ERRO na pesquisa: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Erro ao realizar pesquisa: " + e.getMessage());
        }

        model.addAttribute("query", query);
        model.addAttribute("results", results);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("hasNext", hasNext);
        model.addAttribute("hasPrev", hasPrev);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalMatches", totalMatches);

        System.out.println("[SearchController] ===== PESQUISA FINALIZADA =====");
        return "results";
    }

    @GetMapping("/index")
    public String indexPage(Model model) {
        return "index-url";
    }

    @PostMapping("/index")
    public String indexURL(@RequestParam String url, Model model) {
        try {
            rmiService.indexURL(url);
            model.addAttribute("message", "URL indexada com sucesso: " + url);
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao indexar URL: " + e.getMessage());
        }
        return "index-url";
    }

    @GetMapping("/stats")
    public String stats(Model model) {
        try {
            SystemStats stats = rmiService.getSystemStats();
            model.addAttribute("stats", stats);
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao obter estatísticas: " + e.getMessage());
        }
        return "stats";
    }

    @GetMapping("/error")
    public String error(Model model) {
        return "error";
    }

    public static class ResultItem {
        public String url;
        public String title;
        public String snippet;
    }
}

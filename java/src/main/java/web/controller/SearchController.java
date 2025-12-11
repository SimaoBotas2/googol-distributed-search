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
    public String search(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        long startTime = System.currentTimeMillis();  // Iniciar cronómetro

        List<ResultItem> results = new ArrayList<>();
        int totalMatches = 0;

        boolean hasPrev = page > 1;
        boolean hasNext = false;

        try {
            int offset = (page - 1) * size;

            SearchResult result = rmiService.searchPaginated(query, size, offset);
            totalMatches = result.getTotalMatches();

            // Apenas URLs — SEM SNIPPETS, SEM TÍTULOS GERADOS
            for (String url : result.getResults()) {
                ResultItem item = new ResultItem();
                item.url = url;
                item.title = url;
                item.snippet = "Snippet será gerado na integração OpenAI";
                results.add(item);
            }

            hasNext = (offset + size) < totalMatches;

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erro ao realizar pesquisa: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();  // Parar cronómetro
        long responseTimeMs = endTime - startTime;
        double responseTimeDecimas = responseTimeMs / 100.0;  // Converter para décimas de segundo

        model.addAttribute("query", query);
        model.addAttribute("results", results);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("hasPrev", hasPrev);
        model.addAttribute("hasNext", hasNext);
        model.addAttribute("totalMatches", totalMatches);
        model.addAttribute("responseTime", responseTimeDecimas);  // Passar tempo para template

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

    @GetMapping("/links")
    public String showInLinks(@RequestParam String url, Model model) {
        try {
            var inLinks = rmiService.getPagesLinkingTo(url);
            model.addAttribute("url", url);
            model.addAttribute("inLinks", inLinks);

        } catch (Exception e) {
            model.addAttribute("error", "Erro ao obter ligações: " + e.getMessage());
        }

        return "links";
    }

    public static class ResultItem {
        public String url;
        public String title;
        public String snippet;
    }
}

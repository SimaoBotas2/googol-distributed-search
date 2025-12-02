package web.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import gateway.SystemStats;
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
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {

        try {
            // BACKEND devolve List<String> com URLs
            List<String> urls = rmiService.search(query);

            // Converter para objetos visíveis no HTML
            List<ResultItem> results = new ArrayList<>();

            for (String url : urls) {
                ResultItem item = new ResultItem();
                item.url = url;
                item.title = "Título ainda não disponível"; 
                item.snippet = "Snippet será gerado na integração OpenAI";
                results.add(item);
            }

            // Paginação real
            int pageSize = 10;
            int start = page * pageSize;
            int end = Math.min(start + pageSize, results.size());
            boolean hasNext = end < results.size();

            List<ResultItem> pageResults = results.subList(start, end);

            model.addAttribute("query", query);
            model.addAttribute("results", pageResults);
            model.addAttribute("page", page);
            model.addAttribute("hasNext", hasNext);
            model.addAttribute("totalResults", results.size());

        } catch (Exception e) {
            model.addAttribute("error", "Erro ao realizar pesquisa: " + e.getMessage());
        }

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

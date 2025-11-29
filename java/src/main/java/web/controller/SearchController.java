package web.controller;

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
        // TODO: Tiago - Página inicial de pesquisa (GET /)
        return "index";
    }
    
    @PostMapping("/search")
    public String search(@RequestParam String query, Model model) {
        try{
            List<String> results = rmiService.search(query);
            int totalResults = results.size();
            
            model.addAttribute("query", query);
            model.addAttribute("results", results);
            model.addAttribute("totalResults", totalResults);
            model.addAttribute("currentPage", 1);
            model.addAttribute("totalPages", 1);
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao realizar pesquisa: " + e.getMessage());
        }
        // TODO: Tiago - Implementar paginação e ordenação dos resultados
        return "results";
    }
    
    @GetMapping("/index")
    public String indexPage(Model model) {
        // TODO: Tiago - Página para indexar URL (GET /index)
        return "index-url";
    }
    
    @PostMapping("/index")
    public String indexURL(@RequestParam String url, Model model) {
        try{ //nao tenho de fazer confirmações aqui, apenas no backend
            rmiService.indexURL(url);
            model.addAttribute("message", "URL indexada com sucesso: " + url);
            return "index-url";
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao indexar URL: " + e.getMessage());
            return "index-url";
        }
    }
    
    @GetMapping("/stats")
    public String stats(Model model) {
        try{
        SystemStats stats = rmiService.getSystemStats();
        model.addAttribute("stats", stats);     
        return "stats";
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao obter estatísticas: " + e.getMessage());
            return "stats";
        }

    }
    
    @GetMapping("/error")
    public String error(Model model) {
        // TODO: Tiago - Página de erro
        return "error";
    }
}

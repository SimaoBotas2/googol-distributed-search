package web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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
    public String search(@RequestParam String query, 
                        @RequestParam(defaultValue = "1") int page,
                        Model model) {
        // TODO: Simão - Implementar pesquisa (POST /search)
        // TODO: Tiago - Implementar paginação (por agora retorna tudo)
        // TODO: Tiago - Implementar paginação de 10 em 10
        return "results";
    }
    
    @GetMapping("/index")
    public String indexPage(Model model) {
        // TODO: Tiago - Página para indexar URL (GET /index)
        return "index-url";
    }
    
    @PostMapping("/index")
    public String indexURL(@RequestParam String url, Model model) {
        // TODO: Simão - Implementar indexação (POST /index)
        return "index-url";
    }
    
    @GetMapping("/stats")
    public String stats(Model model) {
        // TODO: Simão - Página de estatísticas (GET /stats)
        return "stats";
    }
    
    @GetMapping("/error")
    public String error(Model model) {
        // TODO: Tiago - Página de erro
        return "error";
    }
}

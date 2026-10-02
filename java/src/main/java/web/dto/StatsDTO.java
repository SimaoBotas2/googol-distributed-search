package web.dto;

import gateway.SystemStats;
import java.util.ArrayList;
import java.util.List;

public class StatsDTO {
    
    // Total de URLs processadas
    public long totalPages;
    
    // Total de palavras processadas
    public long totalKeywords;
    
    // Lista de barrels com informações sobre cada um
    public List<BarrelDTO> barrels;
    
    // Top 10 pesquisas mais frequentes
    public List<String> topSearches;

    /**
     * Construtor privado - usar factory method fromSystemStats()
     */
    private StatsDTO() {
        this.barrels = new ArrayList<>();
        this.topSearches = new ArrayList<>();
    }

    public static StatsDTO fromSystemStats(SystemStats systemStats) {
        StatsDTO dto = new StatsDTO();
        
        if (systemStats == null) {
            // Se systemStats for null, retornar DTO vazio
            dto.totalPages = 0;
            dto.totalKeywords = 0;
            dto.topSearches = new ArrayList<>();
            return dto;
        }
        
        // Mapear os campos do SystemStats para o DTO
        dto.totalPages = systemStats.totalUrls;
        dto.totalKeywords = systemStats.totalPalavras;
        
        // Converter o mapa de tamanhos por barrel para lista de BarrelDTO
        if (systemStats.tamanhoPorBarrel != null && !systemStats.tamanhoPorBarrel.isEmpty()) {
            systemStats.tamanhoPorBarrel.forEach((barrelName, keywordCount) -> {
                BarrelDTO barrelDTO = new BarrelDTO();
                barrelDTO.id = barrelName;
                barrelDTO.totalKeywords = keywordCount;
                dto.barrels.add(barrelDTO);
            });
        }
        
        return dto;
    }

    public static class BarrelDTO {
        // ID/nome do barrel (ex: "localhost:8183")
        public String id;
        
        // Número de palavras-chave indexadas neste barrel
        public int totalKeywords;
    }
}

package web.service;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service para rastrear histórico de pesquisas
 * Mantém top 10 pesquisas mais frequentes
 */
@Component
public class SearchHistoryService {

    private final ConcurrentHashMap<String, SearchQuery> searchHistory;

    public SearchHistoryService() {
        this.searchHistory = new ConcurrentHashMap<>();
    }

    /**
     * Registar uma pesquisa realizada
     */
    public void recordSearch(String query) {
        String normalizedQuery = query.toLowerCase().trim();

        searchHistory.compute(normalizedQuery, (key, existing) -> {
            if (existing == null) {
                return new SearchQuery(key, 1, LocalDateTime.now());
            } else {
                existing.incrementCount();
                existing.updateLastSearchTime(LocalDateTime.now());
                return existing;
            }
        });
    }

    /**
     * Obter top 10 pesquisas mais frequentes
     */
    public List<SearchQuery> getTop10Searches() {
        return searchHistory.values().stream()
                .sorted((a, b) -> Integer.compare(b.getCount(), a.getCount()))
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Obter top 5 pesquisas (para WebSocket stats)
     */
    public List<String> getTop5SearchQueries() {
        return getTop10Searches().stream()
                .limit(5)
                .map(SearchQuery::getQuery)
                .collect(Collectors.toList());
    }

    /**
     * Limpar histórico de pesquisas (útil para testes)
     */
    public void clearHistory() {
        searchHistory.clear();
    }

    /**
     * DTO para uma pesquisa registada
     */
    public static class SearchQuery {
        private final String query;
        private int count;
        private LocalDateTime lastSearchTime;

        public SearchQuery(String query, int count, LocalDateTime lastSearchTime) {
            this.query = query;
            this.count = count;
            this.lastSearchTime = lastSearchTime;
        }

        public String getQuery() {
            return query;
        }

        public int getCount() {
            return count;
        }

        public LocalDateTime getLastSearchTime() {
            return lastSearchTime;
        }

        public void incrementCount() {
            this.count++;
        }

        public void updateLastSearchTime(LocalDateTime time) {
            this.lastSearchTime = time;
        }
    }
}

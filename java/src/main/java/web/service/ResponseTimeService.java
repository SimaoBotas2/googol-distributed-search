package web.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serviço para guardar e calcular estatísticas de tempos de resposta
 */
@Service
public class ResponseTimeService {

    private final List<Double> responseTimes = new ArrayList<>();
    private final ConcurrentHashMap<String, List<Double>> responseTimesByQuery = new ConcurrentHashMap<>();

    /**
     * Registar tempo de resposta em décimas de segundo
     */
    public void recordResponseTime(String query, double decimas) {
        responseTimes.add(decimas);
        responseTimesByQuery.computeIfAbsent(query, k -> new ArrayList<>()).add(decimas);
    }

    /**
     * Obter média de tempos de resposta (em décimas de segundo)
     */
    public double getAverageResponseTime() {
        if (responseTimes.isEmpty()) {
            return 0.0;
        }
        double sum = responseTimes.stream().mapToDouble(Double::doubleValue).sum();
        return sum / responseTimes.size();
    }

    /**
     * Obter número total de pesquisas
     */
    public int getTotalSearches() {
        return responseTimes.size();
    }

    /**
     * Obter tempo mínimo de resposta
     */
    public double getMinResponseTime() {
        if (responseTimes.isEmpty()) {
            return 0.0;
        }
        return responseTimes.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
    }

    /**
     * Obter tempo máximo de resposta
     */
    public double getMaxResponseTime() {
        if (responseTimes.isEmpty()) {
            return 0.0;
        }
        return responseTimes.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
    }

    /**
     * Obter tempo médio para uma query específica
     */
    public double getAverageResponseTimeForQuery(String query) {
        List<Double> times = responseTimesByQuery.get(query);
        if (times == null || times.isEmpty()) {
            return 0.0;
        }
        double sum = times.stream().mapToDouble(Double::doubleValue).sum();
        return sum / times.size();
    }

    /**
     * Limpar histórico
     */
    public void clearHistory() {
        responseTimes.clear();
        responseTimesByQuery.clear();
    }
}

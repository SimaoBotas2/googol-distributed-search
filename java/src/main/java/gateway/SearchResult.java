package gateway;

import java.io.Serializable;
import java.util.List;

/**
 * Classe para encapsular resultados paginados da pesquisa.
 * Permite à Gateway devolver tanto os resultados quanto o total de matches.
 */
public class SearchResult implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private List<String> results;
    private int totalMatches;
    
    public SearchResult(List<String> results, int totalMatches) {
        this.results = results;
        this.totalMatches = totalMatches;
    }
    
    public List<String> getResults() {
        return results;
    }
    
    public void setResults(List<String> results) {
        this.results = results;
    }
    
    public int getTotalMatches() {
        return totalMatches;
    }
    
    public void setTotalMatches(int totalMatches) {
        this.totalMatches = totalMatches;
    }
}

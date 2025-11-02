package gateway;

import java.io.Serializable;
import java.util.Map;

public class SystemStats implements Serializable {
    public int activeBarrels;
    public long totalPalavras;
    public long totalUrls;
    public Map<String, Integer> tamanhoPorBarrel; 

   @Override
public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("\n")
      .append("====================================\n")
      .append("===      ESTATISTICAS DO SISTEMA      ===\n")
      .append("====================================\n");

    sb.append("Barrels ativos: ").append(activeBarrels).append("\n");

    if (tamanhoPorBarrel != null && !tamanhoPorBarrel.isEmpty()) {
        sb.append("\n[Detalhes por Barrel]\n");
        for (Map.Entry<String, Integer> entry : tamanhoPorBarrel.entrySet()) {
            sb.append(entry.getKey())
              .append(" -- ").append(entry.getValue())
              .append(" palavras indexadas\n");
        }
    }

    sb.append("\n[Totais globais]\n")
      .append(" - Total de palavras processadas: ").append(totalPalavras).append("\n")
      .append(" - URLs processados: ").append(totalUrls).append("\n");

    sb.append("====================================\n");

    return sb.toString();
}

}

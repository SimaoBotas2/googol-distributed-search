package web.dto;

import java.io.Serializable;

/**
 * DTO para transmissão de estatísticas do sistema via WebSocket.
 * 
 * Campos:
 * - urlsIndexados: Total de URLs indexadas no sistema
 * - barrelsAtivos: Número de barrels online
 * - downloadersAtivos: Número de downloaders em execução
 * - urlsEmFila: URLs aguardando indexação
 * - uptimeSegundos: Uptime do sistema em segundos
 * - timestampMs: Timestamp Unix da atualização
 */
public class StatsDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private int urlsIndexados;
    private int barrelsAtivos;
    private int downloadersAtivos;
    private int urlsEmFila;
    private long uptimeSegundos;
    private long timestampMs;

    // TODO: Construtor vazio
    // public StatsDto() {}

    // TODO: Construtor completo
    // public StatsDto(int urlsIndexados, int barrelsAtivos, ...) { ... }

    // TODO: Getters e setters
    // public int getUrlsIndexados() { ... }
    // public void setUrlsIndexados(int urlsIndexados) { ... }
    // ... resto dos getters/setters
}

package web.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Configuração de WebSocket para comunicação em tempo real com o cliente.
 * Define os endpoints e handlers para broadcast de estatísticas do sistema.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // TODO: Registar handlers WebSocket
        // registry.addHandler(statsHandler(), "/ws/stats").setAllowedOrigins("*");
    }

    // TODO: Bean do handler
    // public StatsWebSocketHandler statsHandler() {
    //     return new StatsWebSocketHandler();
    // }
}

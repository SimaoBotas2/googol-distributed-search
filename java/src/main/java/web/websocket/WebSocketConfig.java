package web.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Configuração de WebSocket para comunicação em tempo real com o cliente.s
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private StatsWebSocketHandler statsHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Registar o handler de estatísticas no endpoint /ws/stats
        // Permite conexões de qualquer origem para não bloquear desenvolvimento
        registry.addHandler(statsHandler, "/ws/stats").setAllowedOrigins("*");
    }

    @Bean
    public StatsWebSocketHandler statsHandler() {
        // Criar bean gerido pelo Spring para permitir injeção de dependências
        return new StatsWebSocketHandler();
    }
}

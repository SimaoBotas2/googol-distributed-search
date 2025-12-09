package web.websocket;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.CloseStatus;
import gateway.SystemStats;
import web.service.RMIClientService;
import web.dto.StatsDTO;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StatsWebSocketHandler implements WebSocketHandler {

    @Autowired
    private RMIClientService rmiService;

    // ConcurrentHashMap garante que múltiplas threads podem adicionar/remover sessões
    // sem causar race conditions ou corrupção de dados
    private Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    
    // Gson é usado para serializar objetos Java (SystemStats) para JSON
    private Gson gson = new Gson();

    /**
     * Chamado quando um cliente estabelece uma conexão WebSocket.
     * 
     * @param session A sessão WebSocket do cliente conectado
     * @throws Exception Se houver erro ao enviar a primeira mensagem
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("Nova conexao WebSocket estabelecida: " + session.getId());
        
        // Enviar stats iniciais imediatamente quando o cliente se conecta
        // Isso permite que o cliente receba dados antes do próximo polling agendado
        broadcastStats();
    }

    /**
     * Chamado quando o servidor recebe uma mensagem do cliente.
     * 
     * Nesta implementação, não processamos mensagens do cliente
     * pois o servidor apenas envia estatísticas (comunicação unidirecional).
     * 
     * @param session A sessão que enviou a mensagem
     * @param message A mensagem recebida
     * @throws Exception Se houver erro ao processar
     */
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // Implementação vazia: o servidor envia dados, clientes apenas ouvem
        // Se no futuro houver necessidade de receber dados do cliente,
        // adicione lógica aqui para processar as mensagens
    }

    /**
     * Chamado quando ocorre um erro na transmissão WebSocket.
     * 
     * @param session A sessão com erro
     * @param exception A exceção que ocorreu
     * @throws Exception Se houver erro ao processar a falha
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("Erro de transporte WebSocket na sessao " + session.getId() + 
                          ": " + exception.getMessage());
        exception.printStackTrace();
    }

    /**
     * Chamado quando a conexão WebSocket é fechada.
     * Remove a sessão da lista de sessões ativas.
     * 
     * @param session A sessão fechada
     * @param closeStatus Informação sobre o motivo do fechamento
     * @throws Exception Se houver erro ao processar o fechamento
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        sessions.remove(session);
        System.out.println("Conexao WebSocket fechada: " + session.getId() + 
                          " (motivo: " + closeStatus.getReason() + ")");
    }

    /**
     * Indica se o handler suporta mensagens parciais.
     * Retornamos false porque nossas mensagens são pequenas (JSON de stats)
     * e não precisam ser fragmentadas.
     * 
     * @return false - não processamos mensagens parciais
     */
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * Faz broadcast das estatísticas atuais para todas as sessões conectadas.
     * 
     * FLUXO:
     * 1. Contacta o Gateway via RMI para obter SystemStats
     * 2. Serializa SystemStats para JSON usando Gson
     * 3. Cria uma TextMessage com o JSON
     * 4. Itera todas as sessões ativas
     * 5. Envia a mensagem para cada sessão aberta
     * 6. Trata exceções graciosamente
     * 
     * TRATAMENTO DE ERROS:
     * - IOException: Pode ocorrer se a conexão foi fechada entre verificações
     * - Exceção do RMI: Contactar Gateway pode falhar se ele não está disponível
     */
    private void broadcastStats() {
        try {
            // Obter as estatísticas atuais do sistema via RMI (contacta o Gateway)
            SystemStats stats = rmiService.getSystemStats();
            
            // Converter SystemStats para StatsDTO (formato esperado pelo cliente JavaScript)
            // SystemStats usa nomes em português, StatsDTO usa nomes em inglês
            StatsDTO statsDTO = StatsDTO.fromSystemStats(stats);
            
            // Converter o objeto Java para JSON string
            // Formato: { "totalPages": X, "totalKeywords": Y, "barrels": [...] }
            String jsonStats = gson.toJson(statsDTO);
            System.out.println("[StatsWebSocketHandler] JSON enviado: " + jsonStats);
            
            // Criar uma mensagem de texto WebSocket com o JSON
            TextMessage message = new TextMessage(jsonStats);
            
            // Enviar a mensagem para todas as sessões conectadas
            for (WebSocketSession session : sessions) {
                // Verificar se a sessão ainda está aberta
                if (session.isOpen()) {
                    try {
                        session.sendMessage(message);
                    } catch (IOException e) {
                        // Se falhar ao enviar para uma sessão específica,
                        // log do erro mas continua para as outras sessões
                        System.err.println("Erro ao enviar stats para sessao " + 
                                         session.getId() + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            // Erro ao obter stats do Gateway ou ao serializar para JSON
            System.err.println("Erro ao fazer broadcast de stats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tarefa agendada que executa a cada 5 segundos (5000 milissegundos).
     * 
     * FUNCIONAMENTO:
     * - O Spring usa a anotação @Scheduled para chamar este método automaticamente
     * - Ele é executado em uma thread de pool do Spring (não bloqueia outras operações)
     * - Só faz broadcast se houver clientes conectados (otimização)
     * 
     * VANTAGEM DESTA ABORDAGEM:
     * - Simples e automático: não requer código adicional de agendamento
     * - Frequência configurável: pode ajustar o valor de fixedRate
     * - Não causa picos de CPU se não há clientes conectados
     */
    @Scheduled(fixedRate = 5000)
    public void pollStats() {
        // Só fazer broadcast se houver clientes conectados
        // Evita contactar o Gateway desnecessariamente
        if (!sessions.isEmpty()) {
            broadcastStats();
        }
    }
}

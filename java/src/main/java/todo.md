// META 2: INTERFACE WEB
// Módulo responsável pela aplicação Web (Spring Boot MVC)
//
// STATUS: ✅ FUNCIONAL (com features avançadas em desenvolvimento)
//
// ARQUITETURA MVC:
// - Controllers: Lidam com requisições HTTP (SearchController)
// - Services: Lógica de negócio - RMI bridge com Meta 1 (RMIClientService)
// - Models: Representação de dados (SystemStats, etc)
// - Views: Templates Thymeleaf (HTML + CSS)
//
// ═══════════════════════════════════════════════════════════════════════════════
// PROGRESSO - FUNCIONALIDADES CORE IMPLEMENTADAS:
// ═══════════════════════════════════════════════════════════════════════════════
//
// [✅] RMIClientService - Bridge com Meta 1 (100% CONCLUÍDO)
//      - search(query) - Pesquisa completa no índice
//      - indexURL(url) - Indexar URL nova
//      - getPagesOrderedByInLinks(limit, offset) - Top páginas por relevância
//      - getPagesLinkingTo(url) - Análise de backlinks
//      - getSystemStats() - Estatísticas globais do sistema
//      - isConnected() - Verificar status da conexão RMI
//      ✅ Com logging detalhado e error handling
//      ✅ Fallback automático para localhost
//      ✅ Timeout prevention na comunicação
//
// [✅] SearchController - Endpoints HTTP (100% CONCLUÍDO)
//      ✅ GET  /                    - Homepage com form pesquisa
//      ✅ POST /search              - Pesquisa + resultados
//      ✅ GET  /index               - Form indexar URL
//      ✅ POST /index               - Submeter URL para indexação
//      ✅ GET  /stats               - Dashboard estatísticas
//      ✅ GET  /error               - Página erro genérica
//      ✅ Error handling robusto em todos endpoints
//
// [✅] Templates Thymeleaf (100% CONCLUÍDO)
//      ✅ index.html                - Homepage com search bar
//      ✅ results.html              - Resultados paginados + query display
//      ✅ index-url.html            - Form indexar URL com feedback
//      ✅ stats.html                - Dashboard estatísticas tempo real
//      ✅ error.html                - Página erro genérica
//      ✅ Estilos CSS integrados e responsivos
//      ✅ Feedback visual (success/error messages)
//
// ═══════════════════════════════════════════════════════════════════════════════
// BUGFIXES & MELHORIAS IMPLEMENTADAS:
// ═══════════════════════════════════════════════════════════════════════════════
//
// 🐛 Config Loading (RESOLVIDO)
//    Problema: config.properties não era encontrado no classpath
//    Solução:
//      - Movido ficheiro para src/main/resources/ (estrutura Maven correta)
//      - Atualizado Config.java para usar Thread.currentThread().getContextClassLoader()
//      - Adicionado Maven resources configuration no pom.xml
//
// 🐛 RMI Connection (RESOLVIDO)
//    Problema: RMIClientService falhava ao conectar à Gateway
//    Solução:
//      - Adicionar fallback automático para "localhost" quando gateway.host é null
//      - Logging detalhado da conexão RMI
//      - Tratamento de exceções robusto
//
// 🐛 Spring Parameter Resolution (RESOLVIDO)
//    Problema: @RequestParam não funcionava sem flag de compilador
//    Solução:
//      - Adicionado maven-compiler-plugin com <parameters>true</parameters>
//      - Agora Spring consegue resolver nomes dos parâmetros
//
// 🐛 Gateway URL Forwarding (RESOLVIDO)
//    Problema: gateway.addUrl() ficava bloqueado indefinidamente
//    Solução:
//      - Adicionado timeout de 10 segundos em Gateway.addUrl()
//      - Logging detalhado de URL forwarding para Barrels
//      - Timeout previne travamentos da aplicação
//
// 🐛 Downloader Classpath (RESOLVIDO)
//    Problema: Downloader crashava com java.lang.NoClassDefFoundError: org/jsoup/Jsoup
//    Solução:
//      - Adicionado maven-dependency-plugin ao pom.xml
//      - Dependencies copiadas para target/lib na build
//      - Atualizado run_final.cmd com loop for para incluir TODOS os JARs
//      - Agora Downloader tem jsoup, gson e httpclient5 na classpath
//
// ═══════════════════════════════════════════════════════════════════════════════
// COMPONENTES WEBSOCKET IMPLEMENTADOS:
// ═══════════════════════════════════════════════════════════════════════════════
//
// [✅] WebSocketConfig.java
//      - Configuração Spring que ativa suporte WebSocket (@EnableWebSocket)
//      - Registra StatsWebSocketHandler no endpoint /ws/stats
//      - CORS desativado (setAllowedOrigins("*")) para desenvolvimento local
//
// [✅] StatsWebSocketHandler.java
//      - Implementa WebSocketHandler da Spring (gerencia conexões)
//      - afterConnectionEstablished(): Registra nova sessão, envia stats iniciais
//      - handleMessage(): Não implementado (comunicação server-push unidirecional)
//      - afterConnectionClosed(): Remove sessão da lista ativa
//      - pollStats(): @Scheduled(fixedRate = 5000) - executa a cada 5 segundos
//      - broadcastStats(): Contacta Gateway via RMI, envia JSON para todos os clientes
//      - ConcurrentHashMap.newKeySet() para thread-safety em sessões simultâneas
//
// [✅] StatsDTO.java
//      - Data Transfer Object que mapeia SystemStats para formato JSON
//      - Factory method: StatsDTO.fromSystemStats(systemStats)
//      - Campo: totalPages (maps from systemStats.totalUrls)
//      - Campo: totalKeywords (maps from systemStats.totalPalavras)
//      - BarrelDTO: lista de barrels com IDs e contagem de palavras-chave
//      - Null-safety: retorna DTO vazio se systemStats for null
//
// [✅] stats.html
//      - Cliente WebSocket JavaScript
//      - Conecta a ws://localhost:8080/ws/stats (auto-protocolo para HTTPS)
//      - Receptores: onopen, onmessage, onerror, onclose
//      - updateStatsUI(): renderiza cards com dados recebidos
//      - Auto-reconnect: setTimeout(connectWebSocket, 3000) se desconectar
//      - UI: Card "Resumo Geral" + Grid de barrels individuais
//      - Indicador de status: verde (conectado) / vermelho (desconectado)
//      - Botão "Voltar ao Home" com navegação para /
//
// [✅] GooglelApplication.java
//      - @EnableScheduling: Ativa suporte para @Scheduled em StatsWebSocketHandler
//      - Crítico: sem isto, pollStats() nunca é chamado
//
// ═══════════════════════════════════════════════════════════════════════════════
// FLUXO DE DADOS WEBSOCKET:
// ═══════════════════════════════════════════════════════════════════════════════
//
// 1. Cliente conecta: WebSocket.onopen()
// 2. Server: afterConnectionEstablished() → broadcastStats() [imediato]
// 3. Poll automático: pollStats() executa a cada 5 segundos
// 4. Server: Gateway.getSystemStats() via RMI (contacta Meta 1)
// 5. Server: Serializa SystemStats → StatsDTO → JSON com Gson
// 6. Server: broadcast(JSON) para todas as sessões ativas
// 7. Cliente: WebSocket.onmessage(JSON)
// 8. Cliente: JSON.parse() → updateStatsUI()
// 9. UI atualizada: nova contagem de URLs, palavras-chave, barrels
// 10. Repete passo 3-9 a cada 5 segundos
//
// ═══════════════════════════════════════════════════════════════════════════════

// ═══════════════════════════════════════════════════════════════════════════════
// INTEGRAÇÃO COMPLETA META 1 ↔ META 2:
// 1. Spring Boot Web UI (localhost:8080) ← → Gateway RMI (localhost:8186)
// 2. Gateway distribui URLs via RMI para Barrels (localhost:8183, 8184)
// 3. Downloader processa URLs com jsoup e indexa conteúdo
// 4. Barrels retornam resultados de pesquisa para Spring Web
// 5. Web UI exibe resultados + estatísticas do sistema
//
// ✅ Todas comunicações RMI testadas e validadas
// ✅ Sistema distribuído totalmente operacional
//
// ═══════════════════════════════════════════════════════════════════════════════
// FUNCIONALIDADES AVANÇADAS (EM DESENVOLVIMENTO):
// ═══════════════════════════════════════════════════════════════════════════════
//
// [✅] WebSockets para Stats Tempo Real (100% CONCLUÍDO)
//      - [✅] Atualizar dashboard sem refresh manual (stats.html)
//      - [✅] Live feed de estatísticas a cada 5 segundos
//      - [✅] Notificações de conexão em tempo real (indicador verde/vermelho)
//      ✅ Implementado: WebSocketConfig.java, StatsWebSocketHandler.java
//      ✅ Auto-reconnect: cliente tenta reconectar a cada 3 segundos se desconectado
//      ✅ StatsDTO.java: mapeia dados do backend para formato esperado pelo frontend
//      ✅ GooglelApplication.java: @EnableScheduling para polling automático
//
// [🚧] Paginação Avançada de Resultados
//      - [ ] Ordenação por relevância/data/popularidade
//      - [ ] Filtros por domínio/tipo de conteúdo
//      - NOTA: Paginação básica (10 em 10) já funciona
//
// [🚧] Análise de Backlinks (Parcial)
//      - [ ] Visualização gráfica de rede de links
//      - [✅] API existe: Gateway.getPagesOrderedByInLinks() e getPagesLinkingTo()
//      - [ ] Interface web para explorar backlinks
//
// [❌] Integração com APIs Externas
//      - [ ] OpenAI para sumarizações
//      - [ ] Hacker News para top stories
//      - [ ] Weather API integração
//
═══════════════════════════════════════════════════════════════════════════════
// STATUS FINAL DO PROJETO META 2:
// ═══════════════════════════════════════════════════════════════════════════════
//
// 🎯 FUNCIONALIDADES CORE ENTREGUES:
//
// [✅] Pesquisa Web
//     - Homepage com form de pesquisa (index.html)
//     - Resultados paginados (results.html)
//     - Navegação entre páginas (Anterior/Próxima)
//     - Integração RMI com Gateway
//     - Total de matches exibido
//
// [✅] Indexação de URLs
//     - Página form (index-url.html)
//     - Feedback visual (mensagens de sucesso/erro)
//     - Timeout automático (10 segundos)
//     - Navbar com links de navegação
//
// [✅] Dashboard Estatísticas Tempo Real
//     - WebSocket server-push (5 segundos)
//     - Resumo Geral: URLs únicos, Total palavras-chave, Barrels ativos
//     - Grid de Barrels: ID e contagem de palavras-chave únicas
//     - Indicador de conexão (verde/vermelho)
//     - Auto-reconnect (3 segundos se desconectar)
//     - Botão "Voltar ao Home"
//
// [✅] Infraestrutura e Integração
//     - RMIClientService: wrapper para todas as chamadas RMI
//     - WebSocketConfig: configuração Spring WebSocket
//     - StatsWebSocketHandler: handler com polling automático
//     - StatsDTO: mapeamento de dados (Portuguese → English field names)
//     - Error handling robusto em todos endpoints
//     - Logging detalhado para debugging
//     - Fallback automático para localhost se config.properties falhar
//
// [✅] Frontend Responsivo
//     - Design clean com gradientes (azul/roxo)
//     - Hover effects e transições suaves
//     - Mobile-friendly (viewport meta tag)
//     - CSS Grid para layouts responsivos
//     - Ícones e feedback visual
//
// ═══════════════════════════════════════════════════════════════════════════════
// FUNCIONALIDADES NÃO IMPLEMENTADAS (Conforme META2_TASKS.md):
//
// ❌ Paginação com ordenação customizável
// ❌ Filtros avançados (domínio, tipo de conteúdo)
// ❌ Visualização gráfica de backlinks
// ❌ Integração OpenAI (sumarizações)
// ❌ Integração Hacker News (top stories)
//

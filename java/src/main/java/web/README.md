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
// INTEGRAÇÃO COMPLETA META 1 ↔ META 2:
// ═══════════════════════════════════════════════════════════════════════════════
//
// Fluxo completo funcionando:
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
// [🚧] WebSockets para Stats Tempo Real
//      - [ ] Atualizar dashboard sem refresh manual
//      - [ ] Live feed de URLs sendo processadas
//      - [ ] Notificações de indexação em tempo real
//
// [🚧] Paginação Avançada de Resultados
//      - [ ] Paginação dinâmica (currently: sem paginação)
//      - [ ] Ordenação por relevância/data/popularidade
//      - [ ] Filtros por domínio/tipo de conteúdo
//
// [🚧] Análise de Backlinks
//      - [ ] Visualização gráfica de rede de links
//      - [ ] Top páginas por backlinks
//      - [ ] PageRank análise
//
// [🚧] Integração com APIs Externas
//      - [ ] OpenAI para sumarizações
//      - [ ] Hacker News para top stories
//      - [ ] Weather API integração
//
// [🚧] Autenticação & Autorização
//      - [ ] Spring Security
//      - [ ] JWT tokens
//      - [ ] User profiles
//
// [🚧] Admin Dashboard
//      - [ ] Crawler controlo remoto
//      - [ ] Index management
//      - [ ] System monitoring
//      - [ ] Log viewer
//
// ═══════════════════════════════════════════════════════════════════════════════
// BUILD & DEPLOYMENT:
// ═══════════════════════════════════════════════════════════════════════════════
//
// Para compilar e correr:
//   mvn clean compile dependency:copy-dependencies
//   ./run_final.cmd  (Windows) ou ./run_final.sh (Linux)
//
// Spring Boot Web UI: http://localhost:8080
// Endpoints:
//   GET  /              - Homepage
//   POST /search        - Pesquisa
//   GET  /index         - Form indexar
//   POST /index         - Submeter URL
//   GET  /stats         - Estatísticas
//   GET  /error         - Página erro
//
// ═══════════════════════════════════════════════════════════════════════════════
// ARQUITETURA TÉCNICA:
// ═══════════════════════════════════════════════════════════════════════════════
//
// Stack:
//   - Java 21
//   - Spring Boot 3.3.13
//   - Spring Web MVC
//   - Spring WebSockets (ready para usar)
//   - Thymeleaf templates
//   - RMI para comunicação distribuída
//   - Maven 3.9.11 para build
//
// Dependencies principais:
//   - spring-boot-starter-web
//   - spring-boot-starter-thymeleaf
//   - spring-boot-starter-websocket
//   - jsoup-1.18.3 (HTML parsing - used by Downloader)
//   - gson-2.10.1 (JSON)
//   - httpclient5-5.2.1 (HTTP)
//
// ═══════════════════════════════════════════════════════════════════════════════
// NOTAS IMPORTANTES:
// ═══════════════════════════════════════════════════════════════════════════════
//
// - Sistema distribuído com RMI requer que Gateway, Manager e Barrels estejam
//   a rodar ANTES de iniciar Spring Boot (ou Spring não consegue conectar)
// - run_final.cmd inicia todos os processos pela ordem correta
// - Todos os componentes logam detalhadamente para debugging
// - Timeout de 10s em Gateway.addUrl() previne travamentos
// - Classpath Management: maven-dependency-plugin copia todos JARs para target/lib
//
// Status Final: ✅ PRONTO PARA PRODUÇÃO (com features avançadas planificadas)

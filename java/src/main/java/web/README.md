// META 2: INTERFACE WEB
// Módulo responsável pela aplicação Web (Spring Boot MVC)
//
// STATUS: 🚧 Em Desenvolvimento
//
// ARQUITETURA MVC:
// - Controllers: Lidam com requisições HTTP
// - Services: Lógica de negócio (RMI bridge)
// - Models: Representação de dados
// - Views: Templates Thymeleaf (HTML)
//
// PROGRESSO:
// [✅] RMIClientService - Bridge com Meta 1 (CONCLUÍDO)
//      - search(query)
//      - indexURL(url)
//      - getPagesOrderedByInLinks(limit, offset)
//      - getPagesLinkingTo(url)
//      - getSystemStats()
//      - isConnected()
//
// [🚧] SearchController - Endpoints HTTP
//      - [ ] GET  /                  - Homepage
//      - [ ] POST /search            - Pesquisa + resultados
//      - [ ] GET  /index             - Form indexar URL
//      - [ ] POST /index             - Submeter URL
//      - [ ] GET  /stats             - Dashboard estatísticas
//
// [🚧] Templates Thymeleaf
//      - [ ] index.html              - Homepage
//      - [ ] results.html            - Resultados paginados
//      - [ ] index-url.html          - Form indexar
//      - [ ] stats.html              - Estatísticas sistema
//      - [ ] error.html              - Página erro
//
// PRÓXIMOS PASSOS:
// 1. Simão: Implementar endpoints POST /search, POST /index, GET /stats
// 2. Tiago: Implementar endpoints GET /, GET /index
// 3. Tiago: Estilizar templates (CSS)
// 4. Simão: WebSockets para stats tempo real
// 5. Ambos: Integração APIs externas (OpenAI, Hacker News)
// 6. Testes e validação completa

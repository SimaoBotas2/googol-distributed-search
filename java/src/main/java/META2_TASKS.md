# META 2: Funcionalidades Web - Motor de Pesquisa Web

## Status: Não Iniciado

## Divisão de Tarefas

### 👤 Tiago (Tiago) - Responsável por:
1. Interface Web principal - Página de pesquisa com Thymeleaf/FastAPI
2. Templates de resultados - Mostrar título, URL, citação para cada resultado
3. Paginação (10 em 10) - Navegação entre páginas de resultados
4. Integração Hacker News - Buscar "top stories" com termos da pesquisa
5. README e documentação - Instruções de instalação e execução
6. Testes de interface - Validar fluxo de pesquisa web

### 👤 Simão (Simão) - Responsável por:
1. WebSockets tempo real - Atualizações em tempo real das estatísticas
2. Integração OpenAI - Análise contexto nos resultados
3. Ligação RPC/RMI - Conectar frontend web ao serviço da meta 1
4. Controller MVC - Lógica backend dos controllers
5. Indexação recursiva UI - Interface para crawler automático
6. Estatísticas em tempo real - Página com info de barrels, pesquisas, tempos

### 🎯 Tarefas Finais (Todos):
1. Arquitetura MVC Spring/FastAPI - Definir estrutura completa
2. Integração final frontend e backend - Juntar todas as componentes
3. Testes completos do sistema web - Validar toda a funcionalidade
4. Relatório final meta 2
5. Deploy e configuração

---

## Contexto
Criar um frontend Web para a aplicação Googol desenvolvida na Meta 1. Os utilizadores devem ter acesso às mesmas funcionalidades através da Web, sem necessidade de cliente específico.

---

## 1. Interface Web (web/)

### 1.1 Arquitetura MVC com Spring Boot
- [ ] **TODO (Final)**: Definir estrutura completa MVC Spring/FastAPI
- [ ] **TODO (Simão)**: Criar estrutura base do Spring Boot
- [ ] **TODO (Tiago)**: Configurar Thymeleaf para templates HTML
- [ ] **TODO (Simão)**: Criar controllers para as funcionalidades principais
- [ ] **TODO (Tiago)**: Criar modelos (Models) para representar dados
- [ ] **TODO (Tiago)**: Implementar layout base com CSS/Bootstrap

### 1.2 Funcionalidades Web
- [ ] **TODO (Tiago)**: Página de homepage com form de pesquisa
- [ ] **TODO (Tiago)**: Templates de resultados - Mostrar título, URL, citação
- [ ] **TODO (Tiago)**: Página para adicionar novo URL para indexação
- [ ] **TODO (Simão)**: Dashboard com estatísticas do sistema

### 1.3 Formulários e Entrada de Dados
- [ ] **TODO (Tiago)**: Form para pesquisa (input de múltiplas palavras)
- [ ] **TODO (Tiago)**: Form para adicionar URL novo
- [ ] **TODO (Tiago)**: Validação de inputs no servidor
- [ ] **TODO (Tiago)**: Mensagens de erro/sucesso para utilizador

### 1.4 Integração com Backend (RPC/RMI)
- [ ] **TODO (Simão)**: Criar cliente RMI no servidor Web
- [ ] **TODO (Simão)**: Ligação RPC/RMI - conectar frontend web ao serviço da meta 1
- [ ] **TODO (Simão)**: Tratar respostas do backend
- [ ] **TODO (Simão)**: Implementar tratamento de erros RMI

---

## 2. Comunicação em Tempo Real com WebSockets (websocket/)

### 2.1 Setup WebSockets
- [ ] **TODO (Simão)**: WebSockets tempo real - atualizações em tempo real das estatísticas
- [ ] **TODO (Simão)**: Criar WebSocket handler
- [ ] **TODO (Simão)**: Implementar endpoints WebSocket

### 2.2 Atualizações em Tempo Real
- [ ] **TODO (Simão)**: Mostrar downloaders ativos em tempo real
- [ ] **TODO (Simão)**: Mostrar barrels ativos em tempo real
- [ ] **TODO (Simão)**: Mostrar progresso de indexação em tempo real
- [ ] **TODO (Simão)**: Mostrar estatísticas do sistema atualizadas

### 2.3 Notificações para Cliente
- [ ] **TODO (Simão)**: Enviar notificações quando novo URL é indexado
- [ ] **TODO (Simão)**: Enviar notificações quando barrel cai/se reconecta
- [ ] **TODO (Simão)**: Implementar mecanismo de heartbeat
- [ ] **TODO (Simão)**: Implementar reconnection automática

---

## 3. Integração com APIs REST Externas (external_api/)

### 3.1 Integração com Hacker News API
- [ ] **TODO (Tiago)**: Integração Hacker News - Buscar "top stories" com termos da pesquisa
- [ ] **TODO (Tiago)**: Criar cliente para API Hacker News
- [ ] **TODO (Tiago)**: Buscar "top stories" do Hacker News
- [ ] **TODO (Tiago)**: Filtrar histórias por termos de pesquisa
- [ ] **TODO (Tiago)**: Retornar URLs filtrados para o utilizador
- [ ] **TODO (Tiago)**: Mostrar origem dos resultados (Googol vs Hacker News)

### 3.2 Integração com OpenAI API
- [ ] **TODO (Simão)**: Integração OpenAI - análise contexto nos resultados
- [ ] **TODO (Simão)**: Criar cliente para API OpenAI
- [ ] **TODO (Simão)**: Usar para gerar snippets melhorados
- [ ] **TODO (Simão)**: Usar para sugerir termos de pesquisa relacionados
- [ ] **TODO (Simão)**: Implementar tratamento de erros de API

### 3.3 Tratamento de Respostas Externas
- [ ] **TODO (Tiago/Simão)**: Implementar cache de resultados externos
- [ ] **TODO (Tiago/Simão)**: Implementar timeout para chamadas externas
- [ ] **TODO (Tiago/Simão)**: Tratar falhas de APIs externas com fallback
- [ ] **TODO (Tiago/Simão)**: Combinar resultados do Googol com APIs externas

---

## 4. Frontend JavaScript/HTML/CSS (web/)

### 4.1 Página Principal (Homepage)
- [ ] **TODO (Tiago)**: Design clean e minimalista (inspirado em Google)
- [ ] **TODO (Tiago)**: Form de pesquisa centralizado
- [ ] **TODO (Tiago)**: Opção de "I'm Feeling Lucky" (primeiro resultado)
- [ ] **TODO (Tiago)**: Link para adicionar novo URL

### 4.2 Página de Resultados
- [ ] **TODO (Tiago)**: Mostrar resultados em formato lista
- [ ] **TODO (Tiago)**: Mostrar URL, título, snippet de cada resultado
- [ ] **TODO (Tiago)**: Paginação (10 em 10) - Mostrar número de inlinks (relevância)
- [ ] **TODO (Tiago)**: Implementar paginação (10 resultados por página)
- [ ] **TODO (Tiago)**: Implementar "Próxima página" e "Página anterior"

### 4.3 Componentes Interativos
- [ ] **TODO (Tiago)**: AJAX para pesquisa (sem reload de página)
- [ ] **TODO (Tiago)**: Autocomplete para sugestões de termos
- [ ] **TODO (Tiago)**: Filtros de resultado (por data, domínio, etc.)
- [ ] **TODO (Tiago)**: Ordenação de resultados

### 4.4 Dashboard (Tempo Real)
- [ ] **TODO (Simão)**: Estatísticas em tempo real - página com info de barrels, pesquisas, tempos
- [ ] **TODO (Simão)**: Mostrar número de URLs indexados
- [ ] **TODO (Simão)**: Mostrar número de barrels ativos
- [ ] **TODO (Simão)**: Mostrar número de downloaders ativos
- [ ] **TODO (Simão)**: Mostrar URLs em fila para indexação
- [ ] **TODO (Simão)**: Gráficos de estatísticas

### 4.5 Styling e UX
- [ ] **TODO (Tiago)**: Responsivo (mobile, tablet, desktop)
- [ ] **TODO (Tiago)**: Dark mode / Light mode toggle
- [ ] **TODO (Tiago)**: Temas personalizáveis
- [ ] **TODO (Tiago)**: Animações smooth
- [ ] **TODO (Tiago)**: Ícones e emojis

---

## 5. Funcionalidades Adicionais

### 5.1 Indexação Recursiva
- [ ] **TODO (Simão)**: Indexação recursiva UI - interface para crawler automático
- [ ] **TODO (Simão)**: Interface para ativar/desativar crawler
- [ ] **TODO (Simão)**: Visualizar fila de URLs pendentes

### 5.2 Documentação e Testes
- [ ] **TODO (Tiago)**: README e documentação - Instruções de instalação e execução
- [ ] **TODO (Tiago)**: Testes de interface - Validar fluxo de pesquisa web
- [ ] **TODO (Simão)**: Testes de integração com backend
- [ ] **TODO (Final)**: Testes completos do sistema web - validar toda a funcionalidade

### 5.3 Tarefas Finais
- [ ] **TODO (Final)**: Integração final frontend e backend - juntar todas as componentes
- [ ] **TODO (Final)**: Relatório final meta 2
- [ ] **TODO (Final)**: Deploy e configuração

---

## Próximos Passos
1. Setup inicial do Spring Boot
2. Criar estrutura base MVC
3. Integrar com backend RMI (Meta 1)
4. Implementar formulários básicos
5. Adicionar WebSocket para updates em tempo real
6. Integrar com APIs externas
7. Otimizar UI/UX

# Estrutura do Projeto Googol - Sistemas Distribuídos 2025/26

## Visão Geral
Projeto dividido em duas metas:
- **META 1**: Motor de pesquisa Web com arquitetura distribuída (RPC/RMI)
- **META 2**: Interface Web para a aplicação (Spring Boot + WebSockets)

---

## Estrutura de Diretórios

```
sd-googol/
├── java/
│   ├── src/main/java/
│   │   ├── barrel/                 # Core distribuído (Meta 1)
│   │   │   ├── Index.java         # Interface para barrel
│   │   │   ├── IndexBarrel.java   # Implementação de barrel
│   │   │   ├── IndexManager.java  # Gestor de barrels
│   │   │   └── Manager.java       # Interface do manager
│   │   │
│   │   ├── client/                 # Cliente CLI (Meta 1)
│   │   │   └── Client.java        # Interface linha de comando
│   │   │
│   │   ├── downloader/             # Web crawler (Meta 1)
│   │   │   └── Downloader.java    # Crawleia páginas Web
│   │   │
│   │   ├── gateway/                # Interface RMI (Meta 1)
│   │   │   ├── Gateway.java       # Implementação da gateway
│   │   │   └── GatewayInterface.java
│   │   │
│   │   ├── common/                 # Utilitários comuns
│   │   │   └── info.txt           # Configuração de portas
│   │   │
│   │   ├── meta1/                  # Tarefas Meta 1
│   │   │   ├── META1_TASKS.md     # Checklist de tarefas
│   │   │   ├── indexing/          # Módulo de indexação
│   │   │   ├── search/            # Módulo de pesquisa
│   │   │   └── distributed/       # Módulo distribuído
│   │   │
│   │   └── meta2/                  # Tarefas Meta 2
│   │       ├── META2_TASKS.md     # Checklist de tarefas
│   │       ├── web/               # Controllers, models, views
│   │       │   ├── controller/    # Spring Controllers
│   │       │   ├── service/       # Lógica de negócio
│   │       │   ├── model/         # Entidades
│   │       │   └── view/          # Templates Thymeleaf
│   │       ├── websocket/         # Comunicação tempo real
│   │       │   ├── handler/       # WebSocket handlers
│   │       │   └── service/       # Serviços WebSocket
│   │       └── external_api/      # Integração com APIs
│   │           ├── hackernews/    # Cliente HackerNews
│   │           └── openai/        # Cliente OpenAI
│   │
│   ├── pom.xml                     # Maven configuration
│   ├── build.sh / build.cmd        # Scripts de build
│   └── run-commands.sh / .cmd
│
├── config.txt                       # Configuração de barrels (portas)
├── README.md                        # Documentação
├── PROJETO_STRUCTURE.md             # Este ficheiro
├── sd_projeto_meta1_2026.pdf       # Enunciado Meta 1
└── sd_projeto_meta2_2026.pdf       # Enunciado Meta 2
```

---

## Meta 1: Motor de Pesquisa Distribuído

### Componentes Principais

#### 1. **Barrel (Indexing Node)**
- Armazena índice invertido (palavra → URLs)
- Mantém fila de URLs a processar
- Sincroniza com outros barrels
- Fornece métodos de pesquisa

#### 2. **Index Manager**
- Gerencia múltiplos barrels
- Monitora saúde dos barrels
- Sincroniza dados na reconexão
- Mantém lista de barrels ativos

#### 3. **Gateway**
- Interface RMI para clientes
- Distribui requisições entre barrels
- Agrega resultados

#### 4. **Downloader**
- Web crawler que visita URLs
- Extrai texto e links
- Distribui dados aos barrels

#### 5. **Cliente CLI**
- Interface de linha de comando
- Menu interativo para pesquisa e indexação

### Funcionalidades Meta 1
- ✅ Indexação de URLs
- ✅ Pesquisa por múltiplas palavras
- ✅ Redundância com sincronização
- ✅ Arquitetura distribuída com RPC/RMI
- ⏳ Ordenação por relevância
- ⏳ Paginação de resultados
- ⏳ Otimizações estruturais

---

## Meta 2: Interface Web

### Componentes Principais

#### 1. **Web Application (Spring Boot)**
- MVC architecture
- Thymeleaf templates
- Integração com RMI (Meta 1)

#### 2. **WebSocket Server**
- Comunicação assíncrona em tempo real
- Updates de barrels/downloaders
- Notificações de progresso

#### 3. **APIs Externas**
- Hacker News integration
- OpenAI integration
- Enriquecimento de resultados

#### 4. **Frontend (HTML/CSS/JS)**
- Responsive design
- AJAX requests
- Dashboard em tempo real

### Funcionalidades Meta 2
- ⏳ Interface Web MVC
- ⏳ Pesquisa via Web
- ⏳ Adicionar URLs via Web
- ⏳ WebSockets para updates
- ⏳ Integração Hacker News
- ⏳ Integração OpenAI
- ⏳ Paginação web
- ⏳ Dashboard em tempo real

---

## Fluxo de Trabalho

### Meta 1 - Ciclo de Vida de uma Indexação
```
Cliente (addUrl)
    ↓
Gateway.addUrl()
    ↓
Barrel.putNew() (fila)
    ↓
Downloader.takeNext()
    ↓
Jsoup.connect() (fetch página)
    ↓
Extração de palavras e links
    ↓
Broadcast para todos os barrels
    ↓
Barrel.addToIndex() e Barrel.putNew()
    ↓
Sincronização entre barrels
```

### Meta 1 - Ciclo de Vida de uma Pesquisa
```
Cliente (search "termo1 termo2")
    ↓
Gateway.search()
    ↓
Barrel.searchAll() (interseção AND)
    ↓
Ordenação por relevância (inlinks)
    ↓
Paginação
    ↓
Retorno ao cliente
```

### Meta 2 - Fluxo Web
```
Browser (HTTP GET /search?q=termo)
    ↓
Spring Controller
    ↓
RMI Client (chama Gateway)
    ↓
Retorno de resultados
    ↓
Template rendering
    ↓
WebSocket updates (tempo real)
    ↓
HTML + JS (AJAX + WebSocket)
```

---

## Configuração

### Portas
- **Gateway**: 8185
- **IndexManager**: 8182
- **Barrel 1**: 8183
- **Barrel 2**: 8184
- **Web Server (Meta 2)**: 8080
- **WebSocket (Meta 2)**: ws://localhost:8080/ws

### Ficheiros de Configuração
- `config.txt`: Portas dos barrels a iniciar
- `pom.xml`: Dependências Maven
- `application.properties` (Meta 2): Spring Boot config

---

## Próximas Tarefas

### Imediatas (Meta 1 - Ordenação)
1. Modificar estrutura de dados para guardar inlinks
2. Implementar contador de inlinks
3. Ordenar resultados por inlinks
4. Adicionar paginação

### Curto Prazo (Meta 1 - Melhorias)
1. Document IDs
2. Bloom Filter
3. Robustez do crawler
4. Processamento paralelo

### Médio Prazo (Meta 2 - Web)
1. Setup Spring Boot
2. Integração RMI
3. Formulários básicos
4. WebSocket setup
5. APIs externas

---

## Dependências Importantes

### Meta 1
- `jsoup`: HTML parsing
- Java RMI: Remote procedure calls
- Java Collections: Lists, Sets, Maps

### Meta 2
- `Spring Boot`: Web framework
- `Thymeleaf`: Template engine
- `Spring WebSocket`: Real-time communication
- `Retrofit/RestTemplate`: REST client
- `Bootstrap`: Frontend framework

---

## Notas Importantes

1. **Manter separação clara** entre Meta 1 e Meta 2
2. **TODOs bem documentados** em cada ficheiro
3. **Meta 1 deve estar estável** antes de integrar Meta 2
4. **Testes frequentes** durante desenvolvimento
5. **Documentar APIs** entre componentes

---

## Como Usar Este Documento

1. Consulte `META1_TASKS.md` para checklist de Meta 1
2. Consulte `META2_TASKS.md` para checklist de Meta 2
3. Use este ficheiro como referência da arquitetura geral
4. Mantenha atualizado conforme progresso


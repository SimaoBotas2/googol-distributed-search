# Googol – Motor de Pesquisa Distribuído

Googol é um motor de pesquisa distribuído desenvolvido no âmbito da unidade curricular de Sistemas Distribuídos. O sistema é composto por vários serviços cooperantes que executam as tarefas de download, indexação, armazenamento e pesquisa de páginas Web. O projeto inclui ainda uma interface Web construída em Spring Boot com Thymeleaf, bem como integrações externas.

---

## 1. Arquitetura Geral

A arquitetura do Googol é baseada em quatro componentes principais:

### 1. Downloader
Responsável por obter páginas Web, extrair URLs e enviar conteúdos para os Barrels.

### 2. Barrel
Armazena índices locais, gere in-links, e responde a pedidos do Gateway (pesquisa, contagem de ligações e ordenações).

### 3. Gateway
Ponto centralizador que coordena a pesquisa, agrega resultados provenientes dos Barrels e expõe métodos remotos via RMI.

### 4. Interface Web (Spring Boot)
Aplicação Web que permite aos utilizadores:
- Efetuar pesquisas
- Ver resultados paginados
- Consultar páginas que apontam para um determinado URL
- Ver estatísticas do sistema (com WebSockets)
- Consultar notícias do Hacker News
- (Opcional) Obter snippets gerados através da API OpenAI

---

## 2. Funcionalidades Implementadas

### Pesquisa
- Pesquisa distribuída através do Gateway usando RMI.
- Resultados ordenados por relevância (número de in-links).
- Paginação automática de 10 em 10 resultados.
- Geração de títulos básicos a partir do URL.
- Link para visualização das páginas que referenciam cada URL (in-links).

### Interface Web
- Página inicial de pesquisa.
- Página de resultados com paginação.
- Página de estatísticas atualizadas em tempo real via WebSocket.
- Página de visualização de in-links.
- Página de notícias do Hacker News.

### Integrações Externas
- API Hacker News para exibir as principais notícias.
- Integração OpenAI preparada para geração de snippets (ativação opcional).

---

## 3. Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3.3**
- **Thymeleaf**
- **RMI (Remote Method Invocation)**
- **Jsoup (extração HTML)**
- **HTTPClient 5**
- **WebSockets (STOMP não utilizado; WebSocketHandler direto)**
- **OpenAI API (opcional)**
- **Maven**

---

## 4. Estrutura do Projeto (Java)

java/
├── pom.xml
├── src/main/java/
│ ├── downloader/
│ ├── barrel/
│ ├── gateway/
│ ├── client/
│ ├── external_api/
│ │ ├── HackerNewsClient.java
│ │ └── OpenAiClient.java
│ └── web/
│ ├── controller/
│ ├── service/
│ ├── websocket/
│ ├── dto/
│ └── GooglelApplication.java
└── src/main/resources/
├── templates/
├── application.properties

---

## 5. Como Executar

### 1. Iniciar os serviços distribuídos
Em terminais separados:

bash
java downloader.Downloader
java barrel.IndexBarrel
java gateway.Gateway

### 2. Iniciar a interface Web

Entrar na pasta java/:

mvn clean install
java -jar target/sd-tutorial2-1.0-SNAPSHOT.jar


A aplicação estará disponível em:

http://localhost:8080

---

## 6. Créditos

Projeto SD-Googol — desenvolvido por:
- Simão Carvalho nº2021223055
- Tiago Durães   nº2023229933

Este documento foi escrito pelos autores e refinado com o uso de LLM (Chat-GPT 5)

Licenciatura em Engenharia Informática
Universidade de Coimbra - 2025
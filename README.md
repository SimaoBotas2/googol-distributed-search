# SD-Googol — Sistema Distribuído de Indexação

Projeto desenvolvido para a unidade curricular **Sistemas Distribuídos**.  
Implementa um motor de indexação de páginas web em Java RMI, com múltiplos componentes distribuídos entre **duas máquinas**.

---

## Estrutura do Projeto

java/
├── src/
│ └── main/java/
│ ├── barrel/
│ ├── client/
│ ├── common/
│ ├── downloader/
│ ├── gateway/
│ └── resources/
│ └── config.properties
├── target/
│ ├── classes/ ← ficheiros compilados (.class)
│ └── lib/jsoup-1.18.3.jar
├── run_maquina1.cmd ← script da Máquina 1
├── run_maquina2.cmd ← script da Máquina 2
└── README.md


---

## ⚙️ Pré-requisitos

- **Java 17+** (JDK)
- **Windows PowerShell ou CMD**
- Conectividade entre as máquinas na mesma rede local
- Firewalls desativadas (ou portas RMI abertas)

---

## 🌐 Endereços IP utilizados

| Máquina | IP Local | Função |
|----------|-----------|--------|
| **Máquina 1** | `192.168.1.183` | Gateway, Barrel 1, Downloader 1 |
| **Máquina 2** | `192.168.1.66` | Manager, Barrel 2, Downloader 2, Client |

> Estes valores estão definidos no ficheiro `config.properties`.  
> Atualizar o valor dos ips no ficheiro para os valores reais.

---

## 🧩 Compilação manual

Compilação manual:

```bash
cd java
javac -cp "target\lib\jsoup-1.18.3.jar" -d target\classes src\main\java\**\*.java
copy src\main\java\resources\config.properties target\classes\
```


O projeto inclui dois scripts .cmd para iniciar todos os serviços de cada máquina.

**OBRIGATÓRIO**
Rodar primeiro o script da máquina 2!!!!

**Máquina 1:**
Componentes:
    Barrel 1
    Downloader 1
    Gateway

Para rodar:

```bash
cd java
run_maquina1.cmd
```

O script:
- Compila o projeto
- Copia o config.properties
- Abre três janelas:
    - Barrel1
    - Downloader1
    - Gateway


**Máquina 2**
Componentes:
    Barrel 2
    Manager
    Downloader 2
    Client

O script:
- Compila o projeto
- Copia o config.properties
- Abre quatro janelas:
    - Barrel2
    - Manager
    - Downloader2
    - Client


**Testes e Verificação**

O Manager deve listar ambos os barrels ativos:
[IndexManager] Verificacao concluida. Barrels ativos:
  -> 192.168.1.183:8183
  -> 192.168.1.66:8184

A gateway deve imprimir:
[Gateway] Ligado ao Manager!
[Gateway] Conectado ao Barrel em 192.168.1.183:8183
[Gateway] Conectado ao Barrel em 192.168.1.66:8184

O cliente deve ligar de imediato à gateway:
[Client] A tentar ligar à Gateway (192.168.1.183:8186)...
[Client] Ligado à Gateway com sucesso!


**Execução manual**

```bash
cd target\classes

:: Máquina 1
java -cp ".;..\lib\jsoup-1.18.3.jar" barrel.IndexBarrel 1
java -cp ".;..\lib\jsoup-1.18.3.jar" downloader.Downloader 1
java -cp ".;..\lib\jsoup-1.18.3.jar" gateway.Gateway

:: Máquina 2
java -cp ".;..\lib\jsoup-1.18.3.jar" barrel.IndexBarrel 2
java -cp ".;..\lib\jsoup-1.18.3.jar" barrel.IndexManager
java -cp ".;..\lib\jsoup-1.18.3.jar" downloader.Downloader 2
java -cp ".;..\lib\jsoup-1.18.3.jar" client.Client
```

**Encerramento**

Para parar todos os serviços, fecha as janelas individuais (ou pressiona Ctrl + C em cada uma).


👨‍💻 Créditos

Projeto SD-Googol — desenvolvido por:
- Simão Carvalho nº2021223055
- Tiago Durães   nº2023229933

Este documento foi escrito pelos autores e refinado com o uso de LLM (Chat-GPT 5)

Licenciatura em Engenharia Informática
Universidade de Coimbra - 2025
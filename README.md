# 🏗️ fiapx-app-worker-processor

Esta documentação descreve a arquitetura de microsserviços orientada a eventos desenvolvida para o **Sistema de Processamento de Vídeos da FIAP X**.

## 🚀 O papel deste serviço (`worker-processor`)
O **Worker Processor** é o motor de processamento assíncrono do sistema. Suas responsabilidades incluem:
*   Atuar como *Consumer* (Consumidor), ouvindo a fila do RabbitMQ.
*   Processar vídeos em background, escalando horizontalmente conforme o volume de tarefas.
*   Executar a extração de frames e compactação em arquivos `.zip`.
*   Atualizar o status final do processamento no PostgreSQL via API.
*   Garantir resiliência: se o processamento falhar, a mensagem pode ser reprocessada.

---

## 📐 Visão Geral da Arquitetura da Solução

```mermaid
graph TD
    %% Cores e Estilos
    classDef client fill:#f9f9f9,stroke:#333,stroke-width:2px;
    classDef aws fill:#FF9900,stroke:#232F3E,stroke-width:2px,color:#232F3E,font-weight:bold;
    classDef k8s fill:#326ce5,stroke:#fff,stroke-width:2px,color:#fff,font-weight:bold;
    classDef microservice fill:#6DB33F,stroke:#fff,stroke-width:2px,color:#fff;
    classDef database fill:#336791,stroke:#fff,stroke-width:2px,color:#fff;
    classDef queue fill:#FF6600,stroke:#fff,stroke-width:2px,color:#fff;
    classDef cicd fill:#2088FF,stroke:#fff,stroke-width:2px,color:#fff;

    %% Atores e Clientes
    User((🧑‍💻 Usuário / Investidor)):::client

    %% Infraestrutura AWS
    subgraph "☁️ Cloud Provider (AWS)"
        Gateway[🚪 API Gateway]:::aws
        
        subgraph "🔒 VPC (Rede Privada)"
            
            subgraph "☸️ Amazon EKS (Kubernetes)"
                direction TB
                Auth[🔐 fiapx-app-auth-service]:::microservice
                API[📹 fiapx-app-video-api]:::microservice
                Worker[⚙️ fiapx-app-worker-processor]:::microservice
            end
            
            subgraph "🗄️ Dados e Mensageria"
                MQ[(🐇 RabbitMQ / Amazon MQ)]:::queue
                DB[(🐘 PostgreSQL / Amazon RDS)]:::database
                Cache[(🔴 Redis / ElastiCache)]:::database
            end
            
            Storage[(🪣 Amazon S3 - Arquivos)]:::aws
        end
    end

    %% Pipeline CI/CD
    subgraph "🚀 Pipeline CI/CD"
        GH[🐙 GitHub Actions]:::cicd
        Sonar[🔍 SonarCloud]:::cicd
        ECR[📦 Amazon ECR]:::aws
    end

    %% Relacionamentos e Fluxos
    User -->|1. Login / Upload REST| Gateway
    Gateway -->|Roteamento| Auth
    Gateway -->|Roteamento| API

    Auth -->|Valida/Cria Usuário| DB
    Auth -->|Gerencia Sessão| Cache

    API -.->|"Verifica Token JWT"| Auth
    API -->|"Salva Metadados/Status"| DB
    API -->|"Upload do Vídeo Bruto"| Storage
    API -->|"Publica Evento (Pendente)"| MQ

    MQ -->|"Consome Evento"| Worker
    Worker -->|"Lê Vídeo Bruto| Storage
    Worker -->|"Processa e Salva ZIP"| Storage
    Worker -->|"Atualiza Status (Concluído)"| DB

    %% Relacionamentos CI/CD
    GH -.->|"Analisa Código"| Sonar
    GH -.->|"Gera Imagem Docker"| ECR
    GH -.->|"Deploy (kubectl apply)"| Auth
    GH -.->|"Deploy (kubectl apply)"| API
    GH -.->|"Deploy (kubectl apply)"| Worker
```

### Camada de Entrada e Roteamento
*   **AWS API Gateway:** Ponto de entrada único para o ecossistema.

### Orquestração e Compute
*   **Amazon EKS (Elastic Kubernetes Service):** Cluster gerenciado que orquestra a execução dos microsserviços. O worker é o serviço que mais se beneficia do **Horizontal Pod Autoscaling (HPA)**.

### Microsserviços do Ecossistema
*   **`fiapx-app-auth-service`**: Responsável pelo cadastro e autenticação de usuários via JWT.
*   **`fiapx-app-video-api`**: Interface de upload e consulta de status.
*   **`fiapx-app-worker-processor`** (este serviço): Serviço de background que consome as mensagens e processa o vídeo.

### Persistência e Mensageria
*   **RabbitMQ (Amazon MQ):** Broker de mensagens que garante a resiliência do sistema. Este worker é o principal consumidor deste serviço.
*   **PostgreSQL (Amazon RDS):** Banco de dados relacional para persistência de dados.
*   **Redis (Amazon ElastiCache):** Cache de alto desempenho para suporte à autenticação.

---

## 🔄 Fluxo Principal de Processamento

1.  **🔒 Autenticação:** O usuário autentica no `auth-service`.
2.  **📤 Upload:** O usuário envia o vídeo para a `video-api`.
3.  **📨 Enfileiramento:** A API publica uma mensagem no RabbitMQ contendo os dados do vídeo.
4.  **⚙️ Processamento:** Este `worker-processor` consome a mensagem e gera o arquivo ZIP final.
5.  **✅ Finalização:** O worker atualiza o status no banco de dados para `CONCLUÍDO`.

---

## 🛠️ Tecnologias Utilizadas

| Categoria | Tecnologia |
| :--- | :--- |
| **Linguagem / Framework** | Java 17 / Spring Boot 3.x |
| **Cloud Provider** | AWS (Amazon Web Services) |
| **Containers** | Docker / Kubernetes (EKS) |
| **Mensageria** | RabbitMQ |
| **Bancos de Dados** | PostgreSQL / Redis |
| **Infraestrutura** | Terraform (IaC) |
| **CI/CD** | GitHub Actions |
| **Qualidade** | SonarCloud |

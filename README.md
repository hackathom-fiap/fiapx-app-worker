# 🏗️ Solução FIAP X - Sistema de Processamento de Vídeos

Esta documentação descreve a arquitetura completa da solução desenvolvida para o **Hackathon FIAP X**. O sistema foi projetado seguindo os princípios de microsserviços, orientação a eventos e escalabilidade em nuvem.

## 📐 Desenho da Arquitetura

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

    Auth -->|"Valida/Cria Usuário"| DB
    Auth -->|"Gerencia Sessão"| Cache

    API -.->|"Verifica Token JWT"| Auth
    API -->|"Salva Metadados/Status"| DB
    API -->|"Upload do Vídeo Bruto"| Storage
    API -->|"Publica Evento (Pendente)"| MQ

    MQ -->|"Consome Evento"| Worker
    Worker -->|"Lê Vídeo Bruto"| Storage
    Worker -->|"Processa e Salva ZIP"| Storage
    Worker -->|"Atualiza Status (Concluído)"| DB

    %% Relacionamentos CI/CD
    GH -.->|"Analisa Código"| Sonar
    GH -.->|"Gera Imagem Docker"| ECR
    GH -.->|"Deploy (kubectl apply)"| Auth
    GH -.->|"Deploy (kubectl apply)"| API
    GH -.->|"Deploy (kubectl apply)"| Worker
```

---

## 🚀 Componentes da Solução

### 1. Microsserviços
*   **`fiapx-app-auth-service`**: Gerencia a segurança, autenticação e autorização utilizando **JWT**.
*   **`fiapx-app-video-api`**: Interface principal para upload de vídeos e consulta de status de processamento.
*   **`fiapx-app-worker-processor`**: Worker assíncrono que realiza o processamento pesado de vídeos (extração de imagens e compactação ZIP).

### 2. Infraestrutura e Persistência
*   **Amazon EKS (Kubernetes)**: Orquestração de containers com auto-scaling.
*   **RabbitMQ**: Broker de mensageria para desacoplamento e resiliência.
*   **PostgreSQL**: Persistência de dados relacionais e controle de status.
*   **Redis**: Cache e gerenciamento de sessões de segurança.
*   **Amazon S3**: Armazenamento de arquivos binários (vídeos e ZIPs resultantes).
*   **API Gateway**: Ponto de entrada seguro com VPC Link.

---

## 🔄 Fluxo de Funcionamento

1.  **Autenticação**: O usuário obtém um token JWT no serviço de Auth.
2.  **Upload**: O vídeo é enviado para a Video API, que o armazena e registra o status inicial.
3.  **Mensageria**: Um evento é publicado no RabbitMQ para processamento assíncrono.
4.  **Processamento**: O Worker consome a mensagem, processa o vídeo e gera o arquivo final.
5.  **Finalização**: O status é atualizado e o usuário pode baixar o resultado.

---

## 🛠️ Stack Tecnológica

| Categoria | Tecnologia |
| :--- | :--- |
| **Linguagem** | Java 17 |
| **Framework** | Spring Boot 3 |
| **Cloud** | AWS |
| **Mensageria** | RabbitMQ |
| **Containers** | Docker / Kubernetes |
| **Qualidade** | SonarCloud |
| **CI/CD** | GitHub Actions |

---
*Este projeto faz parte do desafio Hackathon FIAP X - Software Architecture.*

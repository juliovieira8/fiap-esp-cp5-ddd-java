# FIAP Bank - Plataforma de Concessão de Crédito 🏦💳

**Checkpoint 5 — Domain Driven Design (Java) · Engenharia de Software · Turma 2ESPH · Prof. Eduardo dos Santos Ramos**

Monorepo com o ecossistema da **Plataforma de Concessão de Crédito** do FIAP Bank: **Core Bancário** (construído do zero), **Plataforma de Crédito** (refatorada), **Mesa de Operações** (Thymeleaf) e **Internet Banking Mobile** (Angular).

## 👥 Integrantes do grupo

| Nome completo | RM |
| :--- | :---: |
| *Guilherme Moura Gama* | *562162* |
| *Guilherme Ruiz Costa* | *563236* |
| *Júlio César Augusto Vieira* | *563366* |



## 📁 Estrutura do monorepo

| Projeto | Tecnologia | Porta | Documentação |
| :--- | :--- | :---: | :--- |
| `fiap-bank-core-bancario` | Java 21, Spring Boot 4.1, Spring Data JPA, DDD, Maven multi-módulo | `8080` | [README](./fiap-bank-core-bancario/README.md) |
| `fiap-bank-plataforma-credito` | Java 21, Spring Boot 4.1, Spring Data JPA, DDD, Maven multi-módulo | `8081` | [README](./fiap-bank-plataforma-credito/README.md) |
| `fiap-bank-mesa-operacoes-credito` | Java 21, Spring Boot 4.1, Thymeleaf, RestClient | `8082` | [README](./fiap-bank-mesa-operacoes-credito/README.md) |
| `fiap-bank-internet-banking-mobile` | Angular 17, TypeScript | `4200` | [README](./fiap-bank-internet-banking-mobile/README.md) |

## 🧭 O que foi feito (resumo da entrega)

**Fase 1 — Core Bancário (do zero).** Maven multi-módulo com os 4 submódulos `domain`, `infrastructure`, `application` e `presentation`; domínio puro (Entidades, Value Objects e interfaces de Repositório, Lombok); infraestrutura com Entidades JPA, Spring Data JPA e *Repository Adapters*; aplicação com DTOs (Java Records + Bean Validation) e serviços; apresentação com controllers RESTful (`/api/v1/clientes` e `/api/v1/contas`, verbos `GET/POST/PUT/DELETE`) e `GlobalExceptionHandler`. Executa na **porta 8080**.

**Fase 2 — Plataforma de Crédito (refatoração).** Diagnóstico das falhas do estado inicial (ver cartão-resposta) e reestruturação nas mesmas 4 camadas, com `@Entity` somente na infraestrutura, *clients* de SCR/Bureau/CRM/Core, API RESTful (`/api/v1/solicitacoes`), Bean Validation, tratamento global de exceções e **controller adaptador** que mantém as rotas legadas (`/api/credito/*`).

**Governança arquitetural.** `maven-enforcer-plugin` (hierarquia de dependências e pureza do `domain`) + teste ArchUnit: violações de fronteira **quebram o build**.

**Retrocompatibilidade.** O app Angular e a Mesa (Thymeleaf) continuam usando os contratos históricos, atendidos pelo adaptador. A Mesa teve o *Feign Client* trocado por `RestClient` (o Spring Cloud 2024.0.0 declarado antes foi lançado para o Spring Boot 3.4, não para o 4.x).

## 🚦 Ordem recomendada de execução

Pré-requisitos: **JDK 21**, **Maven 3.9+** e **Node 18+** (para o Angular).

### 1. Core Bancário — `http://localhost:8080/api/v1`
```bash
cd fiap-bank-core-bancario
mvn clean install
cd fiap-bank-core-bancario-presentation && mvn spring-boot:run
```

### 2. Plataforma de Crédito — `http://localhost:8081`
```bash
cd fiap-bank-plataforma-credito
mvn clean install
cd fiap-bank-plataforma-credito-presentation && mvn spring-boot:run
```
API RESTful em `/api/v1/solicitacoes`; rotas legadas em `/api/credito/*`.

### 3. Mesa de Operações — `http://localhost:8082`
```bash
cd fiap-bank-mesa-operacoes-credito
mvn spring-boot:run
```

### 4. Internet Banking Mobile — `http://localhost:4200`
```bash
cd fiap-bank-internet-banking-mobile
npm install
npm run start
```

## 🧪 Dados pré-carregados

| Cliente | CPF | Conta (Core) | Resultado esperado na esteira |
| :--- | :--- | :---: | :--- |
| Ana Silva | `12345678900` | `12345-6` | Aprovável (parcela < 30% da renda) |
| Carlos Eduardo Oliveira | `98765432100` | `78901-2` | Vai para a Mesa de Operações (parcela > 30% da renda) |
| Mariana Souza | `55544433399` | `34567-8` | Reprovada automaticamente (restrição no SCR — CPF terminado em 9) |

## 🔎 Verificação rápida

```bash
curl -s http://localhost:8080/api/v1/contas/cliente/12345678900                         # Core
curl -s http://localhost:8081/api/v1/solicitacoes                                        # Plataforma (RESTful)
curl -s -X POST http://localhost:8081/api/credito/listarTodasSolicitacoes                # Plataforma (rota legada)
```

# FIAP Bank - Plataforma de Crédito 💳

Microsserviço de **esteira e motor de decisão de crédito** (porta **8081**). Processa propostas, consulta **SCR**, **Bureau de Crédito**, **CRM** e **Core Bancário**, decide automaticamente (aprova, reprova ou encaminha para a **Mesa de Operações**) e dispara notificações.

Esta versão foi **refatorada** de um projeto monolítico (um único módulo, entidade JPA misturada ao serviço, rotas todas `POST` com verbo de ação no path) para **DDD + Maven multi-módulo + API RESTful**, mantendo a compatibilidade com os front-ends legados por meio de um **controller adaptador**.

## 🛠️ Tecnologias

Java 21 · Spring Boot 4.1.1 · Spring Web MVC · Spring Data JPA · Jakarta Bean Validation · Spring `RestClient` (cliente HTTP do Core) · Actuator · H2 (em memória) · Lombok · Maven multi-módulo · maven-enforcer-plugin · ArchUnit

## 🧱 Arquitetura (4 submódulos físicos)

```
fiap-bank-plataforma-credito       (pom agregador, packaging pom)
├── ...-domain                     SolicitacaoCredito, VOs, MotorDecisaoCredito, portas (repositório e gateways)
├── ...-infrastructure             Entidade JPA, Spring Data JPA, adapters e clients (Core, SCR, Bureau, CRM, Notificações)
├── ...-application                DTOs + Bean Validation, serviços (casos de uso), exceções de aplicação
└── ...-presentation               @SpringBootApplication, controllers RESTful, controller adaptador legado, handler global
```

`presentation -> application -> infrastructure -> domain` (e `application -> domain`), validado pelo `maven-enforcer-plugin` (mesmas regras do Core Bancário) e por teste ArchUnit.

**Portas e adaptadores:** o domínio define as interfaces (`SolicitacaoCreditoRepository`, `ScrGateway`, `BureauCreditoGateway`, `CrmGateway`, `CoreBancarioGateway`, `NotificacaoGateway`); a infraestrutura as implementa. SCR, Bureau, CRM e Notificações são simulações; o Core Bancário é consumido de verdade via HTTP (`GET /api/v1/contas/cliente/{cpf}`).

### Regras do motor de crédito (`MotorDecisaoCredito`, domínio puro)

1. Restrição no SCR (CPF terminado em `9`) → **REPROVADO**.
2. Score do Bureau < 500 → **REPROVADO**.
3. Bloqueio interno no CRM (CPF terminado em `000`) → **REPROVADO**.
4. Cliente sem conta ativa no Core Bancário → **REPROVADO**.
5. Core Bancário indisponível → **EM_ANALISE** (nunca aprova sem validar a conta; não há mais dados "inventados" de fallback).
6. Parcela estimada (valor ÷ parcelas) > 30% da renda → **EM_ANALISE** (Mesa de Operações); caso contrário → **APROVADO**.

## 🚀 Como executar

Pré-requisitos: **JDK 21**, **Maven 3.9+** e, para o fluxo completo, o **Core Bancário** rodando na porta 8080.

```bash
cd fiap-bank-plataforma-credito
mvn clean install

cd fiap-bank-plataforma-credito-presentation
mvn spring-boot:run
# ou: java -jar target/fiap-bank-plataforma-credito-presentation-1.0.0-SNAPSHOT.jar
```

Configuração (`application.yml` ou variável de ambiente): `server.port` = `8081`; `core.bancario.url` (`CORE_BANCARIO_URL`) = `http://localhost:8080/api/v1`; `core.bancario.connect-timeout-ms` / `read-timeout-ms`; `app.cors.allowed-origin-patterns` (CORS do app Angular). Health: `http://localhost:8081/actuator/health`.

## 🌐 API RESTful (`/api/v1`)

| Verbo | URI | Sucesso | Erros | Descrição |
| :--- | :--- | :---: | :---: | :--- |
| `POST` | `/api/v1/solicitacoes` | 201 + `Location` | 400 | Abre uma solicitação e executa o motor de crédito |
| `GET` | `/api/v1/solicitacoes` | 200 | 400 | Lista todas; filtro opcional `?cpfCliente=` |
| `GET` | `/api/v1/solicitacoes/{id}` | 200 | 404 | Recupera uma solicitação |
| `PUT` | `/api/v1/solicitacoes/{id}/decisao` | 200 | 400, 404, 409 | Mesa aprova/reprova (idempotente) |
| `GET` | `/api/v1/clientes/{cpf}/conta` | 200 | 400, 404, 503 | Conta do cliente no Core Bancário |

Exemplos:

```bash
curl -s -X POST http://localhost:8081/api/v1/solicitacoes -H 'Content-Type: application/json' -d '{
  "cpfCliente":"12345678900","nomeCliente":"Ana Silva","tipoCredito":"EMPRESTIMO_PESSOAL",
  "valorSolicitado":15000.00,"quantidadeParcelas":24,"rendaMensal":7500.00}'

curl -s "http://localhost:8081/api/v1/solicitacoes?cpfCliente=12345678900"

curl -s -X PUT http://localhost:8081/api/v1/solicitacoes/2/decisao -H 'Content-Type: application/json' \
  -d '{"decisao":"APROVADO","parecer":"Documentação conferida"}'
```

`tipoCredito`: `EMPRESTIMO_PESSOAL`, `CREDITO_IMOBILIARIO`, `CREDITO_AUTOMOTIVO` ou `CONSIGNADO` (a leitura também aceita os rótulos legados, ex.: `"EMPRÉSTIMO PESSOAL"`). Erros seguem `application/problem+json` (RFC 9457) com a lista de campos inválidos quando aplicável.

## 🔁 Retrocompatibilidade — controller adaptador (`/api/credito`)

As rotas antigas, orientadas a ação (todas `POST`), **continuam funcionando com o mesmo contrato** para não quebrar o Internet Banking Mobile (Angular) e a Mesa de Operações (Thymeleaf). `CreditoLegadoAdapterController` apenas traduz o contrato legado e delega para os **mesmos casos de uso** da API RESTful (sem regra de negócio duplicada) e marca as respostas com `Deprecation: true` e `Link: </api/v1/solicitacoes>; rel="successor-version"`.

| Rota legada | Rota RESTful sucessora |
| :--- | :--- |
| `POST /api/credito/solicitarCredito` | `POST /api/v1/solicitacoes` |
| `POST /api/credito/consultarStatusCredito` `{cpfCliente}` | `GET /api/v1/solicitacoes?cpfCliente=` |
| `POST /api/credito/buscarSolicitacaoPorId` `{solicitacaoId}` | `GET /api/v1/solicitacoes/{id}` |
| `POST /api/credito/listarTodasSolicitacoes` | `GET /api/v1/solicitacoes` |
| `POST /api/credito/aprovarSolicitacao` `{solicitacaoId, parecerOperador}` | `PUT /api/v1/solicitacoes/{id}/decisao` |
| `POST /api/credito/reprovarSolicitacao` `{solicitacaoId, motivoReprovacao}` | `PUT /api/v1/solicitacoes/{id}/decisao` |
| `POST /api/credito/consultarClienteCore` `{cpfCliente}` | `GET /api/v1/clientes/{cpf}/conta` |

Na resposta legada, `tipoCredito` mantém o rótulo histórico (ex.: `EMPRÉSTIMO PESSOAL`), como o app Angular espera.

## 🧪 Testes

`mvn test`: tipos de crédito (rótulos legados), transições da solicitação, motor de decisão, serviço de aplicação (Mockito) e ArchUnit.

# FIAP Bank - Core Bancário 🏦

Microsserviço de **Core Bancário** (porta **8080**) responsável pela gestão central de clientes e contas do FIAP Bank. Construído do zero com **Domain-Driven Design** e **Maven multi-módulo**, expondo uma API **RESTful** (`/api/v1`).

## 🛠️ Tecnologias

Java 21 · Spring Boot 4.1.1 · Spring Web MVC · Spring Data JPA · Jakarta Bean Validation · H2 (em memória) · Lombok · Maven multi-módulo · maven-enforcer-plugin · ArchUnit

## 🧱 Arquitetura (4 submódulos físicos)

```
fiap-bank-core-bancario            (pom agregador, packaging pom)
├── ...-domain                     Entidades, Value Objects e interfaces de Repositório (Java puro + Lombok)
├── ...-infrastructure             Ports & Adapters: entidades JPA, Spring Data JPA, adapters, carga inicial
├── ...-application                DTOs (Java Records + Bean Validation), serviços (casos de uso), exceções
└── ...-presentation               @SpringBootApplication, controllers RESTful, GlobalExceptionHandler
```

Hierarquia de dependências (imposta em tempo de build):

`presentation -> application -> infrastructure -> domain` (e `application -> domain`).

| Regra | Como é garantida |
| :--- | :--- |
| `domain` totalmente isolado (sem Spring Web, Spring Data JPA, `@Entity`, `@Table`, `@Column`, `@Id`) | `maven-enforcer-plugin` (`bannedDependencies`, busca transitiva) no `pom.xml` do `domain` |
| `infrastructure` só enxerga `domain`; `application` não enxerga `presentation` | `maven-enforcer-plugin` nos respectivos `pom.xml` |
| `presentation` **não** declara dependência direta de `domain`/`infrastructure` | `maven-enforcer-plugin` (`searchTransitive=false`) → o build falha imediatamente |
| `presentation` não referencia classes de `domain`/`infrastructure` no código | Teste de arquitetura `ArquiteturaEmCamadasTest` (ArchUnit) |

> Para testar a proteção: adicione `fiap-bank-core-bancario-domain` como dependência do `presentation` e rode `mvn validate` — o build deve falhar com *"Violacao de hierarquia"*.

**Modelo de domínio** (conforme o diagrama de classes do enunciado): `Cliente`, `ContaBancaria`, VOs imutáveis `Cpf`, `Email` e `Saldo` (`adicionar`/`subtrair`), enums `StatusConta` (`ATIVA`, `BLOQUEADA`, `ENCERRADA`) e `TipoConta` (`CORRENTE`, `POUPANCA`, `SALARIO`). Um cliente possui no máximo uma conta. A conta usa bloqueio otimista (`@Version`) para que movimentações concorrentes não sobrescrevam o saldo.

## 🚀 Como executar

Pré-requisitos: **JDK 21** e **Maven 3.9+**.

```bash
cd fiap-bank-core-bancario
mvn clean install                       # compila, roda os testes e valida as regras do enforcer

# opção 1: via Maven
cd fiap-bank-core-bancario-presentation
mvn spring-boot:run

# opção 2: via JAR executável
java -jar fiap-bank-core-bancario-presentation/target/fiap-bank-core-bancario-presentation-1.0.0-SNAPSHOT.jar
```

A API sobe em **http://localhost:8080** (as outras três aplicações do ecossistema usam as portas 8081, 8082 e 4200, portanto os quatro projetos rodam simultaneamente).

## 📌 Carga inicial

| Cliente | CPF | Agência | Conta | Saldo |
| :--- | :--- | :---: | :---: | ---: |
| Ana Silva | `12345678900` | `0001` | `12345-6` | R$ 12.500,00 |
| Carlos Eduardo Oliveira | `98765432100` | `0001` | `78901-2` | R$ 45.800,50 |
| Mariana Souza | `55544433399` | `0001` | `34567-8` | R$ 3.200,00 |

## 🌐 API RESTful (`/api/v1`)

Recursos = substantivos no plural; a intenção está no verbo HTTP. Veja a especificação completa na seção 9/10 do enunciado.

### Clientes (`/api/v1/clientes`)

| Verbo | URI | Sucesso | Erros |
| :--- | :--- | :---: | :---: |
| `GET` | `/api/v1/clientes` | 200 | — |
| `GET` | `/api/v1/clientes/{id}` | 200 | 400, 404 |
| `GET` | `/api/v1/clientes/cpf/{cpf}` | 200 | 400, 404 |
| `POST` | `/api/v1/clientes` | 201 + `Location` | 400, 409 |
| `PUT` | `/api/v1/clientes/{id}` | 200 | 400, 404 |
| `DELETE` | `/api/v1/clientes/{id}` | 204 | 400, 404 |

### Contas (`/api/v1/contas`)

| Verbo | URI | Sucesso | Erros |
| :--- | :--- | :---: | :---: |
| `GET` | `/api/v1/contas` | 200 | — |
| `GET` | `/api/v1/contas/{numeroConta}` | 200 | 400, 404 |
| `GET` | `/api/v1/contas/cliente/{cpf}` | 200 | 400, 404 |
| `POST` | `/api/v1/contas` | 201 + `Location` | 400, 409 |
| `PUT` | `/api/v1/contas/{numeroConta}/saldo` | 200 | 400, 404, 409 |
| `DELETE` | `/api/v1/contas/{numeroConta}` | 204 (encerramento lógico, idempotente) | 400, 404 |

Decisões de projeto: listagens vazias retornam `200 []` (e não 404); `POST /contas` com CPF de cliente inexistente retorna `400` (referência inválida no payload, conforme a matriz do enunciado); `409` indica CPF duplicado, cliente que já possui conta, saldo insuficiente, conta não ativa ou conflito de concorrência; `DELETE /clientes/{id}` remove também a conta vinculada.

### Validações (Jakarta Bean Validation nos DTOs, `@Valid` nos controllers)

- `CriarClienteRequest`: `cpf` (11 dígitos ou `XXX.XXX.XXX-XX`), `nome` (3–150), `email` (válido), `dataNascimento` (passado), `rendaMensal` (≥ 0).
- `CriarContaRequest`: `cpfCliente`, `agencia` (opcional, 4 dígitos, padrão `0001`), `tipoConta`, `saldoInicial` (≥ 0).
- `AtualizarSaldoRequest`: `operacao` (`DEPOSITO`/`SAQUE`, *case-insensitive*), `valor` (> 0).

### Erros padronizados (`GlobalExceptionHandler`, `@RestControllerAdvice`)

Todas as falhas retornam `application/problem+json` (RFC 9457), por exemplo:

```json
{
  "type": "about:blank",
  "title": "Requisição inválida",
  "status": 400,
  "detail": "Um ou mais campos da requisição são inválidos.",
  "erros": [{ "campo": "cpf", "mensagem": "O CPF deve conter 11 dígitos numéricos ou o formato XXX.XXX.XXX-XX." }]
}
```

### Exemplos

```bash
curl -s http://localhost:8080/api/v1/contas/cliente/12345678900
curl -s -X POST http://localhost:8080/api/v1/clientes -H 'Content-Type: application/json' \
  -d '{"cpf":"111.222.333-44","nome":"Maria Souza","email":"maria@email.com","dataNascimento":"1995-05-10","rendaMensal":5000.00}'
curl -s -X POST http://localhost:8080/api/v1/contas -H 'Content-Type: application/json' \
  -d '{"cpfCliente":"11122233344","tipoConta":"CORRENTE","saldoInicial":100.00}'
curl -s -X PUT http://localhost:8080/api/v1/contas/12345-6/saldo -H 'Content-Type: application/json' \
  -d '{"operacao":"DEPOSITO","valor":250.00}'
```

## 🧪 Testes

`mvn test` executa testes unitários do domínio (VOs, regras de conta e cliente), do serviço de contas (Mockito) e o teste de arquitetura (ArchUnit).

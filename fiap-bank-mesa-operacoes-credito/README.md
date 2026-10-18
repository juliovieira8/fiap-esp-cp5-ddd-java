# FIAP Bank - Mesa de Operações de Crédito 🧾

Front-end web (**Thymeleaf**, porta **8082**) para operadores de crédito analisarem, aprovarem ou reprovarem as propostas encaminhadas pela Plataforma de Crédito.

## 🛠️ Tecnologias

Java 21 · Spring Boot 4.1.1 · Spring Web MVC · Thymeleaf · Spring `RestClient`

## 🔌 Integração com a Plataforma de Crédito

A Mesa consome os **contratos históricos** da Plataforma (`POST /api/credito/<ação>`), preservados pelo **controller adaptador de retrocompatibilidade** — nenhuma tela ou contrato foi quebrado pela refatoração para DDD/RESTful.

Ajustes realizados nesta entrega:

- O antigo *Feign Client* (Spring Cloud OpenFeign 2024.0.0) foi substituído por um cliente baseado em **`RestClient`** (`PlataformaCreditoClient`), pois o Spring Cloud 2024.0.0 foi lançado para o Spring Boot 3.4 e não é a linha de versões do Spring Boot 4.x usado no projeto.
- Os redirecionamentos de sucesso/erro passaram a usar `RedirectAttributes` (parâmetros codificados) e as mensagens de erro da Plataforma (`application/problem+json`) são exibidas ao operador.

URL da Plataforma: `plataforma.credito.url` em `src/main/resources/application.yml` (padrão `http://localhost:8081`).

## 🚀 Como executar

```bash
cd fiap-bank-mesa-operacoes-credito
mvn clean package
mvn spring-boot:run
```

Painel do operador: **http://localhost:8082** (suba antes o Core Bancário e a Plataforma de Crédito).

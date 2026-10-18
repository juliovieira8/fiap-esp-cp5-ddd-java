package br.com.fiap.bank.plataforma.infrastructure.client;

import br.com.fiap.bank.plataforma.domain.exception.ServicoExternoIndisponivelException;
import br.com.fiap.bank.plataforma.domain.gateway.CoreBancarioGateway;
import br.com.fiap.bank.plataforma.domain.model.ContaCore;
import br.com.fiap.bank.plataforma.domain.model.Cpf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Client de comunicacao com o Core Bancario (adapter do port {@link CoreBancarioGateway}).
 * Consome a API RESTful {@code GET /api/v1/contas/cliente/{cpf}}.
 */
@Slf4j
@Component
public class CoreBancarioRestClient implements CoreBancarioGateway {

    private static final ParameterizedTypeReference<Map<String, Object>> TIPO_MAPA =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public CoreBancarioRestClient(
            @Value("${core.bancario.url:http://localhost:8080/api/v1}") String urlBase,
            @Value("${core.bancario.connect-timeout-ms:2000}") long connectTimeoutMs,
            @Value("${core.bancario.read-timeout-ms:5000}") long readTimeoutMs) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        this.restClient = RestClient.builder()
                .baseUrl(urlBase)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public Optional<ContaCore> consultarContaPorCpf(Cpf cpf) {
        try {
            Map<String, Object> corpo = restClient.get()
                    .uri("/contas/cliente/{cpf}", cpf.getValor())
                    .retrieve()
                    .body(TIPO_MAPA);
            return Optional.ofNullable(corpo).map(CoreBancarioRestClient::paraConta);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            log.warn("Core Bancário indisponível ao consultar a conta do CPF {}: {}", cpf, e.getMessage());
            throw new ServicoExternoIndisponivelException(
                    "Não foi possível consultar o Core Bancário no momento.", e);
        }
    }

    private static ContaCore paraConta(Map<String, Object> corpo) {
        return new ContaCore(
                texto(corpo.get("numeroConta")),
                texto(corpo.get("agencia")),
                texto(corpo.get("statusConta")),
                corpo.get("saldo") == null ? null : new BigDecimal(String.valueOf(corpo.get("saldo"))));
    }

    private static String texto(Object valor) {
        return valor == null ? null : String.valueOf(valor);
    }
}

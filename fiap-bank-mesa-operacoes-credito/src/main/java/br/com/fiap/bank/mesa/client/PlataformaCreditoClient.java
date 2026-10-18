package br.com.fiap.bank.mesa.client;

import br.com.fiap.bank.mesa.dto.AprovarSolicitacaoRequestDTO;
import br.com.fiap.bank.mesa.dto.BuscarSolicitacaoRequestDTO;
import br.com.fiap.bank.mesa.dto.ReprovarSolicitacaoRequestDTO;
import br.com.fiap.bank.mesa.dto.SolicitacaoCreditoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

/**
 * Cliente HTTP da Plataforma de Credito (substitui o antigo Feign Client, que dependia do Spring
 * Cloud 2024.0.0, lancado para o Spring Boot 3.4 e nao para o 4.x). Consome os contratos historicos
 * orientados a acao ({@code POST /api/credito/<acao>}), preservados na Plataforma pelo controller
 * adaptador de retrocompatibilidade.
 */
@Component
public class PlataformaCreditoClient {

    private static final String BASE_LEGADA = "/api/credito";

    private final RestClient restClient;

    public PlataformaCreditoClient(@Value("${plataforma.credito.url:http://localhost:8081}") String urlBase) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder()
                .baseUrl(urlBase)
                .requestFactory(requestFactory)
                .build();
    }

    public List<SolicitacaoCreditoDTO> listarTodasSolicitacoes() {
        List<SolicitacaoCreditoDTO> solicitacoes = restClient.post()
                .uri(BASE_LEGADA + "/listarTodasSolicitacoes")
                .retrieve()
                .body(new ParameterizedTypeReference<List<SolicitacaoCreditoDTO>>() {
                });
        return solicitacoes == null ? List.of() : solicitacoes;
    }

    public SolicitacaoCreditoDTO buscarSolicitacaoPorId(BuscarSolicitacaoRequestDTO request) {
        return enviar("/buscarSolicitacaoPorId", request);
    }

    public SolicitacaoCreditoDTO aprovarSolicitacao(AprovarSolicitacaoRequestDTO request) {
        return enviar("/aprovarSolicitacao", request);
    }

    public SolicitacaoCreditoDTO reprovarSolicitacao(ReprovarSolicitacaoRequestDTO request) {
        return enviar("/reprovarSolicitacao", request);
    }

    private SolicitacaoCreditoDTO enviar(String acao, Object corpo) {
        return restClient.post()
                .uri(BASE_LEGADA + acao)
                .contentType(MediaType.APPLICATION_JSON)
                .body(corpo)
                .retrieve()
                .body(SolicitacaoCreditoDTO.class);
    }
}

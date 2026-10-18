package br.com.fiap.bank.plataforma.presentation.legacy;

import br.com.fiap.bank.plataforma.application.dto.ContaCoreResponse;
import br.com.fiap.bank.plataforma.application.dto.DecisaoOperador;
import br.com.fiap.bank.plataforma.application.dto.DecisaoSolicitacaoRequest;
import br.com.fiap.bank.plataforma.application.dto.SolicitacaoCreditoResponse;
import br.com.fiap.bank.plataforma.application.dto.SolicitarCreditoRequest;
import br.com.fiap.bank.plataforma.application.service.ContaCoreService;
import br.com.fiap.bank.plataforma.application.service.SolicitacaoCreditoService;
import br.com.fiap.bank.plataforma.presentation.legacy.dto.AprovarSolicitacaoRequest;
import br.com.fiap.bank.plataforma.presentation.legacy.dto.BuscarSolicitacaoRequest;
import br.com.fiap.bank.plataforma.presentation.legacy.dto.ConsultarStatusRequest;
import br.com.fiap.bank.plataforma.presentation.legacy.dto.ReprovarSolicitacaoRequest;
import br.com.fiap.bank.plataforma.presentation.legacy.dto.SolicitacaoCreditoLegadoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <b>Controller Adaptador (Adapter) de retrocompatibilidade.</b>
 *
 * <p>Preserva, sem nenhuma alteracao de contrato, as rotas historicas orientadas a acao
 * ({@code POST /api/credito/<acao>}) consumidas pelo Internet Banking Mobile (Angular) e pela Mesa de
 * Operacoes (Thymeleaf). Cada rota apenas <i>traduz</i> o contrato legado e delega para os mesmos
 * casos de uso da camada de aplicacao usados pela API RESTful ({@code /api/v1/solicitacoes}): nao
 * existe regra de negocio duplicada aqui.
 *
 * <p>As respostas carregam os cabecalhos {@code Deprecation} e {@code Link: rel="successor-version"}
 * indicando a rota RESTful sucessora, para que os clientes possam migrar gradualmente.
 *
 * @deprecated mantido apenas ate que todos os front-ends migrem para {@code /api/v1}.
 */
@Deprecated(since = "1.0.0")
@RestController
@RequestMapping("/api/credito")
@RequiredArgsConstructor
public class CreditoLegadoAdapterController {

    private static final String SUCESSORA_SOLICITACOES = "</api/v1/solicitacoes>; rel=\"successor-version\"";
    private static final String SUCESSORA_CONTA = "</api/v1/clientes/{cpf}/conta>; rel=\"successor-version\"";

    private final SolicitacaoCreditoService solicitacaoService;
    private final ContaCoreService contaCoreService;

    /** Legado: {@code solicitarCredito} -> {@code POST /api/v1/solicitacoes}. */
    @PostMapping("/solicitarCredito")
    public ResponseEntity<SolicitacaoCreditoLegadoResponse> solicitarCredito(
            @Valid @RequestBody SolicitarCreditoRequest request) {
        return ok(SolicitacaoCreditoLegadoResponse.de(solicitacaoService.solicitar(request)), SUCESSORA_SOLICITACOES);
    }

    /** Legado: {@code consultarStatusCredito} -> {@code GET /api/v1/solicitacoes?cpfCliente=}. */
    @PostMapping("/consultarStatusCredito")
    public ResponseEntity<List<SolicitacaoCreditoLegadoResponse>> consultarStatusCredito(
            @Valid @RequestBody ConsultarStatusRequest request) {
        return ok(paraLegado(solicitacaoService.listar(request.cpfCliente())), SUCESSORA_SOLICITACOES);
    }

    /** Legado: {@code buscarSolicitacaoPorId} -> {@code GET /api/v1/solicitacoes/{id}}. */
    @PostMapping("/buscarSolicitacaoPorId")
    public ResponseEntity<SolicitacaoCreditoLegadoResponse> buscarSolicitacaoPorId(
            @Valid @RequestBody BuscarSolicitacaoRequest request) {
        return ok(SolicitacaoCreditoLegadoResponse.de(solicitacaoService.buscarPorId(request.solicitacaoId())),
                SUCESSORA_SOLICITACOES);
    }

    /** Legado: {@code listarTodasSolicitacoes} -> {@code GET /api/v1/solicitacoes}. */
    @PostMapping("/listarTodasSolicitacoes")
    public ResponseEntity<List<SolicitacaoCreditoLegadoResponse>> listarTodasSolicitacoes() {
        return ok(paraLegado(solicitacaoService.listar(null)), SUCESSORA_SOLICITACOES);
    }

    /** Legado: {@code aprovarSolicitacao} -> {@code PUT /api/v1/solicitacoes/{id}/decisao}. */
    @PostMapping("/aprovarSolicitacao")
    public ResponseEntity<SolicitacaoCreditoLegadoResponse> aprovarSolicitacao(
            @Valid @RequestBody AprovarSolicitacaoRequest request) {
        SolicitacaoCreditoResponse resposta = solicitacaoService.registrarDecisao(request.solicitacaoId(),
                new DecisaoSolicitacaoRequest(DecisaoOperador.APROVADO, request.parecerOperador()));
        return ok(SolicitacaoCreditoLegadoResponse.de(resposta), SUCESSORA_SOLICITACOES);
    }

    /** Legado: {@code reprovarSolicitacao} -> {@code PUT /api/v1/solicitacoes/{id}/decisao}. */
    @PostMapping("/reprovarSolicitacao")
    public ResponseEntity<SolicitacaoCreditoLegadoResponse> reprovarSolicitacao(
            @Valid @RequestBody ReprovarSolicitacaoRequest request) {
        SolicitacaoCreditoResponse resposta = solicitacaoService.registrarDecisao(request.solicitacaoId(),
                new DecisaoSolicitacaoRequest(DecisaoOperador.REPROVADO, request.motivoReprovacao()));
        return ok(SolicitacaoCreditoLegadoResponse.de(resposta), SUCESSORA_SOLICITACOES);
    }

    /** Legado: {@code consultarClienteCore} -> {@code GET /api/v1/clientes/{cpf}/conta}. */
    @PostMapping("/consultarClienteCore")
    public ResponseEntity<ContaCoreResponse> consultarClienteCore(@Valid @RequestBody ConsultarStatusRequest request) {
        return ok(contaCoreService.consultarPorCpf(request.cpfCliente()), SUCESSORA_CONTA);
    }

    private static List<SolicitacaoCreditoLegadoResponse> paraLegado(List<SolicitacaoCreditoResponse> respostas) {
        return respostas.stream().map(SolicitacaoCreditoLegadoResponse::de).toList();
    }

    private static <T> ResponseEntity<T> ok(T corpo, String rotaSucessora) {
        return ResponseEntity.ok()
                .header("Deprecation", "true")
                .header("Link", rotaSucessora)
                .body(corpo);
    }
}

package br.com.fiap.bank.plataforma.presentation.controller;

import br.com.fiap.bank.plataforma.application.dto.DecisaoSolicitacaoRequest;
import br.com.fiap.bank.plataforma.application.dto.SolicitacaoCreditoResponse;
import br.com.fiap.bank.plataforma.application.dto.SolicitarCreditoRequest;
import br.com.fiap.bank.plataforma.application.service.SolicitacaoCreditoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * API RESTful da esteira de credito. O recurso e o substantivo plural {@code /solicitacoes}; a
 * intencao da operacao e dada pelo verbo HTTP (nada de verbos de acao no path):
 * <ul>
 *   <li>{@code POST /api/v1/solicitacoes} cria uma solicitacao (201 Created + Location);</li>
 *   <li>{@code GET /api/v1/solicitacoes[?cpfCliente=]} lista/filtra a colecao;</li>
 *   <li>{@code GET /api/v1/solicitacoes/{id}} recupera uma solicitacao;</li>
 *   <li>{@code PUT /api/v1/solicitacoes/{id}/decisao} registra (idempotente) a decisao da Mesa.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/solicitacoes")
@RequiredArgsConstructor
public class SolicitacaoCreditoController {

    private final SolicitacaoCreditoService solicitacaoService;

    @PostMapping
    public ResponseEntity<SolicitacaoCreditoResponse> solicitar(@Valid @RequestBody SolicitarCreditoRequest request) {
        SolicitacaoCreditoResponse criada = solicitacaoService.solicitar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criada.id())
                .toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @GetMapping
    public List<SolicitacaoCreditoResponse> listar(
            @RequestParam(name = "cpfCliente", required = false) String cpfCliente) {
        return solicitacaoService.listar(cpfCliente);
    }

    @GetMapping("/{id}")
    public SolicitacaoCreditoResponse buscarPorId(@PathVariable("id") Long id) {
        return solicitacaoService.buscarPorId(id);
    }

    @PutMapping("/{id}/decisao")
    public SolicitacaoCreditoResponse registrarDecisao(@PathVariable("id") Long id,
                                                       @Valid @RequestBody DecisaoSolicitacaoRequest request) {
        return solicitacaoService.registrarDecisao(id, request);
    }
}

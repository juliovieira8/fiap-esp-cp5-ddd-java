package br.com.fiap.bank.core.presentation.controller;

import br.com.fiap.bank.core.application.dto.AtualizarSaldoRequest;
import br.com.fiap.bank.core.application.dto.ContaResponse;
import br.com.fiap.bank.core.application.dto.CriarContaRequest;
import br.com.fiap.bank.core.application.service.ContaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Recurso canonico {@code /api/v1/contas}. O saldo e um sub-recurso
 * ({@code /contas/{numeroConta}/saldo}) alterado por PUT.
 */
@RestController
@RequestMapping("/api/v1/contas")
@RequiredArgsConstructor
public class ContaController {

    private final ContaService contaService;

    @GetMapping
    public List<ContaResponse> listar() {
        return contaService.listar();
    }

    @GetMapping("/{numeroConta}")
    public ContaResponse buscarPorNumero(@PathVariable("numeroConta") String numeroConta) {
        return contaService.buscarPorNumero(numeroConta);
    }

    @GetMapping("/cliente/{cpf}")
    public ContaResponse buscarPorCpf(@PathVariable("cpf") String cpf) {
        return contaService.buscarPorCpf(cpf);
    }

    @PostMapping
    public ResponseEntity<ContaResponse> criar(@Valid @RequestBody CriarContaRequest request) {
        ContaResponse criada = contaService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{numeroConta}")
                .buildAndExpand(criada.numeroConta())
                .toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @PutMapping("/{numeroConta}/saldo")
    public ContaResponse atualizarSaldo(@PathVariable("numeroConta") String numeroConta,
                                        @Valid @RequestBody AtualizarSaldoRequest request) {
        return contaService.atualizarSaldo(numeroConta, request);
    }

    @DeleteMapping("/{numeroConta}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void encerrar(@PathVariable("numeroConta") String numeroConta) {
        contaService.encerrar(numeroConta);
    }
}

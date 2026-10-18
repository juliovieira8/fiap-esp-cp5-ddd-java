package br.com.fiap.bank.plataforma.presentation.controller;

import br.com.fiap.bank.plataforma.application.dto.ContaCoreResponse;
import br.com.fiap.bank.plataforma.application.service.ContaCoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Consulta RESTful da conta do cliente no Core Bancario: {@code GET /api/v1/clientes/{cpf}/conta}. */
@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ContaCoreController {

    private final ContaCoreService contaCoreService;

    @GetMapping("/{cpf}/conta")
    public ContaCoreResponse consultarConta(@PathVariable("cpf") String cpf) {
        return contaCoreService.consultarPorCpf(cpf);
    }
}

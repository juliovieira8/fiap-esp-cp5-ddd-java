package br.com.fiap.bank.plataforma.application.service;

import br.com.fiap.bank.plataforma.application.dto.ContaCoreResponse;
import br.com.fiap.bank.plataforma.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.bank.plataforma.domain.gateway.CoreBancarioGateway;
import br.com.fiap.bank.plataforma.domain.model.Cpf;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static br.com.fiap.bank.plataforma.application.exception.TradutorDeExcecoes.executar;

/** Caso de uso: consulta da conta do cliente no Core Bancario (usado pelo Internet Banking Mobile). */
@Service
@RequiredArgsConstructor
public class ContaCoreService {

    private final CoreBancarioGateway coreBancarioGateway;

    public ContaCoreResponse consultarPorCpf(String cpf) {
        return executar(() -> coreBancarioGateway.consultarContaPorCpf(Cpf.de(cpf))
                .map(ContaCoreResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nenhuma conta encontrada no Core Bancário para o CPF informado.")));
    }
}

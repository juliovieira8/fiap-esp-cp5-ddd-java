package br.com.fiap.bank.plataforma.application.dto;

import br.com.fiap.bank.plataforma.domain.model.ContaCore;

import java.math.BigDecimal;

/** Dados da conta do cliente no Core Bancario (contrato consumido pelo Internet Banking Mobile). */
public record ContaCoreResponse(String statusConta, BigDecimal saldo, String agencia, String numeroConta) {

    public static ContaCoreResponse de(ContaCore conta) {
        return new ContaCoreResponse(conta.statusConta(), conta.saldo(), conta.agencia(), conta.numeroConta());
    }
}

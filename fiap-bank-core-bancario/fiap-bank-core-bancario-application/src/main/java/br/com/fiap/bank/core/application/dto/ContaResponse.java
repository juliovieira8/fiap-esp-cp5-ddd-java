package br.com.fiap.bank.core.application.dto;

import br.com.fiap.bank.core.domain.model.Cliente;
import br.com.fiap.bank.core.domain.model.ContaBancaria;
import br.com.fiap.bank.core.domain.model.StatusConta;
import br.com.fiap.bank.core.domain.model.TipoConta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Representacao externa (imutavel) de uma conta. Os nomes {@code numeroConta}, {@code agencia},
 * {@code statusConta} e {@code saldo} sao consumidos pela Plataforma de Credito.
 */
public record ContaResponse(
        UUID id,
        String numeroConta,
        String agencia,
        TipoConta tipoConta,
        StatusConta statusConta,
        BigDecimal saldo,
        LocalDate dataAbertura,
        String cpfTitular,
        String nomeTitular
) {

    public static ContaResponse de(ContaBancaria conta, Cliente titular) {
        return new ContaResponse(
                conta.getId(),
                conta.getNumero(),
                conta.getAgencia(),
                conta.getTipo(),
                conta.getStatus(),
                conta.getSaldo().getValor(),
                conta.getDataAbertura(),
                titular != null ? titular.getCpf().getValor() : null,
                titular != null ? titular.getNome() : null);
    }
}

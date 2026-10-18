package br.com.fiap.bank.plataforma.domain.model;

import java.math.BigDecimal;

/** Visao, no contexto da Plataforma, da conta do cliente mantida no Core Bancario. */
public record ContaCore(String numeroConta, String agencia, String statusConta, BigDecimal saldo) {

    public boolean ativa() {
        return "ATIVA".equalsIgnoreCase(statusConta);
    }
}

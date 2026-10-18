package br.com.fiap.bank.core.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/** Contrato de entrada para movimentacao de saldo (PUT /api/v1/contas/{numeroConta}/saldo). */
public record AtualizarSaldoRequest(

        @NotBlank(message = "A operação é obrigatória (DEPOSITO ou SAQUE).")
        @Pattern(regexp = "(?i)^(DEPOSITO|SAQUE)$", message = "A operação deve ser DEPOSITO ou SAQUE.")
        String operacao,

        @NotNull(message = "O valor é obrigatório.")
        @DecimalMin(value = "0.00", inclusive = false, message = "O valor deve ser maior que zero.")
        BigDecimal valor
) {
}

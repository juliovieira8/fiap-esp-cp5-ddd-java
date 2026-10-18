package br.com.fiap.bank.core.application.dto;

import br.com.fiap.bank.core.domain.model.TipoConta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/** Contrato de entrada para abertura de conta (POST /api/v1/contas). */
public record CriarContaRequest(

        @NotBlank(message = "O CPF do cliente titular é obrigatório.")
        @Pattern(regexp = "^(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$",
                message = "O CPF deve conter 11 dígitos numéricos ou o formato XXX.XXX.XXX-XX.")
        String cpfCliente,

        /** Opcional: assume "0001" quando ausente; se informada deve ter exatamente 4 dígitos. */
        @Pattern(regexp = "^\\d{4}$", message = "A agência deve possuir exatamente 4 dígitos numéricos.")
        String agencia,

        @NotNull(message = "O tipo da conta é obrigatório (CORRENTE, POUPANCA ou SALARIO).")
        TipoConta tipoConta,

        @NotNull(message = "O saldo inicial é obrigatório.")
        @DecimalMin(value = "0.00", message = "O saldo inicial não pode ser negativo.")
        BigDecimal saldoInicial
) {
}

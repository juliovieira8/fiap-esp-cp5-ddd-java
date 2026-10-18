package br.com.fiap.bank.plataforma.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Contrato de entrada para abrir uma solicitacao de credito. Compartilhado pela rota RESTful
 * ({@code POST /api/v1/solicitacoes}) e pela rota legada ({@code POST /api/credito/solicitarCredito}).
 */
public record SolicitarCreditoRequest(

        @NotBlank(message = "O CPF do cliente é obrigatório.")
        @Pattern(regexp = "^(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$",
                message = "O CPF deve conter 11 dígitos numéricos ou o formato XXX.XXX.XXX-XX.")
        String cpfCliente,

        @NotBlank(message = "O nome do cliente é obrigatório.")
        @Size(min = 3, max = 150, message = "O nome deve ter entre 3 e 150 caracteres.")
        String nomeCliente,

        /** EMPRESTIMO_PESSOAL, CREDITO_IMOBILIARIO, CREDITO_AUTOMOTIVO ou CONSIGNADO (aceita rotulos legados). */
        @NotBlank(message = "O tipo de crédito é obrigatório.")
        String tipoCredito,

        @NotNull(message = "O valor solicitado é obrigatório.")
        @DecimalMin(value = "0.00", inclusive = false, message = "O valor solicitado deve ser maior que zero.")
        BigDecimal valorSolicitado,

        @NotNull(message = "A quantidade de parcelas é obrigatória.")
        @Min(value = 1, message = "A quantidade de parcelas deve ser no mínimo 1.")
        @Max(value = 600, message = "A quantidade de parcelas deve ser no máximo 600.")
        Integer quantidadeParcelas,

        @NotNull(message = "A renda mensal é obrigatória.")
        @DecimalMin(value = "0.00", inclusive = false, message = "A renda mensal deve ser maior que zero.")
        BigDecimal rendaMensal
) {
}

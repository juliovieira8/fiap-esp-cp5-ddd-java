package br.com.fiap.bank.plataforma.presentation.legacy.dto;

import jakarta.validation.constraints.NotBlank;

/** Contrato historico de {@code POST /api/credito/consultarStatusCredito} e {@code /consultarClienteCore}. */
public record ConsultarStatusRequest(
        @NotBlank(message = "O CPF do cliente é obrigatório.") String cpfCliente
) {
}

package br.com.fiap.bank.plataforma.presentation.legacy.dto;

import jakarta.validation.constraints.NotNull;

/** Contrato historico de {@code POST /api/credito/buscarSolicitacaoPorId}. */
public record BuscarSolicitacaoRequest(
        @NotNull(message = "O identificador da solicitação é obrigatório.") Long solicitacaoId
) {
}

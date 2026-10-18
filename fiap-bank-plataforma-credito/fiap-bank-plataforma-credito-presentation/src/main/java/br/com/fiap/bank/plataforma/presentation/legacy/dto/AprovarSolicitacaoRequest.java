package br.com.fiap.bank.plataforma.presentation.legacy.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Contrato historico de {@code POST /api/credito/aprovarSolicitacao}. */
public record AprovarSolicitacaoRequest(
        @NotNull(message = "O identificador da solicitação é obrigatório.") Long solicitacaoId,
        @Size(max = 400, message = "O parecer deve ter no máximo 400 caracteres.") String parecerOperador
) {
}

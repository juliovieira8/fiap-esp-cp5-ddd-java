package br.com.fiap.bank.plataforma.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Corpo de {@code PUT /api/v1/solicitacoes/{id}/decisao}: a decisao e um sub-recurso da
 * solicitacao e substituida de forma idempotente.
 */
public record DecisaoSolicitacaoRequest(

        @NotNull(message = "A decisão é obrigatória (APROVADO ou REPROVADO).")
        DecisaoOperador decisao,

        @Size(max = 400, message = "O parecer deve ter no máximo 400 caracteres.")
        String parecer
) {
}

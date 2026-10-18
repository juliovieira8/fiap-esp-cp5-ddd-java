package br.com.fiap.bank.plataforma.domain.model;

/** Decisao automatica do motor de credito: novo status e parecer justificando. */
public record DecisaoCredito(StatusSolicitacao status, String parecer) {
}

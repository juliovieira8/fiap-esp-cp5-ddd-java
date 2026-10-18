package br.com.fiap.bank.plataforma.domain.model;

/** Consolidado das consultas aos sistemas externos usado pelo motor de decisao. */
public record AnaliseExterna(
        boolean restricaoScr,
        int scoreBureau,
        boolean bloqueioInternoCrm,
        SituacaoCore situacaoCore) {
}

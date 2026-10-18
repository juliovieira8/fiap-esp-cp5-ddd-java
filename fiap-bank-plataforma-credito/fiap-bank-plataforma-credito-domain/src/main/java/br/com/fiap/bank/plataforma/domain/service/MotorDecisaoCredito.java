package br.com.fiap.bank.plataforma.domain.service;

import br.com.fiap.bank.plataforma.domain.model.AnaliseExterna;
import br.com.fiap.bank.plataforma.domain.model.DecisaoCredito;
import br.com.fiap.bank.plataforma.domain.model.SituacaoCore;
import br.com.fiap.bank.plataforma.domain.model.SolicitacaoCredito;
import br.com.fiap.bank.plataforma.domain.model.StatusSolicitacao;

import java.math.BigDecimal;

/**
 * Servico de dominio: motor de regras de concessao de credito. Puro (sem Spring): e registrado
 * como bean pela camada de aplicacao.
 */
public class MotorDecisaoCredito {

    private static final int SCORE_MINIMO = 500;
    private static final BigDecimal LIMITE_COMPROMETIMENTO_RENDA = new BigDecimal("0.30");

    public DecisaoCredito avaliar(SolicitacaoCredito solicitacao, AnaliseExterna analise) {
        if (analise.restricaoScr()) {
            return new DecisaoCredito(StatusSolicitacao.REPROVADO,
                    "Solicitação reprovada automaticamente devido a restrições ativas no SCR (Banco Central).");
        }
        if (analise.scoreBureau() < SCORE_MINIMO) {
            return new DecisaoCredito(StatusSolicitacao.REPROVADO,
                    "Solicitação reprovada automaticamente. Score no Bureau de Crédito insuficiente ("
                            + analise.scoreBureau() + ").");
        }
        if (analise.bloqueioInternoCrm()) {
            return new DecisaoCredito(StatusSolicitacao.REPROVADO,
                    "Solicitação reprovada automaticamente: cliente com bloqueio interno no CRM corporativo.");
        }
        if (analise.situacaoCore() == SituacaoCore.INEXISTENTE || analise.situacaoCore() == SituacaoCore.INATIVA) {
            return new DecisaoCredito(StatusSolicitacao.REPROVADO,
                    "Solicitação reprovada automaticamente: o cliente não possui conta ativa no Core Bancário.");
        }
        if (analise.situacaoCore() == SituacaoCore.INDISPONIVEL) {
            return new DecisaoCredito(StatusSolicitacao.EM_ANALISE,
                    "Encaminhado para a Mesa de Operações de Crédito: não foi possível validar a conta no Core Bancário (serviço indisponível).");
        }

        BigDecimal parcelaEstimada = solicitacao.parcelaEstimada();
        BigDecimal limiteParcela = solicitacao.getRendaMensal().multiply(LIMITE_COMPROMETIMENTO_RENDA);
        if (parcelaEstimada.compareTo(limiteParcela) > 0) {
            return new DecisaoCredito(StatusSolicitacao.EM_ANALISE,
                    "Encaminhado para a Mesa de Operações de Crédito. Parcela estimada (R$ " + parcelaEstimada
                            + ") compromete mais de 30% da renda informada.");
        }
        return new DecisaoCredito(StatusSolicitacao.APROVADO,
                "Solicitação aprovada automaticamente pelo Motor de Crédito FIAP Bank!");
    }
}

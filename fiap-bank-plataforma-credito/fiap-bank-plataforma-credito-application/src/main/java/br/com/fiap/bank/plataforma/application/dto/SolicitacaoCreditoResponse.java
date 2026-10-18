package br.com.fiap.bank.plataforma.application.dto;

import br.com.fiap.bank.plataforma.domain.model.SolicitacaoCredito;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Representacao externa (imutavel) de uma solicitacao de credito. */
public record SolicitacaoCreditoResponse(
        Long id,
        String cpfCliente,
        String nomeCliente,
        String tipoCredito,
        String tipoCreditoDescricao,
        BigDecimal valorSolicitado,
        Integer quantidadeParcelas,
        BigDecimal rendaMensal,
        Integer scoreCredito,
        String status,
        String parecerOperador,
        Boolean scrRestricoes,
        Integer scoreBureau,
        LocalDateTime dataSolicitacao,
        LocalDateTime dataAtualizacao
) {

    public static SolicitacaoCreditoResponse de(SolicitacaoCredito solicitacao) {
        return new SolicitacaoCreditoResponse(
                solicitacao.getId(),
                solicitacao.getCpf().getValor(),
                solicitacao.getNomeCliente(),
                solicitacao.getTipoCredito().name(),
                solicitacao.getTipoCredito().getDescricao(),
                solicitacao.getValorSolicitado(),
                solicitacao.getQuantidadeParcelas(),
                solicitacao.getRendaMensal(),
                solicitacao.getScoreCredito(),
                solicitacao.getStatus().name(),
                solicitacao.getParecerOperador(),
                solicitacao.getScrRestricoes(),
                solicitacao.getScoreBureau(),
                solicitacao.getDataSolicitacao(),
                solicitacao.getDataAtualizacao());
    }
}

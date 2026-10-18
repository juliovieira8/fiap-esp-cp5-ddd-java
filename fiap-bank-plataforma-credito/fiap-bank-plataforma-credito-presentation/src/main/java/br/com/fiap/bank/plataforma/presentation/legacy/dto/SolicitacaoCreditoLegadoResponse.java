package br.com.fiap.bank.plataforma.presentation.legacy.dto;

import br.com.fiap.bank.plataforma.application.dto.SolicitacaoCreditoResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Resposta no formato historico consumido pelo Internet Banking Mobile (Angular) e pela Mesa de
 * Operacoes (Thymeleaf): mesmos campos de sempre, com {@code tipoCredito} no rotulo legado
 * (ex.: "EMPRÉSTIMO PESSOAL").
 */
public record SolicitacaoCreditoLegadoResponse(
        Long id,
        String cpfCliente,
        String nomeCliente,
        String tipoCredito,
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

    public static SolicitacaoCreditoLegadoResponse de(SolicitacaoCreditoResponse resposta) {
        return new SolicitacaoCreditoLegadoResponse(
                resposta.id(),
                resposta.cpfCliente(),
                resposta.nomeCliente(),
                resposta.tipoCreditoDescricao(),
                resposta.valorSolicitado(),
                resposta.quantidadeParcelas(),
                resposta.rendaMensal(),
                resposta.scoreCredito(),
                resposta.status(),
                resposta.parecerOperador(),
                resposta.scrRestricoes(),
                resposta.scoreBureau(),
                resposta.dataSolicitacao(),
                resposta.dataAtualizacao());
    }
}

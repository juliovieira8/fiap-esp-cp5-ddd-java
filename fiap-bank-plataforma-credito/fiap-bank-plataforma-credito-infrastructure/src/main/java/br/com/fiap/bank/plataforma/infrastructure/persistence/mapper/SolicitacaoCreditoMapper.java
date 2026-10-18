package br.com.fiap.bank.plataforma.infrastructure.persistence.mapper;

import br.com.fiap.bank.plataforma.domain.model.Cpf;
import br.com.fiap.bank.plataforma.domain.model.SolicitacaoCredito;
import br.com.fiap.bank.plataforma.infrastructure.persistence.entity.SolicitacaoCreditoEntity;

/** Conversao entre a entidade de dominio SolicitacaoCredito e sua representacao JPA. */
public final class SolicitacaoCreditoMapper {

    private SolicitacaoCreditoMapper() {
    }

    public static SolicitacaoCredito paraDominio(SolicitacaoCreditoEntity entity) {
        return SolicitacaoCredito.reconstituir(
                entity.getId(),
                Cpf.de(entity.getCpfCliente()),
                entity.getNomeCliente(),
                entity.getTipoCredito(),
                entity.getValorSolicitado(),
                entity.getQuantidadeParcelas(),
                entity.getRendaMensal(),
                entity.getScoreCredito(),
                entity.getStatus(),
                entity.getParecerOperador(),
                entity.getScrRestricoes(),
                entity.getScoreBureau(),
                entity.getDataSolicitacao(),
                entity.getDataAtualizacao());
    }

    /** Copia o estado do dominio para a entidade (nova ou ja gerenciada). O id so e copiado se existir. */
    public static void copiarParaEntidade(SolicitacaoCredito solicitacao, SolicitacaoCreditoEntity entity) {
        if (solicitacao.getId() != null) {
            entity.setId(solicitacao.getId());
        }
        entity.setCpfCliente(solicitacao.getCpf().getValor());
        entity.setNomeCliente(solicitacao.getNomeCliente());
        entity.setTipoCredito(solicitacao.getTipoCredito());
        entity.setValorSolicitado(solicitacao.getValorSolicitado());
        entity.setQuantidadeParcelas(solicitacao.getQuantidadeParcelas());
        entity.setRendaMensal(solicitacao.getRendaMensal());
        entity.setScoreCredito(solicitacao.getScoreCredito());
        entity.setStatus(solicitacao.getStatus());
        entity.setParecerOperador(solicitacao.getParecerOperador());
        entity.setScrRestricoes(solicitacao.getScrRestricoes());
        entity.setScoreBureau(solicitacao.getScoreBureau());
        entity.setDataSolicitacao(solicitacao.getDataSolicitacao());
        entity.setDataAtualizacao(solicitacao.getDataAtualizacao());
    }
}

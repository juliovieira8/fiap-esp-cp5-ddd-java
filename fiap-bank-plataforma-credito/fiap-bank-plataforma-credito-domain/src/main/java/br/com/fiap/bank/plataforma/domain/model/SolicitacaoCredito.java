package br.com.fiap.bank.plataforma.domain.model;

import br.com.fiap.bank.plataforma.domain.exception.DadoInvalidoException;
import br.com.fiap.bank.plataforma.domain.exception.RegraDeNegocioException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Entidade de dominio (raiz de agregado): proposta de credito. Livre de qualquer anotacao de
 * framework ou persistencia; as transicoes de status sao protegidas por comportamentos.
 */
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SolicitacaoCredito {

    private final Long id;
    private final Cpf cpf;
    private final String nomeCliente;
    private final TipoCredito tipoCredito;
    private final BigDecimal valorSolicitado;
    private final Integer quantidadeParcelas;
    private final BigDecimal rendaMensal;
    private Integer scoreCredito;
    private StatusSolicitacao status;
    private String parecerOperador;
    private Boolean scrRestricoes;
    private Integer scoreBureau;
    private final LocalDateTime dataSolicitacao;
    private LocalDateTime dataAtualizacao;

    /** Abre uma nova solicitacao (ainda sem decisao: EM_ANALISE). */
    public static SolicitacaoCredito abrir(Cpf cpf, String nomeCliente, TipoCredito tipoCredito,
                                           BigDecimal valorSolicitado, Integer quantidadeParcelas,
                                           BigDecimal rendaMensal) {
        if (cpf == null) {
            throw new DadoInvalidoException("O CPF do cliente é obrigatório.");
        }
        if (nomeCliente == null || nomeCliente.isBlank()) {
            throw new DadoInvalidoException("O nome do cliente é obrigatório.");
        }
        if (tipoCredito == null) {
            throw new DadoInvalidoException("O tipo de crédito é obrigatório.");
        }
        if (valorSolicitado == null || valorSolicitado.signum() <= 0) {
            throw new DadoInvalidoException("O valor solicitado deve ser maior que zero.");
        }
        if (quantidadeParcelas == null || quantidadeParcelas <= 0) {
            throw new DadoInvalidoException("A quantidade de parcelas deve ser maior que zero.");
        }
        if (rendaMensal == null || rendaMensal.signum() <= 0) {
            throw new DadoInvalidoException("A renda mensal deve ser maior que zero.");
        }
        LocalDateTime agora = LocalDateTime.now();
        return new SolicitacaoCredito(null, cpf, nomeCliente.trim(), tipoCredito, valorSolicitado,
                quantidadeParcelas, rendaMensal, null, StatusSolicitacao.EM_ANALISE, null,
                null, null, agora, agora);
    }

    /** Reidrata a entidade a partir da persistencia. */
    public static SolicitacaoCredito reconstituir(Long id, Cpf cpf, String nomeCliente, TipoCredito tipoCredito,
                                                  BigDecimal valorSolicitado, Integer quantidadeParcelas,
                                                  BigDecimal rendaMensal, Integer scoreCredito,
                                                  StatusSolicitacao status, String parecerOperador,
                                                  Boolean scrRestricoes, Integer scoreBureau,
                                                  LocalDateTime dataSolicitacao, LocalDateTime dataAtualizacao) {
        return new SolicitacaoCredito(id, cpf, nomeCliente, tipoCredito, valorSolicitado, quantidadeParcelas,
                rendaMensal, scoreCredito, status, parecerOperador, scrRestricoes, scoreBureau,
                dataSolicitacao, dataAtualizacao);
    }

    /** Parcela estimada: valor solicitado dividido pela quantidade de parcelas. */
    public BigDecimal parcelaEstimada() {
        return valorSolicitado.divide(BigDecimal.valueOf(quantidadeParcelas), 2, RoundingMode.HALF_UP);
    }

    /** Registra o resultado das consultas externas e a decisao automatica do motor. */
    public void registrarAnalise(boolean restricaoScr, int scoreBureau, DecisaoCredito decisao) {
        this.scrRestricoes = restricaoScr;
        this.scoreBureau = scoreBureau;
        this.scoreCredito = scoreBureau;
        this.status = decisao.status();
        this.parecerOperador = decisao.parecer();
        this.dataAtualizacao = LocalDateTime.now();
    }

    /** Aprovacao manual pela Mesa de Operacoes (idempotente quando ja aprovada). */
    public void aprovar(String parecer) {
        if (status == StatusSolicitacao.APROVADO) {
            return;
        }
        exigirAguardandoDecisao("aprovar");
        this.status = StatusSolicitacao.APROVADO;
        this.parecerOperador = "Aprovado pela Mesa de Operações: " + (parecer == null || parecer.isBlank() ? "OK" : parecer.trim());
        this.dataAtualizacao = LocalDateTime.now();
    }

    /** Reprovacao manual pela Mesa de Operacoes (idempotente quando ja reprovada). */
    public void reprovar(String motivo) {
        if (status == StatusSolicitacao.REPROVADO) {
            return;
        }
        exigirAguardandoDecisao("reprovar");
        this.status = StatusSolicitacao.REPROVADO;
        this.parecerOperador = "Reprovado pela Mesa de Operações. Motivo: "
                + (motivo == null || motivo.isBlank() ? "Inconformidade nos critérios de risco" : motivo.trim());
        this.dataAtualizacao = LocalDateTime.now();
    }

    private void exigirAguardandoDecisao(String acao) {
        if (status != StatusSolicitacao.EM_ANALISE && status != StatusSolicitacao.DOCUMENTACAO_PENDENTE) {
            throw new RegraDeNegocioException("Não é possível " + acao + " a solicitação #" + id
                    + ": ela já está " + status + ".");
        }
    }
}

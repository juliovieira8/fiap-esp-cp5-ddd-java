package br.com.fiap.bank.plataforma.domain.model;

import br.com.fiap.bank.plataforma.domain.exception.DadoInvalidoException;
import br.com.fiap.bank.plataforma.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SolicitacaoCreditoTest {

    private SolicitacaoCredito nova() {
        return SolicitacaoCredito.abrir(Cpf.de("12345678900"), "Ana Silva", TipoCredito.EMPRESTIMO_PESSOAL,
                new BigDecimal("15000.00"), 24, new BigDecimal("7500.00"));
    }

    @Test
    void novaSolicitacaoDeveNascerEmAnalise() {
        assertThat(nova().getStatus()).isEqualTo(StatusSolicitacao.EM_ANALISE);
    }

    @Test
    void parcelaEstimadaDeveDividirOValorPelasParcelas() {
        assertThat(nova().parcelaEstimada()).isEqualByComparingTo("625.00");
    }

    @Test
    void aprovacaoManualDeveAtualizarStatusEParecer() {
        SolicitacaoCredito solicitacao = nova();

        solicitacao.aprovar("Cliente com bom histórico");

        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.APROVADO);
        assertThat(solicitacao.getParecerOperador()).contains("Cliente com bom histórico");
    }

    @Test
    void aprovarDuasVezesDeveSerIdempotente() {
        SolicitacaoCredito solicitacao = nova();

        solicitacao.aprovar("ok");
        solicitacao.aprovar("outro parecer");

        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.APROVADO);
        assertThat(solicitacao.getParecerOperador()).contains("ok");
    }

    @Test
    void naoDeveAprovarSolicitacaoJaReprovada() {
        SolicitacaoCredito solicitacao = nova();
        solicitacao.reprovar("Risco elevado");

        assertThatThrownBy(() -> solicitacao.aprovar("tentativa"))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.REPROVADO);
    }

    @Test
    void reprovacaoSemMotivoDeveUsarMotivoPadrao() {
        SolicitacaoCredito solicitacao = nova();

        solicitacao.reprovar(null);

        assertThat(solicitacao.getParecerOperador()).contains("Inconformidade nos critérios de risco");
    }

    @Test
    void deveRejeitarValoresInvalidos() {
        assertThatThrownBy(() -> SolicitacaoCredito.abrir(Cpf.de("12345678900"), "Ana", TipoCredito.CONSIGNADO,
                BigDecimal.ZERO, 12, BigDecimal.TEN)).isInstanceOf(DadoInvalidoException.class);
        assertThatThrownBy(() -> SolicitacaoCredito.abrir(Cpf.de("12345678900"), "Ana", TipoCredito.CONSIGNADO,
                BigDecimal.TEN, 0, BigDecimal.TEN)).isInstanceOf(DadoInvalidoException.class);
    }
}

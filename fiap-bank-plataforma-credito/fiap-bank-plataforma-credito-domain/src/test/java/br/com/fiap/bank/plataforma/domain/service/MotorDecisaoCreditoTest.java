package br.com.fiap.bank.plataforma.domain.service;

import br.com.fiap.bank.plataforma.domain.model.AnaliseExterna;
import br.com.fiap.bank.plataforma.domain.model.Cpf;
import br.com.fiap.bank.plataforma.domain.model.DecisaoCredito;
import br.com.fiap.bank.plataforma.domain.model.SituacaoCore;
import br.com.fiap.bank.plataforma.domain.model.SolicitacaoCredito;
import br.com.fiap.bank.plataforma.domain.model.StatusSolicitacao;
import br.com.fiap.bank.plataforma.domain.model.TipoCredito;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MotorDecisaoCreditoTest {

    private final MotorDecisaoCredito motor = new MotorDecisaoCredito();

    private SolicitacaoCredito solicitacao(String valor, int parcelas, String renda) {
        return SolicitacaoCredito.abrir(Cpf.de("12345678900"), "Ana Silva", TipoCredito.EMPRESTIMO_PESSOAL,
                new BigDecimal(valor), parcelas, new BigDecimal(renda));
    }

    private AnaliseExterna limpa() {
        return new AnaliseExterna(false, 700, false, SituacaoCore.ATIVA);
    }

    @Test
    void restricaoNoScrDeveReprovar() {
        DecisaoCredito decisao = motor.avaliar(solicitacao("15000.00", 24, "7500.00"),
                new AnaliseExterna(true, 800, false, SituacaoCore.ATIVA));

        assertThat(decisao.status()).isEqualTo(StatusSolicitacao.REPROVADO);
        assertThat(decisao.parecer()).contains("SCR");
    }

    @Test
    void scoreAbaixoDe500DeveReprovar() {
        DecisaoCredito decisao = motor.avaliar(solicitacao("15000.00", 24, "7500.00"),
                new AnaliseExterna(false, 499, false, SituacaoCore.ATIVA));

        assertThat(decisao.status()).isEqualTo(StatusSolicitacao.REPROVADO);
    }

    @Test
    void clienteSemContaAtivaNoCoreDeveSerReprovado() {
        DecisaoCredito decisao = motor.avaliar(solicitacao("15000.00", 24, "7500.00"),
                new AnaliseExterna(false, 700, false, SituacaoCore.INEXISTENTE));

        assertThat(decisao.status()).isEqualTo(StatusSolicitacao.REPROVADO);
    }

    @Test
    void coreIndisponivelDeveEncaminharParaAMesa() {
        DecisaoCredito decisao = motor.avaliar(solicitacao("15000.00", 24, "7500.00"),
                new AnaliseExterna(false, 700, false, SituacaoCore.INDISPONIVEL));

        assertThat(decisao.status()).isEqualTo(StatusSolicitacao.EM_ANALISE);
    }

    @Test
    void parcelaAte30PorCentoDaRendaDeveAprovarAutomaticamente() {
        // parcela = 625,00 e limite = 30% de 7.500,00 = 2.250,00
        DecisaoCredito decisao = motor.avaliar(solicitacao("15000.00", 24, "7500.00"), limpa());

        assertThat(decisao.status()).isEqualTo(StatusSolicitacao.APROVADO);
    }

    @Test
    void parcelaAcimaDe30PorCentoDaRendaDeveIrParaAMesa() {
        // parcela = 1.000,00 e limite = 30% de 2.000,00 = 600,00
        DecisaoCredito decisao = motor.avaliar(solicitacao("12000.00", 12, "2000.00"), limpa());

        assertThat(decisao.status()).isEqualTo(StatusSolicitacao.EM_ANALISE);
        assertThat(decisao.parecer()).contains("30%");
    }
}

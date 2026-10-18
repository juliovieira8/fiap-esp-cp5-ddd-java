package br.com.fiap.bank.core.domain.model;

import br.com.fiap.bank.core.domain.exception.DadoInvalidoException;
import br.com.fiap.bank.core.domain.exception.RegraDeNegocioException;
import br.com.fiap.bank.core.domain.exception.SaldoInsuficienteException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContaBancariaTest {

    private ContaBancaria novaConta(String saldoInicial) {
        return ContaBancaria.abrir(UUID.randomUUID(), "12345-6", "0001", TipoConta.CORRENTE,
                new BigDecimal(saldoInicial));
    }

    @Test
    void contaAbertaDeveEstarAtivaComSaldoInicial() {
        ContaBancaria conta = novaConta("250.00");

        assertThat(conta.getStatus()).isEqualTo(StatusConta.ATIVA);
        assertThat(conta.getSaldo().getValor()).isEqualByComparingTo("250.00");
    }

    @Test
    void depositoESaqueDevemAtualizarOSaldo() {
        ContaBancaria conta = novaConta("100.00");

        conta.depositar(new BigDecimal("50.00"));
        conta.sacar(new BigDecimal("30.00"));

        assertThat(conta.getSaldo().getValor()).isEqualByComparingTo("120.00");
    }

    @Test
    void saqueMaiorQueOSaldoDeveFalharSemAlterarOSaldo() {
        ContaBancaria conta = novaConta("100.00");

        assertThatThrownBy(() -> conta.sacar(new BigDecimal("100.01")))
                .isInstanceOf(SaldoInsuficienteException.class);
        assertThat(conta.getSaldo().getValor()).isEqualByComparingTo("100.00");
    }

    @Test
    void contaEncerradaNaoPodeSerMovimentada() {
        ContaBancaria conta = novaConta("100.00");
        conta.encerrar();

        assertThatThrownBy(() -> conta.depositar(new BigDecimal("1.00")))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void encerrarDeveSerIdempotente() {
        ContaBancaria conta = novaConta("0.00");

        conta.encerrar();
        conta.encerrar();

        assertThat(conta.getStatus()).isEqualTo(StatusConta.ENCERRADA);
    }

    @Test
    void agenciaDeveTerQuatroDigitos() {
        assertThatThrownBy(() -> ContaBancaria.abrir(UUID.randomUUID(), "12345-6", "01", TipoConta.POUPANCA,
                BigDecimal.ZERO)).isInstanceOf(DadoInvalidoException.class);
    }
}

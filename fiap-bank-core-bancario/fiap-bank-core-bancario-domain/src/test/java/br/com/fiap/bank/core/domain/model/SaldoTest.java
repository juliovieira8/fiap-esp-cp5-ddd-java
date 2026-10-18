package br.com.fiap.bank.core.domain.model;

import br.com.fiap.bank.core.domain.exception.DadoInvalidoException;
import br.com.fiap.bank.core.domain.exception.SaldoInsuficienteException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SaldoTest {

    @Test
    void adicionarDeveDevolverNovoSaldoComDuasCasasDecimais() {
        Saldo saldo = Saldo.de(new BigDecimal("10")).adicionar(new BigDecimal("5.5"));

        assertThat(saldo).isEqualTo(Saldo.de(new BigDecimal("15.50")));
        assertThat(saldo.getValor()).isEqualByComparingTo("15.50");
    }

    @Test
    void subtrairDeveReduzirOSaldo() {
        Saldo saldo = Saldo.de(new BigDecimal("100.00")).subtrair(new BigDecimal("40.25"));

        assertThat(saldo.getValor()).isEqualByComparingTo("59.75");
    }

    @Test
    void subtrairMaisQueODisponivelDeveFalhar() {
        Saldo saldo = Saldo.de(new BigDecimal("10.00"));

        assertThatThrownBy(() -> saldo.subtrair(new BigDecimal("10.01")))
                .isInstanceOf(SaldoInsuficienteException.class);
    }

    @Test
    void saldoInicialNegativoDeveSerRejeitado() {
        assertThatThrownBy(() -> Saldo.de(new BigDecimal("-0.01")))
                .isInstanceOf(DadoInvalidoException.class);
    }

    @Test
    void movimentacaoDeveSerMaiorQueZero() {
        Saldo saldo = Saldo.zero();

        assertThatThrownBy(() -> saldo.adicionar(BigDecimal.ZERO))
                .isInstanceOf(DadoInvalidoException.class);
    }
}

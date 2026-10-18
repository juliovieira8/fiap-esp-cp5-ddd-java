package br.com.fiap.bank.core.domain.model;

import br.com.fiap.bank.core.domain.exception.DadoInvalidoException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfTest {

    @Test
    void deveAceitarOnzeDigitosLimpos() {
        assertThat(Cpf.de("12345678900").getValor()).isEqualTo("12345678900");
    }

    @Test
    void deveSanitizarOFormatoComMascara() {
        Cpf cpf = Cpf.de("123.456.789-00");

        assertThat(cpf.getValor()).isEqualTo("12345678900");
        assertThat(cpf.formatado()).isEqualTo("123.456.789-00");
        assertThat(cpf).isEqualTo(Cpf.de("12345678900"));
    }

    @Test
    void deveRejeitarFormatosInvalidos() {
        assertThatThrownBy(() -> Cpf.de("1234567890")).isInstanceOf(DadoInvalidoException.class);
        assertThatThrownBy(() -> Cpf.de("123.456.789/00")).isInstanceOf(DadoInvalidoException.class);
        assertThatThrownBy(() -> Cpf.de(null)).isInstanceOf(DadoInvalidoException.class);
    }
}

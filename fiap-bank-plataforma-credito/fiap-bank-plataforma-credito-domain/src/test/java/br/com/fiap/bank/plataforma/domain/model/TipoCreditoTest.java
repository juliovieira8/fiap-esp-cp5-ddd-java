package br.com.fiap.bank.plataforma.domain.model;

import br.com.fiap.bank.plataforma.domain.exception.DadoInvalidoException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TipoCreditoTest {

    @Test
    void deveAceitarOIdentificadorCanonico() {
        assertThat(TipoCredito.fromTexto("EMPRESTIMO_PESSOAL")).isEqualTo(TipoCredito.EMPRESTIMO_PESSOAL);
        assertThat(TipoCredito.fromTexto("consignado")).isEqualTo(TipoCredito.CONSIGNADO);
    }

    @Test
    void deveAceitarOsRotulosHistoricosDosFrontEndsLegados() {
        assertThat(TipoCredito.fromTexto("EMPRÉSTIMO PESSOAL")).isEqualTo(TipoCredito.EMPRESTIMO_PESSOAL);
        assertThat(TipoCredito.fromTexto("CRÉDITO IMOBILIÁRIO")).isEqualTo(TipoCredito.CREDITO_IMOBILIARIO);
        assertThat(TipoCredito.fromTexto("CRÉDITO AUTOMOTIVO")).isEqualTo(TipoCredito.CREDITO_AUTOMOTIVO);
    }

    @Test
    void deveExporORotuloHistoricoComoDescricao() {
        assertThat(TipoCredito.EMPRESTIMO_PESSOAL.getDescricao()).isEqualTo("EMPRÉSTIMO PESSOAL");
    }

    @Test
    void deveRejeitarTipoDesconhecido() {
        assertThatThrownBy(() -> TipoCredito.fromTexto("CARTAO")).isInstanceOf(DadoInvalidoException.class);
        assertThatThrownBy(() -> TipoCredito.fromTexto(" ")).isInstanceOf(DadoInvalidoException.class);
    }
}

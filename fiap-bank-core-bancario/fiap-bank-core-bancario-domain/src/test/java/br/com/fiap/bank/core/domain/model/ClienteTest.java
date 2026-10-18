package br.com.fiap.bank.core.domain.model;

import br.com.fiap.bank.core.domain.exception.DadoInvalidoException;
import br.com.fiap.bank.core.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClienteTest {

    private Cliente novoCliente() {
        return Cliente.criar(Cpf.de("12345678900"), "Ana Silva", Email.de("Ana.Silva@Email.com"),
                LocalDate.of(1992, 4, 15), new BigDecimal("7500.00"));
    }

    @Test
    void deveCriarClienteNormalizandoEmail() {
        Cliente cliente = novoCliente();

        assertThat(cliente.getId()).isNotNull();
        assertThat(cliente.getEmail().getValor()).isEqualTo("ana.silva@email.com");
        assertThat(cliente.possuiConta()).isFalse();
    }

    @Test
    void dataDeNascimentoNoFuturoDeveSerRejeitada() {
        assertThatThrownBy(() -> Cliente.criar(Cpf.de("12345678900"), "Ana Silva", Email.de("ana@email.com"),
                LocalDate.now().plusDays(1), BigDecimal.TEN)).isInstanceOf(DadoInvalidoException.class);
    }

    @Test
    void clienteSoPodeTerUmaConta() {
        Cliente cliente = novoCliente();
        cliente.abrirConta("12345-6", "0001", TipoConta.CORRENTE, new BigDecimal("100.00"));

        assertThat(cliente.possuiConta()).isTrue();
        assertThatThrownBy(() -> cliente.abrirConta("99999-9", "0001", TipoConta.POUPANCA, BigDecimal.ZERO))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void atualizarDadosNaoDeveAlterarCpfNemId() {
        Cliente cliente = novoCliente();
        var idOriginal = cliente.getId();

        cliente.atualizarDados("Ana Maria Silva", Email.de("ana.maria@email.com"),
                LocalDate.of(1992, 4, 15), new BigDecimal("8000.00"));

        assertThat(cliente.getNome()).isEqualTo("Ana Maria Silva");
        assertThat(cliente.getCpf().getValor()).isEqualTo("12345678900");
        assertThat(cliente.getId()).isEqualTo(idOriginal);
    }
}

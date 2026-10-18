package br.com.fiap.bank.core.application.service;

import br.com.fiap.bank.core.application.dto.AtualizarSaldoRequest;
import br.com.fiap.bank.core.application.dto.ContaResponse;
import br.com.fiap.bank.core.application.dto.CriarContaRequest;
import br.com.fiap.bank.core.application.exception.ConflitoException;
import br.com.fiap.bank.core.application.exception.DadosInvalidosException;
import br.com.fiap.bank.core.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.bank.core.domain.model.Cliente;
import br.com.fiap.bank.core.domain.model.ContaBancaria;
import br.com.fiap.bank.core.domain.model.Cpf;
import br.com.fiap.bank.core.domain.model.Email;
import br.com.fiap.bank.core.domain.model.StatusConta;
import br.com.fiap.bank.core.domain.model.TipoConta;
import br.com.fiap.bank.core.domain.repository.ClienteRepository;
import br.com.fiap.bank.core.domain.repository.ContaBancariaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ContaServiceTest {

    private ClienteRepository clienteRepository;
    private ContaBancariaRepository contaRepository;
    private ContaService service;

    private Cliente cliente;
    private ContaBancaria conta;

    @BeforeEach
    void preparar() {
        clienteRepository = mock(ClienteRepository.class);
        contaRepository = mock(ContaBancariaRepository.class);
        service = new ContaService(clienteRepository, contaRepository, new GeradorNumeroConta());

        cliente = Cliente.criar(Cpf.de("12345678900"), "Ana Silva", Email.de("ana@email.com"),
                LocalDate.of(1992, 4, 15), new BigDecimal("7500.00"));
        conta = ContaBancaria.abrir(cliente.getId(), "12345-6", "0001", TipoConta.CORRENTE,
                new BigDecimal("100.00"));

        when(contaRepository.buscarPorNumero("12345-6")).thenReturn(Optional.of(conta));
        when(clienteRepository.buscarPorId(cliente.getId())).thenReturn(Optional.of(cliente));
        when(contaRepository.salvar(any(ContaBancaria.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    @Test
    void depositoDeveAumentarOSaldo() {
        ContaResponse resposta = service.atualizarSaldo("12345-6",
                new AtualizarSaldoRequest("deposito", new BigDecimal("50.00")));

        assertThat(resposta.saldo()).isEqualByComparingTo("150.00");
        assertThat(resposta.cpfTitular()).isEqualTo("12345678900");
    }

    @Test
    void saqueAcimaDoSaldoDeveVirarConflito() {
        assertThatThrownBy(() -> service.atualizarSaldo("12345-6",
                new AtualizarSaldoRequest("SAQUE", new BigDecimal("500.00"))))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void contaInexistenteDeveVirarRecursoNaoEncontrado() {
        when(contaRepository.buscarPorNumero("99999-9")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorNumero("99999-9"))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void numeroDeContaMalFormadoDeveVirarDadosInvalidos() {
        assertThatThrownBy(() -> service.buscarPorNumero("abc"))
                .isInstanceOf(DadosInvalidosException.class);
    }

    @Test
    void encerrarDeveInativarLogicamenteAConta() {
        service.encerrar("12345-6");

        assertThat(conta.getStatus()).isEqualTo(StatusConta.ENCERRADA);
    }

    @Test
    void criarContaParaClienteInexistenteDeveVirarDadosInvalidos() {
        when(clienteRepository.buscarPorCpf(Cpf.de("11122233344"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(
                new CriarContaRequest("11122233344", null, TipoConta.POUPANCA, BigDecimal.ZERO)))
                .isInstanceOf(DadosInvalidosException.class);
    }

    @Test
    void clienteComContaNaoPodeAbrirOutraConta() {
        Cliente comConta = Cliente.criar(Cpf.de("98765432100"), "Carlos Oliveira", Email.de("carlos@email.com"),
                LocalDate.of(1985, 9, 30), new BigDecimal("12000.00"));
        comConta.abrirConta("78901-2", "0001", TipoConta.CORRENTE, BigDecimal.TEN);
        when(clienteRepository.buscarPorCpf(Cpf.de("98765432100"))).thenReturn(Optional.of(comConta));
        when(contaRepository.existePorNumero(any(String.class))).thenReturn(false);

        assertThatThrownBy(() -> service.criar(
                new CriarContaRequest("98765432100", "0001", TipoConta.CORRENTE, BigDecimal.TEN)))
                .isInstanceOf(ConflitoException.class);
    }
}

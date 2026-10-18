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
import br.com.fiap.bank.core.domain.repository.ClienteRepository;
import br.com.fiap.bank.core.domain.repository.ContaBancariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static br.com.fiap.bank.core.application.exception.TradutorDeExcecoes.executar;
import static br.com.fiap.bank.core.application.exception.TradutorDeExcecoes.executarSemRetorno;

/** Casos de uso do recurso Conta Bancaria (e do sub-recurso saldo). */
@Service
@RequiredArgsConstructor
public class ContaService {

    private static final String AGENCIA_PADRAO = "0001";
    private static final int MAXIMO_TENTATIVAS_NUMERO = 20;
    private static final Pattern FORMATO_NUMERO_CONTA = Pattern.compile("^\\d{4,12}-\\d$");

    private final ClienteRepository clienteRepository;
    private final ContaBancariaRepository contaRepository;
    private final GeradorNumeroConta geradorNumeroConta;

    @Transactional(readOnly = true)
    public List<ContaResponse> listar() {
        Map<UUID, Cliente> titulares = clienteRepository.listarTodos().stream()
                .collect(Collectors.toMap(Cliente::getId, Function.identity()));
        return contaRepository.listarTodas().stream()
                .map(conta -> ContaResponse.de(conta, titulares.get(conta.getClienteId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ContaResponse buscarPorNumero(String numeroConta) {
        return responder(obterConta(numeroConta));
    }

    @Transactional(readOnly = true)
    public ContaResponse buscarPorCpf(String cpf) {
        Cpf cpfValido = executar(() -> Cpf.de(cpf));
        Cliente titular = clienteRepository.buscarPorCpf(cpfValido)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado para o CPF informado."));
        if (!titular.possuiConta()) {
            throw new RecursoNaoEncontradoException("Nenhuma conta bancária vinculada ao CPF informado.");
        }
        return ContaResponse.de(titular.getConta(), titular);
    }

    @Transactional
    public ContaResponse criar(CriarContaRequest request) {
        Cpf cpf = executar(() -> Cpf.de(request.cpfCliente()));
        Cliente titular = clienteRepository.buscarPorCpf(cpf)
                .orElseThrow(() -> new DadosInvalidosException(
                        "Cliente titular não encontrado para o CPF informado. Cadastre o cliente antes de abrir a conta."));

        String agencia = request.agencia() == null || request.agencia().isBlank()
                ? AGENCIA_PADRAO : request.agencia();
        String numero = gerarNumeroDisponivel();

        ContaBancaria conta = executar(() ->
                titular.abrirConta(numero, agencia, request.tipoConta(), request.saldoInicial()));
        return ContaResponse.de(contaRepository.salvar(conta), titular);
    }

    @Transactional
    public ContaResponse atualizarSaldo(String numeroConta, AtualizarSaldoRequest request) {
        ContaBancaria conta = obterConta(numeroConta);
        String operacao = request.operacao().trim().toUpperCase(Locale.ROOT);
        executarSemRetorno(() -> {
            switch (operacao) {
                case "DEPOSITO" -> conta.depositar(request.valor());
                case "SAQUE" -> conta.sacar(request.valor());
                default -> throw new DadosInvalidosException("Operação inválida: use DEPOSITO ou SAQUE.");
            }
        });
        return responder(contaRepository.salvar(conta));
    }

    /** Encerramento logico (idempotente): a conta permanece registrada com status ENCERRADA. */
    @Transactional
    public void encerrar(String numeroConta) {
        ContaBancaria conta = obterConta(numeroConta);
        conta.encerrar();
        contaRepository.salvar(conta);
    }

    private ContaBancaria obterConta(String numeroConta) {
        if (numeroConta == null || !FORMATO_NUMERO_CONTA.matcher(numeroConta).matches()) {
            throw new DadosInvalidosException("Número de conta inválido: use o formato NNNNN-D.");
        }
        return contaRepository.buscarPorNumero(numeroConta)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada: " + numeroConta));
    }

    private ContaResponse responder(ContaBancaria conta) {
        Cliente titular = clienteRepository.buscarPorId(conta.getClienteId()).orElse(null);
        return ContaResponse.de(conta, titular);
    }

    private String gerarNumeroDisponivel() {
        for (int tentativa = 0; tentativa < MAXIMO_TENTATIVAS_NUMERO; tentativa++) {
            String numero = geradorNumeroConta.gerar();
            if (!contaRepository.existePorNumero(numero)) {
                return numero;
            }
        }
        throw new ConflitoException("Não foi possível gerar um número de conta disponível. Tente novamente.");
    }
}

package br.com.fiap.bank.core.infrastructure.config;

import br.com.fiap.bank.core.domain.model.Cliente;
import br.com.fiap.bank.core.domain.model.ContaBancaria;
import br.com.fiap.bank.core.domain.model.Cpf;
import br.com.fiap.bank.core.domain.model.Email;
import br.com.fiap.bank.core.domain.model.TipoConta;
import br.com.fiap.bank.core.domain.repository.ClienteRepository;
import br.com.fiap.bank.core.domain.repository.ContaBancariaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Carga inicial (banco H2 em memoria) com os correntistas usados no ecossistema para testes
 * ponta a ponta: Plataforma de Credito, Mesa de Operacoes e Internet Banking Mobile.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DadosIniciaisLoader implements CommandLineRunner {

    private final ClienteRepository clienteRepository;
    private final ContaBancariaRepository contaRepository;

    @Override
    public void run(String... args) {
        if (!clienteRepository.listarTodos().isEmpty()) {
            return;
        }
        semear("12345678900", "Ana Silva", "ana.silva@email.com", LocalDate.of(1992, 4, 15),
                "7500.00", "12345-6", "12500.00");
        semear("98765432100", "Carlos Eduardo Oliveira", "carlos.oliveira@email.com", LocalDate.of(1985, 9, 30),
                "12000.00", "78901-2", "45800.50");
        semear("55544433399", "Mariana Souza", "mariana.souza@email.com", LocalDate.of(1990, 1, 20),
                "4200.00", "34567-8", "3200.00");
        log.info("Core Bancário: dados iniciais carregados com sucesso.");
    }

    private void semear(String cpf, String nome, String email, LocalDate nascimento, String renda,
                        String numeroConta, String saldoInicial) {
        Cliente cliente = clienteRepository.salvar(
                Cliente.criar(Cpf.de(cpf), nome, Email.de(email), nascimento, new BigDecimal(renda)));
        ContaBancaria conta = cliente.abrirConta(numeroConta, "0001", TipoConta.CORRENTE, new BigDecimal(saldoInicial));
        contaRepository.salvar(conta);
    }
}

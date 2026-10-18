package br.com.fiap.bank.core.domain.model;

import br.com.fiap.bank.core.domain.exception.DadoInvalidoException;
import br.com.fiap.bank.core.domain.exception.RegraDeNegocioException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidade de dominio (raiz de agregado): correntista do FIAP Bank. O CPF e a identidade de
 * negocio e nunca muda; os demais dados cadastrais so mudam por meio de comportamentos.
 */
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Cliente {

    @EqualsAndHashCode.Include
    private final UUID id;
    private String nome;
    private Email email;
    private LocalDate dataNascimento;
    private final Cpf cpf;
    private BigDecimal rendaMensal;
    private ContaBancaria conta;

    public static Cliente criar(Cpf cpf, String nome, Email email, LocalDate dataNascimento,
                                BigDecimal rendaMensal) {
        if (cpf == null) {
            throw new DadoInvalidoException("O CPF é obrigatório.");
        }
        validarDados(nome, email, dataNascimento, rendaMensal);
        return new Cliente(UUID.randomUUID(), nome.trim(), email, dataNascimento, cpf, rendaMensal, null);
    }

    /** Reidrata a entidade a partir da persistencia. */
    public static Cliente reconstituir(UUID id, String nome, Email email, LocalDate dataNascimento,
                                       Cpf cpf, BigDecimal rendaMensal, ContaBancaria conta) {
        return new Cliente(id, nome, email, dataNascimento, cpf, rendaMensal, conta);
    }

    /** Substituicao integral dos dados cadastrais (o CPF permanece imutavel). */
    public void atualizarDados(String nome, Email email, LocalDate dataNascimento, BigDecimal rendaMensal) {
        validarDados(nome, email, dataNascimento, rendaMensal);
        this.nome = nome.trim();
        this.email = email;
        this.dataNascimento = dataNascimento;
        this.rendaMensal = rendaMensal;
    }

    /** Abre a conta do cliente. Cada cliente possui no maximo uma conta vinculada. */
    public ContaBancaria abrirConta(String numero, String agencia, TipoConta tipo, BigDecimal saldoInicial) {
        if (conta != null) {
            throw new RegraDeNegocioException(
                    "O cliente já possui uma conta bancária vinculada (" + conta.getNumero() + ").");
        }
        this.conta = ContaBancaria.abrir(id, numero, agencia, tipo, saldoInicial);
        return this.conta;
    }

    public boolean possuiConta() {
        return conta != null;
    }

    private static void validarDados(String nome, Email email, LocalDate dataNascimento, BigDecimal rendaMensal) {
        if (nome == null || nome.isBlank()) {
            throw new DadoInvalidoException("O nome do cliente é obrigatório.");
        }
        if (email == null) {
            throw new DadoInvalidoException("O e-mail do cliente é obrigatório.");
        }
        if (dataNascimento == null || !dataNascimento.isBefore(LocalDate.now())) {
            throw new DadoInvalidoException("A data de nascimento deve estar no passado.");
        }
        if (rendaMensal == null || rendaMensal.signum() < 0) {
            throw new DadoInvalidoException("A renda mensal não pode ser negativa.");
        }
    }
}

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
 * Entidade de dominio: conta bancaria. Referencia o titular apenas pelo identificador
 * (referencia entre agregados por id) e protege suas invariantes por meio de comportamentos.
 */
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContaBancaria {

    @EqualsAndHashCode.Include
    private final UUID id;
    private final UUID clienteId;
    private final String numero;
    private final String agencia;
    private final TipoConta tipo;
    private StatusConta status;
    private Saldo saldo;
    private final LocalDate dataAbertura;

    public static ContaBancaria abrir(UUID clienteId, String numero, String agencia,
                                      TipoConta tipo, BigDecimal saldoInicial) {
        if (clienteId == null) {
            throw new DadoInvalidoException("A conta precisa estar vinculada a um titular.");
        }
        if (numero == null || numero.isBlank()) {
            throw new DadoInvalidoException("O número da conta é obrigatório.");
        }
        if (agencia == null || !agencia.matches("\\d{4}")) {
            throw new DadoInvalidoException("A agência deve possuir exatamente 4 dígitos numéricos.");
        }
        if (tipo == null) {
            throw new DadoInvalidoException("O tipo da conta é obrigatório.");
        }
        return new ContaBancaria(UUID.randomUUID(), clienteId, numero, agencia, tipo,
                StatusConta.ATIVA, Saldo.de(saldoInicial), LocalDate.now());
    }

    /** Reidrata a entidade a partir da persistencia (sem reaplicar regras de abertura). */
    public static ContaBancaria reconstituir(UUID id, UUID clienteId, String numero, String agencia,
                                             TipoConta tipo, StatusConta status, Saldo saldo,
                                             LocalDate dataAbertura) {
        return new ContaBancaria(id, clienteId, numero, agencia, tipo, status, saldo, dataAbertura);
    }

    public void depositar(BigDecimal valor) {
        exigirAtiva();
        this.saldo = saldo.adicionar(valor);
    }

    public void sacar(BigDecimal valor) {
        exigirAtiva();
        this.saldo = saldo.subtrair(valor);
    }

    public void bloquear() {
        if (status == StatusConta.ENCERRADA) {
            throw new RegraDeNegocioException("A conta " + numero + " já está encerrada e não pode ser bloqueada.");
        }
        this.status = StatusConta.BLOQUEADA;
    }

    /** Encerramento logico e idempotente: encerrar uma conta ja encerrada nao altera nada. */
    public void encerrar() {
        this.status = StatusConta.ENCERRADA;
    }

    public boolean estaAtiva() {
        return status == StatusConta.ATIVA;
    }

    private void exigirAtiva() {
        if (!estaAtiva()) {
            throw new RegraDeNegocioException(
                    "Operação não permitida: a conta " + numero + " está " + status + ".");
        }
    }
}

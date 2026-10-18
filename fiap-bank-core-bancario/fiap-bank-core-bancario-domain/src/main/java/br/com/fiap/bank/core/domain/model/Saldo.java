package br.com.fiap.bank.core.domain.model;

import br.com.fiap.bank.core.domain.exception.DadoInvalidoException;
import br.com.fiap.bank.core.domain.exception.SaldoInsuficienteException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object imutavel que representa o saldo de uma conta (sempre com 2 casas decimais e
 * nunca negativo). Toda movimentacao devolve um novo Saldo.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Saldo {

    private static final int ESCALA = 2;

    BigDecimal valor;

    public static Saldo zero() {
        return new Saldo(BigDecimal.ZERO.setScale(ESCALA, RoundingMode.HALF_EVEN));
    }

    public static Saldo de(BigDecimal valor) {
        if (valor == null) {
            throw new DadoInvalidoException("O saldo não pode ser nulo.");
        }
        if (valor.signum() < 0) {
            throw new DadoInvalidoException("O saldo não pode ser negativo.");
        }
        return new Saldo(valor.setScale(ESCALA, RoundingMode.HALF_EVEN));
    }

    public Saldo adicionar(BigDecimal quantia) {
        validarQuantia(quantia);
        return new Saldo(valor.add(quantia).setScale(ESCALA, RoundingMode.HALF_EVEN));
    }

    public Saldo subtrair(BigDecimal quantia) {
        validarQuantia(quantia);
        BigDecimal resultado = valor.subtract(quantia);
        if (resultado.signum() < 0) {
            throw new SaldoInsuficienteException(
                    "Saldo insuficiente: disponível R$ " + valor.toPlainString()
                            + ", solicitado R$ " + quantia.setScale(ESCALA, RoundingMode.HALF_EVEN).toPlainString() + ".");
        }
        return new Saldo(resultado.setScale(ESCALA, RoundingMode.HALF_EVEN));
    }

    private static void validarQuantia(BigDecimal quantia) {
        if (quantia == null || quantia.signum() <= 0) {
            throw new DadoInvalidoException("O valor da movimentação deve ser maior que zero.");
        }
    }

    @Override
    public String toString() {
        return valor.toPlainString();
    }
}

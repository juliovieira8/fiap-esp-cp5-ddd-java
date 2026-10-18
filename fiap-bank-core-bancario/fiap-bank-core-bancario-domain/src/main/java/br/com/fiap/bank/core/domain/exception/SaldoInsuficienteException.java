package br.com.fiap.bank.core.domain.exception;

/** Debito superior ao saldo disponivel. */
public class SaldoInsuficienteException extends RegraDeNegocioException {

    public SaldoInsuficienteException(String mensagem) {
        super(mensagem);
    }
}

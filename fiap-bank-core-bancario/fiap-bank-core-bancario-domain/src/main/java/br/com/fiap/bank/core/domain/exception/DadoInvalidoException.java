package br.com.fiap.bank.core.domain.exception;

/** Valor que viola uma invariante de formato/dominio (CPF, e-mail, saldo, valores monetarios...). */
public class DadoInvalidoException extends DomainException {

    public DadoInvalidoException(String mensagem) {
        super(mensagem);
    }
}

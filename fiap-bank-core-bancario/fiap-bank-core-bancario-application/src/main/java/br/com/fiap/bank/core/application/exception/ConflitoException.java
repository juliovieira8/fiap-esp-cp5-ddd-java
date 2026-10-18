package br.com.fiap.bank.core.application.exception;

/** Conflito com o estado atual do recurso (mapeada para HTTP 409). */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}

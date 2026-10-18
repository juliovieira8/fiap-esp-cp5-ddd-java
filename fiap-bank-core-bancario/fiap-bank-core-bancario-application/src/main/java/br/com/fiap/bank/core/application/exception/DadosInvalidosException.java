package br.com.fiap.bank.core.application.exception;

/** Requisicao semanticamente invalida (mapeada para HTTP 400). */
public class DadosInvalidosException extends RuntimeException {

    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}

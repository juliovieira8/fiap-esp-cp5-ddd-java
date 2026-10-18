package br.com.fiap.bank.core.application.exception;

/** Recurso inexistente (mapeada para HTTP 404 na camada de apresentacao). */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}

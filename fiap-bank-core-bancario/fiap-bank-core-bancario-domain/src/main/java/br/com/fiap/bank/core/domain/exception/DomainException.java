package br.com.fiap.bank.core.domain.exception;

/**
 * Raiz das excecoes de dominio. Nao depende de nenhum framework: a camada de aplicacao e
 * responsavel por traduzi-las para erros de aplicacao/HTTP.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String mensagem) {
        super(mensagem);
    }
}

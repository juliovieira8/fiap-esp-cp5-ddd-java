package br.com.fiap.bank.plataforma.domain.exception;

/**
 * Raiz das excecoes de dominio. Nao depende de nenhum framework: a camada de aplicacao
 * traduz estas excecoes para erros de aplicacao/HTTP.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String mensagem) {
        super(mensagem);
    }

    protected DomainException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}

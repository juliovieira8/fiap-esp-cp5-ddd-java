package br.com.fiap.bank.core.domain.exception;

/** Operacao valida em formato, mas que conflita com o estado atual do negocio. */
public class RegraDeNegocioException extends DomainException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}

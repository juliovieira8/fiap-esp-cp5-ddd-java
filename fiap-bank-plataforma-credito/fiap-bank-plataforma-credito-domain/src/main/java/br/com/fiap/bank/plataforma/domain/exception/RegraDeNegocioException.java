package br.com.fiap.bank.plataforma.domain.exception;

/** Operacao valida em formato, mas incompativel com o estado atual da solicitacao. */
public class RegraDeNegocioException extends DomainException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}

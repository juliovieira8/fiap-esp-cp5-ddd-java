package br.com.fiap.bank.plataforma.domain.exception;

/** Valor que viola uma invariante de formato/dominio (CPF, tipo de credito, valores...). */
public class DadoInvalidoException extends DomainException {

    public DadoInvalidoException(String mensagem) {
        super(mensagem);
    }
}

package br.com.fiap.bank.plataforma.application.exception;

/** Dependencia externa fora do ar (mapeada para HTTP 503). */
public class ServicoIndisponivelException extends RuntimeException {

    public ServicoIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}

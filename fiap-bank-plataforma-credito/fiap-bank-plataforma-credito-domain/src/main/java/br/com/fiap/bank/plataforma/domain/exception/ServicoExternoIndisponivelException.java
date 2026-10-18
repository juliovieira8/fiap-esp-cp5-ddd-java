package br.com.fiap.bank.plataforma.domain.exception;

/** Falha de comunicacao com um sistema externo (Core Bancario, SCR, Bureau, CRM, Notificacoes). */
public class ServicoExternoIndisponivelException extends DomainException {

    public ServicoExternoIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}

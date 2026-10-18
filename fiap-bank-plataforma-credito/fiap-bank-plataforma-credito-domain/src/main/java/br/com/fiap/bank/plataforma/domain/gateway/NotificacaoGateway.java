package br.com.fiap.bank.plataforma.domain.gateway;

import br.com.fiap.bank.plataforma.domain.model.Cpf;

/** Port: Sistema de Notificacoes (SMS/e-mail ao cliente). */
public interface NotificacaoGateway {

    void notificar(Cpf cpf, String mensagem);
}

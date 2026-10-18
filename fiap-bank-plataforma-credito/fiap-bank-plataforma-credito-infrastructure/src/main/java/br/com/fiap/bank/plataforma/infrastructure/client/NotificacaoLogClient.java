package br.com.fiap.bank.plataforma.infrastructure.client;

import br.com.fiap.bank.plataforma.domain.gateway.NotificacaoGateway;
import br.com.fiap.bank.plataforma.domain.model.Cpf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Simulacao do Sistema de Notificacoes: registra em log o SMS/e-mail que seria disparado. */
@Slf4j
@Component
public class NotificacaoLogClient implements NotificacaoGateway {

    @Override
    public void notificar(Cpf cpf, String mensagem) {
        log.info("[SISTEMA DE NOTIFICAÇÕES] Notificação enviada para o CPF {}: {}", cpf, mensagem);
    }
}

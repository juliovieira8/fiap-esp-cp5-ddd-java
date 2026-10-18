package br.com.fiap.bank.plataforma.infrastructure.client;

import br.com.fiap.bank.plataforma.domain.gateway.ScrGateway;
import br.com.fiap.bank.plataforma.domain.model.Cpf;
import org.springframework.stereotype.Component;

/** Simulacao do Sistema do Banco Central (SCR): CPFs terminados em 9 possuem restricao ativa. */
@Component
public class ScrSimuladoClient implements ScrGateway {

    @Override
    public boolean possuiRestricoes(Cpf cpf) {
        return cpf.terminaCom("9");
    }
}

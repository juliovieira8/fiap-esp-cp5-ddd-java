package br.com.fiap.bank.plataforma.domain.gateway;

import br.com.fiap.bank.plataforma.domain.model.Cpf;

/** Port: Sistema de Informacoes de Credito do Banco Central (SCR). */
public interface ScrGateway {

    /** Indica se o cliente possui restricoes ou dividas vencidas em outras instituicoes. */
    boolean possuiRestricoes(Cpf cpf);
}

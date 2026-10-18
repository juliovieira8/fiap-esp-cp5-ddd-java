package br.com.fiap.bank.plataforma.domain.gateway;

import br.com.fiap.bank.plataforma.domain.model.Cpf;

/** Port: Bureau de Credito (Serasa/SPC). */
public interface BureauCreditoGateway {

    /** Score de risco do cliente (0 a 1000). */
    int consultarScore(Cpf cpf);
}

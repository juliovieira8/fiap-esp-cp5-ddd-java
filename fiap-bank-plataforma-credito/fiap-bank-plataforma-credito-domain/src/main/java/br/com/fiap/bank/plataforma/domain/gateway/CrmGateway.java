package br.com.fiap.bank.plataforma.domain.gateway;

import br.com.fiap.bank.plataforma.domain.model.Cpf;
import br.com.fiap.bank.plataforma.domain.model.HistoricoCrm;

/** Port: CRM corporativo (historico do cliente no banco). */
public interface CrmGateway {

    HistoricoCrm consultarHistorico(Cpf cpf);
}

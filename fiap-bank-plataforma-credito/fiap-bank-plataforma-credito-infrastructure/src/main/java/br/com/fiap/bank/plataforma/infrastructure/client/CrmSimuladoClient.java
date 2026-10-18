package br.com.fiap.bank.plataforma.infrastructure.client;

import br.com.fiap.bank.plataforma.domain.gateway.CrmGateway;
import br.com.fiap.bank.plataforma.domain.model.Cpf;
import br.com.fiap.bank.plataforma.domain.model.HistoricoCrm;
import org.springframework.stereotype.Component;

/**
 * Simulacao do CRM corporativo: tempo de relacionamento derivado do CPF; CPFs terminados em
 * "000" possuem bloqueio interno.
 */
@Component
public class CrmSimuladoClient implements CrmGateway {

    @Override
    public HistoricoCrm consultarHistorico(Cpf cpf) {
        int soma = 0;
        for (char digito : cpf.getValor().toCharArray()) {
            soma += Character.getNumericValue(digito);
        }
        return new HistoricoCrm((soma % 15) + 1, cpf.terminaCom("000"));
    }
}

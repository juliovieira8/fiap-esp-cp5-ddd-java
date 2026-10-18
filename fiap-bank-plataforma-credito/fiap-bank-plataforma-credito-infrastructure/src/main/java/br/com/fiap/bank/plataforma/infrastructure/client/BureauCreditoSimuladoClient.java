package br.com.fiap.bank.plataforma.infrastructure.client;

import br.com.fiap.bank.plataforma.domain.gateway.BureauCreditoGateway;
import br.com.fiap.bank.plataforma.domain.model.Cpf;
import org.springframework.stereotype.Component;

/** Simulacao do Bureau de Credito (Serasa/SPC): score deterministico entre 400 e 950 derivado do CPF. */
@Component
public class BureauCreditoSimuladoClient implements BureauCreditoGateway {

    @Override
    public int consultarScore(Cpf cpf) {
        int soma = 0;
        for (char digito : cpf.getValor().toCharArray()) {
            soma += Character.getNumericValue(digito);
        }
        return 400 + (soma * 9) % 551;
    }
}

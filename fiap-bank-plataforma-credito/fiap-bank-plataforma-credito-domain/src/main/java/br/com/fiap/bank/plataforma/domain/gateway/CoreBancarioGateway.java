package br.com.fiap.bank.plataforma.domain.gateway;

import br.com.fiap.bank.plataforma.domain.model.ContaCore;
import br.com.fiap.bank.plataforma.domain.model.Cpf;

import java.util.Optional;

/** Port: Core Bancario (contas e saldos). */
public interface CoreBancarioGateway {

    /**
     * Consulta a conta vinculada ao CPF.
     *
     * @return a conta, ou vazio quando o cliente/conta nao existe no Core
     * @throws br.com.fiap.bank.plataforma.domain.exception.ServicoExternoIndisponivelException
     *         quando o Core nao pode ser alcancado
     */
    Optional<ContaCore> consultarContaPorCpf(Cpf cpf);
}

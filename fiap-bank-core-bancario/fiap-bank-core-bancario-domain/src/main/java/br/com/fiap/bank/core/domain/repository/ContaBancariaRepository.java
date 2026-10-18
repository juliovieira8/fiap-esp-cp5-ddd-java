package br.com.fiap.bank.core.domain.repository;

import br.com.fiap.bank.core.domain.model.ContaBancaria;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Port (interface de Repositorio) de contas bancarias. */
public interface ContaBancariaRepository {

    ContaBancaria salvar(ContaBancaria conta);

    Optional<ContaBancaria> buscarPorNumero(String numero);

    Optional<ContaBancaria> buscarPorClienteId(UUID clienteId);

    List<ContaBancaria> listarTodas();

    boolean existePorNumero(String numero);
}

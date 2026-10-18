package br.com.fiap.bank.core.domain.repository;

import br.com.fiap.bank.core.domain.model.Cliente;
import br.com.fiap.bank.core.domain.model.Cpf;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface de Repositorio) do agregado Cliente. A implementacao concreta (adapter) vive
 * na camada de infraestrutura; o dominio nao conhece JPA nem Spring Data.
 * Os clientes retornados ja carregam a conta vinculada (quando existir).
 */
public interface ClienteRepository {

    Cliente salvar(Cliente cliente);

    Optional<Cliente> buscarPorId(UUID id);

    Optional<Cliente> buscarPorCpf(Cpf cpf);

    List<Cliente> listarTodos();

    boolean existePorCpf(Cpf cpf);

    /** Remove o cliente e a conta a ele vinculada. */
    void remover(UUID id);
}

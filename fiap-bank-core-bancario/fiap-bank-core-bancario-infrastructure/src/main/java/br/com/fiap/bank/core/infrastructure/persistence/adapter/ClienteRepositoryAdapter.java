package br.com.fiap.bank.core.infrastructure.persistence.adapter;

import br.com.fiap.bank.core.domain.model.Cliente;
import br.com.fiap.bank.core.domain.model.ContaBancaria;
import br.com.fiap.bank.core.domain.model.Cpf;
import br.com.fiap.bank.core.domain.repository.ClienteRepository;
import br.com.fiap.bank.core.infrastructure.persistence.entity.ClienteJpaEntity;
import br.com.fiap.bank.core.infrastructure.persistence.mapper.ClienteMapper;
import br.com.fiap.bank.core.infrastructure.persistence.mapper.ContaBancariaMapper;
import br.com.fiap.bank.core.infrastructure.persistence.repository.ClienteJpaRepository;
import br.com.fiap.bank.core.infrastructure.persistence.repository.ContaBancariaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Adapter: implementa o port do dominio {@link ClienteRepository} usando Spring Data JPA. */
@Repository
@RequiredArgsConstructor
public class ClienteRepositoryAdapter implements ClienteRepository {

    private final ClienteJpaRepository clienteJpaRepository;
    private final ContaBancariaJpaRepository contaJpaRepository;

    @Override
    @Transactional
    public Cliente salvar(Cliente cliente) {
        ClienteJpaEntity entity = clienteJpaRepository.findById(cliente.getId())
                .orElseGet(ClienteJpaEntity::new);
        ClienteMapper.copiarParaEntidade(cliente, entity);
        ClienteJpaEntity salvo = clienteJpaRepository.save(entity);
        return ClienteMapper.paraDominio(salvo, cliente.getConta());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorId(UUID id) {
        return clienteJpaRepository.findById(id).map(this::hidratar);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorCpf(Cpf cpf) {
        return clienteJpaRepository.findByCpf(cpf.getValor()).map(this::hidratar);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        Map<UUID, ContaBancaria> contasPorCliente = contaJpaRepository.findAll().stream()
                .map(ContaBancariaMapper::paraDominio)
                .collect(Collectors.toMap(ContaBancaria::getClienteId, Function.identity()));
        return clienteJpaRepository.findAll().stream()
                .map(entity -> ClienteMapper.paraDominio(entity, contasPorCliente.get(entity.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCpf(Cpf cpf) {
        return clienteJpaRepository.existsByCpf(cpf.getValor());
    }

    @Override
    @Transactional
    public void remover(UUID id) {
        contaJpaRepository.deleteByClienteId(id);
        clienteJpaRepository.deleteById(id);
    }

    private Cliente hidratar(ClienteJpaEntity entity) {
        ContaBancaria conta = contaJpaRepository.findByClienteId(entity.getId())
                .map(ContaBancariaMapper::paraDominio)
                .orElse(null);
        return ClienteMapper.paraDominio(entity, conta);
    }
}

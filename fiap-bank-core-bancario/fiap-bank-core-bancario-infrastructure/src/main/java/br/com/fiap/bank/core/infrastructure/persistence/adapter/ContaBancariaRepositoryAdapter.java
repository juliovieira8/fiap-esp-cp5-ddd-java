package br.com.fiap.bank.core.infrastructure.persistence.adapter;

import br.com.fiap.bank.core.domain.exception.DadoInvalidoException;
import br.com.fiap.bank.core.domain.model.ContaBancaria;
import br.com.fiap.bank.core.domain.repository.ContaBancariaRepository;
import br.com.fiap.bank.core.infrastructure.persistence.entity.ClienteJpaEntity;
import br.com.fiap.bank.core.infrastructure.persistence.entity.ContaBancariaJpaEntity;
import br.com.fiap.bank.core.infrastructure.persistence.mapper.ContaBancariaMapper;
import br.com.fiap.bank.core.infrastructure.persistence.repository.ClienteJpaRepository;
import br.com.fiap.bank.core.infrastructure.persistence.repository.ContaBancariaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Adapter: implementa o port do dominio {@link ContaBancariaRepository} usando Spring Data JPA. */
@Repository
@RequiredArgsConstructor
public class ContaBancariaRepositoryAdapter implements ContaBancariaRepository {

    private final ContaBancariaJpaRepository contaJpaRepository;
    private final ClienteJpaRepository clienteJpaRepository;

    @Override
    @Transactional
    public ContaBancaria salvar(ContaBancaria conta) {
        ClienteJpaEntity titular = clienteJpaRepository.findById(conta.getClienteId())
                .orElseThrow(() -> new DadoInvalidoException("Titular da conta não encontrado."));
        ContaBancariaJpaEntity entity = contaJpaRepository.findById(conta.getId())
                .orElseGet(ContaBancariaJpaEntity::new);
        ContaBancariaMapper.copiarParaEntidade(conta, titular, entity);
        return ContaBancariaMapper.paraDominio(contaJpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ContaBancaria> buscarPorNumero(String numero) {
        return contaJpaRepository.findByNumero(numero).map(ContaBancariaMapper::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ContaBancaria> buscarPorClienteId(UUID clienteId) {
        return contaJpaRepository.findByClienteId(clienteId).map(ContaBancariaMapper::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContaBancaria> listarTodas() {
        return contaJpaRepository.findAll().stream().map(ContaBancariaMapper::paraDominio).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorNumero(String numero) {
        return contaJpaRepository.existsByNumero(numero);
    }
}

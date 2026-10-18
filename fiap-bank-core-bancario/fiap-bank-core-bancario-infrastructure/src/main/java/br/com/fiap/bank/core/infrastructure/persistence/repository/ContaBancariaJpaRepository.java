package br.com.fiap.bank.core.infrastructure.persistence.repository;

import br.com.fiap.bank.core.infrastructure.persistence.entity.ContaBancariaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ContaBancariaJpaRepository extends JpaRepository<ContaBancariaJpaEntity, UUID> {

    Optional<ContaBancariaJpaEntity> findByNumero(String numero);

    Optional<ContaBancariaJpaEntity> findByClienteId(UUID clienteId);

    boolean existsByNumero(String numero);

    void deleteByClienteId(UUID clienteId);
}

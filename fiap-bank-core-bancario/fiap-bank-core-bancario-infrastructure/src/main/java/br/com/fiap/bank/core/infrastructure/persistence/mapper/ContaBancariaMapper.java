package br.com.fiap.bank.core.infrastructure.persistence.mapper;

import br.com.fiap.bank.core.domain.model.ContaBancaria;
import br.com.fiap.bank.core.domain.model.Saldo;
import br.com.fiap.bank.core.infrastructure.persistence.entity.ClienteJpaEntity;
import br.com.fiap.bank.core.infrastructure.persistence.entity.ContaBancariaJpaEntity;

/** Conversao entre a entidade de dominio ContaBancaria e sua representacao JPA. */
public final class ContaBancariaMapper {

    private ContaBancariaMapper() {
    }

    public static ContaBancaria paraDominio(ContaBancariaJpaEntity entity) {
        return ContaBancaria.reconstituir(
                entity.getId(),
                entity.getCliente().getId(),
                entity.getNumero(),
                entity.getAgencia(),
                entity.getTipo(),
                entity.getStatus(),
                Saldo.de(entity.getSaldo()),
                entity.getDataAbertura());
    }

    /** Copia o estado do dominio para a entidade (nova ou ja gerenciada, preservando a versao). */
    public static void copiarParaEntidade(ContaBancaria conta, ClienteJpaEntity titular, ContaBancariaJpaEntity entity) {
        entity.setId(conta.getId());
        entity.setCliente(titular);
        entity.setNumero(conta.getNumero());
        entity.setAgencia(conta.getAgencia());
        entity.setTipo(conta.getTipo());
        entity.setStatus(conta.getStatus());
        entity.setSaldo(conta.getSaldo().getValor());
        entity.setDataAbertura(conta.getDataAbertura());
    }
}

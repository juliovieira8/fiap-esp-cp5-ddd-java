package br.com.fiap.bank.core.infrastructure.persistence.mapper;

import br.com.fiap.bank.core.domain.model.Cliente;
import br.com.fiap.bank.core.domain.model.ContaBancaria;
import br.com.fiap.bank.core.domain.model.Cpf;
import br.com.fiap.bank.core.domain.model.Email;
import br.com.fiap.bank.core.infrastructure.persistence.entity.ClienteJpaEntity;

/** Conversao entre a entidade de dominio Cliente e sua representacao JPA. */
public final class ClienteMapper {

    private ClienteMapper() {
    }

    public static Cliente paraDominio(ClienteJpaEntity entity, ContaBancaria conta) {
        return Cliente.reconstituir(
                entity.getId(),
                entity.getNome(),
                Email.de(entity.getEmail()),
                entity.getDataNascimento(),
                Cpf.de(entity.getCpf()),
                entity.getRendaMensal(),
                conta);
    }

    public static void copiarParaEntidade(Cliente cliente, ClienteJpaEntity entity) {
        entity.setId(cliente.getId());
        entity.setCpf(cliente.getCpf().getValor());
        entity.setNome(cliente.getNome());
        entity.setEmail(cliente.getEmail().getValor());
        entity.setDataNascimento(cliente.getDataNascimento());
        entity.setRendaMensal(cliente.getRendaMensal());
    }
}

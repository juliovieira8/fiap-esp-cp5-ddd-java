package br.com.fiap.bank.plataforma.infrastructure.persistence.adapter;

import br.com.fiap.bank.plataforma.domain.model.Cpf;
import br.com.fiap.bank.plataforma.domain.model.SolicitacaoCredito;
import br.com.fiap.bank.plataforma.domain.repository.SolicitacaoCreditoRepository;
import br.com.fiap.bank.plataforma.infrastructure.persistence.entity.SolicitacaoCreditoEntity;
import br.com.fiap.bank.plataforma.infrastructure.persistence.mapper.SolicitacaoCreditoMapper;
import br.com.fiap.bank.plataforma.infrastructure.persistence.repository.SolicitacaoCreditoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Adapter: implementa o port do dominio {@link SolicitacaoCreditoRepository} com Spring Data JPA. */
@Repository
@RequiredArgsConstructor
public class SolicitacaoCreditoRepositoryAdapter implements SolicitacaoCreditoRepository {

    private final SolicitacaoCreditoJpaRepository jpaRepository;

    @Override
    @Transactional
    public SolicitacaoCredito salvar(SolicitacaoCredito solicitacao) {
        SolicitacaoCreditoEntity entity = solicitacao.getId() == null
                ? new SolicitacaoCreditoEntity()
                : jpaRepository.findById(solicitacao.getId()).orElseGet(SolicitacaoCreditoEntity::new);
        SolicitacaoCreditoMapper.copiarParaEntidade(solicitacao, entity);
        return SolicitacaoCreditoMapper.paraDominio(jpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SolicitacaoCredito> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(SolicitacaoCreditoMapper::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitacaoCredito> listarTodas() {
        return jpaRepository.findAllByOrderByDataSolicitacaoDesc().stream()
                .map(SolicitacaoCreditoMapper::paraDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitacaoCredito> listarPorCpf(Cpf cpf) {
        return jpaRepository.findByCpfClienteOrderByDataSolicitacaoDesc(cpf.getValor()).stream()
                .map(SolicitacaoCreditoMapper::paraDominio)
                .toList();
    }
}

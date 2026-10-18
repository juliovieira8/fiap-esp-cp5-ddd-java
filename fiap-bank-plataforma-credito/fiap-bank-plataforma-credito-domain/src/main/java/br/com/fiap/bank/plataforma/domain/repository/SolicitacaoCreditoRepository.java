package br.com.fiap.bank.plataforma.domain.repository;

import br.com.fiap.bank.plataforma.domain.model.Cpf;
import br.com.fiap.bank.plataforma.domain.model.SolicitacaoCredito;

import java.util.List;
import java.util.Optional;

/**
 * Port (interface de Repositorio) das solicitacoes de credito. A implementacao concreta
 * (adapter Spring Data JPA) vive na camada de infraestrutura.
 */
public interface SolicitacaoCreditoRepository {

    SolicitacaoCredito salvar(SolicitacaoCredito solicitacao);

    Optional<SolicitacaoCredito> buscarPorId(Long id);

    List<SolicitacaoCredito> listarTodas();

    List<SolicitacaoCredito> listarPorCpf(Cpf cpf);
}

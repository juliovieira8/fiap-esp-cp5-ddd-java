package br.com.fiap.bank.plataforma.application.service;

import br.com.fiap.bank.plataforma.application.dto.DecisaoOperador;
import br.com.fiap.bank.plataforma.application.dto.DecisaoSolicitacaoRequest;
import br.com.fiap.bank.plataforma.application.dto.SolicitacaoCreditoResponse;
import br.com.fiap.bank.plataforma.application.dto.SolicitarCreditoRequest;
import br.com.fiap.bank.plataforma.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.bank.plataforma.domain.exception.ServicoExternoIndisponivelException;
import br.com.fiap.bank.plataforma.domain.gateway.BureauCreditoGateway;
import br.com.fiap.bank.plataforma.domain.gateway.CoreBancarioGateway;
import br.com.fiap.bank.plataforma.domain.gateway.CrmGateway;
import br.com.fiap.bank.plataforma.domain.gateway.NotificacaoGateway;
import br.com.fiap.bank.plataforma.domain.gateway.ScrGateway;
import br.com.fiap.bank.plataforma.domain.model.AnaliseExterna;
import br.com.fiap.bank.plataforma.domain.model.Cpf;
import br.com.fiap.bank.plataforma.domain.model.DecisaoCredito;
import br.com.fiap.bank.plataforma.domain.model.HistoricoCrm;
import br.com.fiap.bank.plataforma.domain.model.SituacaoCore;
import br.com.fiap.bank.plataforma.domain.model.SolicitacaoCredito;
import br.com.fiap.bank.plataforma.domain.model.StatusSolicitacao;
import br.com.fiap.bank.plataforma.domain.model.TipoCredito;
import br.com.fiap.bank.plataforma.domain.repository.SolicitacaoCreditoRepository;
import br.com.fiap.bank.plataforma.domain.service.MotorDecisaoCredito;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static br.com.fiap.bank.plataforma.application.exception.TradutorDeExcecoes.executar;
import static br.com.fiap.bank.plataforma.application.exception.TradutorDeExcecoes.executarSemRetorno;

/**
 * Casos de uso da esteira de credito: solicitar, consultar e registrar a decisao da Mesa de
 * Operacoes. Orquestra o dominio e as portas de integracao (SCR, Bureau, CRM, Core, Notificacoes).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitacaoCreditoService {

    private final SolicitacaoCreditoRepository repository;
    private final ScrGateway scrGateway;
    private final BureauCreditoGateway bureauGateway;
    private final CrmGateway crmGateway;
    private final CoreBancarioGateway coreBancarioGateway;
    private final NotificacaoGateway notificacaoGateway;
    private final MotorDecisaoCredito motorDecisao;

    @Transactional
    public SolicitacaoCreditoResponse solicitar(SolicitarCreditoRequest request) {
        return executar(() -> {
            Cpf cpf = Cpf.de(request.cpfCliente());
            TipoCredito tipo = TipoCredito.fromTexto(request.tipoCredito());
            SolicitacaoCredito solicitacao = SolicitacaoCredito.abrir(cpf, request.nomeCliente(), tipo,
                    request.valorSolicitado(), request.quantidadeParcelas(), request.rendaMensal());

            boolean restricaoScr = scrGateway.possuiRestricoes(cpf);
            int scoreBureau = bureauGateway.consultarScore(cpf);
            HistoricoCrm historicoCrm = crmGateway.consultarHistorico(cpf);
            SituacaoCore situacaoCore = consultarSituacaoCore(cpf);

            DecisaoCredito decisao = motorDecisao.avaliar(solicitacao,
                    new AnaliseExterna(restricaoScr, scoreBureau, historicoCrm.bloqueioInterno(), situacaoCore));
            solicitacao.registrarAnalise(restricaoScr, scoreBureau, decisao);

            SolicitacaoCredito salva = repository.salvar(solicitacao);
            notificacaoGateway.notificar(cpf, "Sua solicitação de crédito #" + salva.getId()
                    + " está no status: " + salva.getStatus());
            return SolicitacaoCreditoResponse.de(salva);
        });
    }

    /** Lista todas as solicitacoes ou, quando informado, apenas as do CPF (filtro da colecao). */
    @Transactional(readOnly = true)
    public List<SolicitacaoCreditoResponse> listar(String cpfCliente) {
        List<SolicitacaoCredito> solicitacoes = cpfCliente == null || cpfCliente.isBlank()
                ? repository.listarTodas()
                : repository.listarPorCpf(executar(() -> Cpf.de(cpfCliente)));
        return solicitacoes.stream().map(SolicitacaoCreditoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public SolicitacaoCreditoResponse buscarPorId(Long id) {
        return SolicitacaoCreditoResponse.de(obterPorId(id));
    }

    /** Registra a decisao da Mesa de Operacoes (idempotente: repetir a mesma decisao nao notifica de novo). */
    @Transactional
    public SolicitacaoCreditoResponse registrarDecisao(Long id, DecisaoSolicitacaoRequest request) {
        SolicitacaoCredito solicitacao = obterPorId(id);
        StatusSolicitacao statusAnterior = solicitacao.getStatus();
        executarSemRetorno(() -> {
            if (request.decisao() == DecisaoOperador.APROVADO) {
                solicitacao.aprovar(request.parecer());
            } else {
                solicitacao.reprovar(request.parecer());
            }
        });
        SolicitacaoCredito salva = repository.salvar(solicitacao);
        if (salva.getStatus() != statusAnterior) {
            notificacaoGateway.notificar(salva.getCpf(), mensagemDecisao(salva));
        }
        return SolicitacaoCreditoResponse.de(salva);
    }

    private SolicitacaoCredito obterPorId(Long id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação de crédito não encontrada com ID: " + id));
    }

    private SituacaoCore consultarSituacaoCore(Cpf cpf) {
        try {
            return coreBancarioGateway.consultarContaPorCpf(cpf)
                    .map(conta -> conta.ativa() ? SituacaoCore.ATIVA : SituacaoCore.INATIVA)
                    .orElse(SituacaoCore.INEXISTENTE);
        } catch (ServicoExternoIndisponivelException e) {
            log.warn("Core Bancário indisponível; a solicitação seguirá para análise manual: {}", e.getMessage());
            return SituacaoCore.INDISPONIVEL;
        }
    }

    private String mensagemDecisao(SolicitacaoCredito solicitacao) {
        String resultado = switch (solicitacao.getStatus()) {
            case APROVADO -> "foi APROVADA";
            case REPROVADO -> "foi REPROVADA";
            default -> "foi atualizada";
        };
        return "Sua solicitação de crédito #" + solicitacao.getId() + " " + resultado + " pela Mesa de Operações.";
    }
}

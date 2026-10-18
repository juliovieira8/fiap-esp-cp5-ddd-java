package br.com.fiap.bank.plataforma.application.service;

import br.com.fiap.bank.plataforma.application.dto.DecisaoOperador;
import br.com.fiap.bank.plataforma.application.dto.DecisaoSolicitacaoRequest;
import br.com.fiap.bank.plataforma.application.dto.SolicitacaoCreditoResponse;
import br.com.fiap.bank.plataforma.application.dto.SolicitarCreditoRequest;
import br.com.fiap.bank.plataforma.application.exception.ConflitoException;
import br.com.fiap.bank.plataforma.application.exception.DadosInvalidosException;
import br.com.fiap.bank.plataforma.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.bank.plataforma.domain.exception.ServicoExternoIndisponivelException;
import br.com.fiap.bank.plataforma.domain.gateway.BureauCreditoGateway;
import br.com.fiap.bank.plataforma.domain.gateway.CoreBancarioGateway;
import br.com.fiap.bank.plataforma.domain.gateway.CrmGateway;
import br.com.fiap.bank.plataforma.domain.gateway.NotificacaoGateway;
import br.com.fiap.bank.plataforma.domain.gateway.ScrGateway;
import br.com.fiap.bank.plataforma.domain.model.ContaCore;
import br.com.fiap.bank.plataforma.domain.model.Cpf;
import br.com.fiap.bank.plataforma.domain.model.HistoricoCrm;
import br.com.fiap.bank.plataforma.domain.model.SolicitacaoCredito;
import br.com.fiap.bank.plataforma.domain.model.StatusSolicitacao;
import br.com.fiap.bank.plataforma.domain.model.TipoCredito;
import br.com.fiap.bank.plataforma.domain.repository.SolicitacaoCreditoRepository;
import br.com.fiap.bank.plataforma.domain.service.MotorDecisaoCredito;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SolicitacaoCreditoServiceTest {

    private SolicitacaoCreditoRepository repository;
    private ScrGateway scrGateway;
    private BureauCreditoGateway bureauGateway;
    private CrmGateway crmGateway;
    private CoreBancarioGateway coreGateway;
    private NotificacaoGateway notificacaoGateway;
    private SolicitacaoCreditoService service;

    @BeforeEach
    void preparar() {
        repository = mock(SolicitacaoCreditoRepository.class);
        scrGateway = mock(ScrGateway.class);
        bureauGateway = mock(BureauCreditoGateway.class);
        crmGateway = mock(CrmGateway.class);
        coreGateway = mock(CoreBancarioGateway.class);
        notificacaoGateway = mock(NotificacaoGateway.class);
        service = new SolicitacaoCreditoService(repository, scrGateway, bureauGateway, crmGateway,
                coreGateway, notificacaoGateway, new MotorDecisaoCredito());

        when(scrGateway.possuiRestricoes(any(Cpf.class))).thenReturn(false);
        when(bureauGateway.consultarScore(any(Cpf.class))).thenReturn(700);
        when(crmGateway.consultarHistorico(any(Cpf.class))).thenReturn(new HistoricoCrm(5, false));
        when(coreGateway.consultarContaPorCpf(any(Cpf.class)))
                .thenReturn(Optional.of(new ContaCore("12345-6", "0001", "ATIVA", new BigDecimal("12500.00"))));
        when(repository.salvar(any(SolicitacaoCredito.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    private SolicitarCreditoRequest requisicao(String tipo) {
        return new SolicitarCreditoRequest("12345678900", "Ana Silva", tipo,
                new BigDecimal("15000.00"), 24, new BigDecimal("7500.00"));
    }

    private SolicitacaoCredito solicitacaoEmAnalise() {
        return SolicitacaoCredito.abrir(Cpf.de("12345678900"), "Ana Silva", TipoCredito.EMPRESTIMO_PESSOAL,
                new BigDecimal("15000.00"), 24, new BigDecimal("7500.00"));
    }

    @Test
    void deveAprovarAutomaticamenteQuandoOMotorPermite() {
        SolicitacaoCreditoResponse resposta = service.solicitar(requisicao("EMPRÉSTIMO PESSOAL"));

        assertThat(resposta.status()).isEqualTo(StatusSolicitacao.APROVADO.name());
        assertThat(resposta.tipoCredito()).isEqualTo("EMPRESTIMO_PESSOAL");
        assertThat(resposta.tipoCreditoDescricao()).isEqualTo("EMPRÉSTIMO PESSOAL");
        assertThat(resposta.scoreBureau()).isEqualTo(700);
    }

    @Test
    void deveEncaminharParaAMesaQuandoOCoreEstaIndisponivel() {
        when(coreGateway.consultarContaPorCpf(any(Cpf.class)))
                .thenThrow(new ServicoExternoIndisponivelException("fora do ar", new RuntimeException()));

        SolicitacaoCreditoResponse resposta = service.solicitar(requisicao("CONSIGNADO"));

        assertThat(resposta.status()).isEqualTo(StatusSolicitacao.EM_ANALISE.name());
    }

    @Test
    void deveReprovarQuandoOClienteNaoTemContaNoCore() {
        when(coreGateway.consultarContaPorCpf(any(Cpf.class))).thenReturn(Optional.empty());

        SolicitacaoCreditoResponse resposta = service.solicitar(requisicao("CONSIGNADO"));

        assertThat(resposta.status()).isEqualTo(StatusSolicitacao.REPROVADO.name());
    }

    @Test
    void tipoDeCreditoDesconhecidoDeveVirarDadosInvalidos() {
        assertThatThrownBy(() -> service.solicitar(requisicao("CARTAO")))
                .isInstanceOf(DadosInvalidosException.class);
    }

    @Test
    void mesaDeveConseguirAprovarSolicitacaoEmAnalise() {
        when(repository.buscarPorId(1L)).thenReturn(Optional.of(solicitacaoEmAnalise()));

        SolicitacaoCreditoResponse resposta = service.registrarDecisao(1L,
                new DecisaoSolicitacaoRequest(DecisaoOperador.APROVADO, "Documentação conferida"));

        assertThat(resposta.status()).isEqualTo(StatusSolicitacao.APROVADO.name());
        assertThat(resposta.parecerOperador()).contains("Documentação conferida");
    }

    @Test
    void naoDeveAprovarSolicitacaoJaReprovada() {
        SolicitacaoCredito reprovada = solicitacaoEmAnalise();
        reprovada.reprovar("Risco elevado");
        when(repository.buscarPorId(2L)).thenReturn(Optional.of(reprovada));

        assertThatThrownBy(() -> service.registrarDecisao(2L,
                new DecisaoSolicitacaoRequest(DecisaoOperador.APROVADO, null)))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void solicitacaoInexistenteDeveVirarRecursoNaoEncontrado() {
        when(repository.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}

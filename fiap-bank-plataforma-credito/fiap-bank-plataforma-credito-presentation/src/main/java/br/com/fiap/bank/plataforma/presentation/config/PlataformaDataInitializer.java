package br.com.fiap.bank.plataforma.presentation.config;

import br.com.fiap.bank.plataforma.application.dto.SolicitarCreditoRequest;
import br.com.fiap.bank.plataforma.application.service.SolicitacaoCreditoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/** Carga inicial: tres solicitacoes de exemplo processadas pelo motor de credito. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlataformaDataInitializer implements CommandLineRunner {

    private final SolicitacaoCreditoService solicitacaoService;

    @Override
    public void run(String... args) {
        List<SolicitarCreditoRequest> exemplos = List.of(
                new SolicitarCreditoRequest("12345678900", "Ana Silva", "EMPRESTIMO_PESSOAL",
                        new BigDecimal("15000.00"), 24, new BigDecimal("7500.00")),
                new SolicitarCreditoRequest("98765432100", "Carlos Eduardo Oliveira", "CREDITO_IMOBILIARIO",
                        new BigDecimal("350000.00"), 180, new BigDecimal("12000.00")),
                new SolicitarCreditoRequest("55544433399", "Mariana Souza", "CREDITO_AUTOMOTIVO",
                        new BigDecimal("45000.00"), 48, new BigDecimal("4200.00")));

        int carregadas = 0;
        for (SolicitarCreditoRequest exemplo : exemplos) {
            try {
                solicitacaoService.solicitar(exemplo);
                carregadas++;
            } catch (RuntimeException e) {
                log.warn("Não foi possível carregar a solicitação de exemplo do CPF {}: {}",
                        exemplo.cpfCliente(), e.getMessage());
            }
        }
        log.info("Plataforma de Crédito: {} de {} solicitações iniciais carregadas.", carregadas, exemplos.size());
    }
}

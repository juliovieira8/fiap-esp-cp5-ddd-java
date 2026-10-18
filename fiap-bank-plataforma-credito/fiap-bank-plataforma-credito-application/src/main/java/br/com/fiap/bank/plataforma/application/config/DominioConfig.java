package br.com.fiap.bank.plataforma.application.config;

import br.com.fiap.bank.plataforma.domain.service.MotorDecisaoCredito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registra como beans os servicos de dominio puros (que nao conhecem o Spring). */
@Configuration
public class DominioConfig {

    @Bean
    public MotorDecisaoCredito motorDecisaoCredito() {
        return new MotorDecisaoCredito();
    }
}

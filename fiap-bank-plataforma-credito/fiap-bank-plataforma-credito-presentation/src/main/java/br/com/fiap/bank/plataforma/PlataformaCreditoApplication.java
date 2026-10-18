package br.com.fiap.bank.plataforma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da Plataforma de Credito (porta 8081). A classe fica no pacote raiz
 * {@code br.com.fiap.bank.plataforma} para que o component scan, as entidades JPA e os repositorios
 * Spring Data dos demais modulos (application e infrastructure) sejam descobertos.
 */
@SpringBootApplication
public class PlataformaCreditoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlataformaCreditoApplication.class, args);
    }
}

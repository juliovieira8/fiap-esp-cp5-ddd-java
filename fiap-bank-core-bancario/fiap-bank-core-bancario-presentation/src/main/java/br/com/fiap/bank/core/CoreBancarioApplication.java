package br.com.fiap.bank.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada do Core Bancario (porta 8080). A classe fica no pacote raiz
 * {@code br.com.fiap.bank.core} para que o component scan, as entidades JPA e os repositorios
 * Spring Data dos demais modulos (application e infrastructure) sejam descobertos.
 */
@SpringBootApplication
public class CoreBancarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreBancarioApplication.class, args);
    }
}

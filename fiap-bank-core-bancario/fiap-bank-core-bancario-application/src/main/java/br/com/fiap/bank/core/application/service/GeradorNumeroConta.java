package br.com.fiap.bank.core.application.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/** Gera numeros de conta no formato {@code NNNNN-D} (5 digitos + digito verificador). */
@Component
public class GeradorNumeroConta {

    private final SecureRandom random = new SecureRandom();

    public String gerar() {
        int base = 10000 + random.nextInt(90000);
        return base + "-" + digitoVerificador(base);
    }

    private int digitoVerificador(int base) {
        String digitos = String.valueOf(base);
        int soma = 0;
        for (int i = 0; i < digitos.length(); i++) {
            soma += Character.getNumericValue(digitos.charAt(i)) * (i + 2);
        }
        return soma % 10;
    }
}

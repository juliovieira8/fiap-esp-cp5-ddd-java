package br.com.fiap.bank.core.domain.model;

import br.com.fiap.bank.core.domain.exception.DadoInvalidoException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.regex.Pattern;

/**
 * Value Object: CPF. Aceita 11 digitos limpos ou o formato canonico com mascara
 * (XXX.XXX.XXX-XX) e sempre armazena apenas os 11 digitos.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Cpf {

    private static final Pattern FORMATO_VALIDO =
            Pattern.compile("^(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$");

    String valor;

    public static Cpf de(String texto) {
        if (texto == null || !FORMATO_VALIDO.matcher(texto.trim()).matches()) {
            throw new DadoInvalidoException(
                    "CPF inválido: informe 11 dígitos numéricos ou o formato XXX.XXX.XXX-XX.");
        }
        return new Cpf(texto.trim().replaceAll("\\D", ""));
    }

    public String formatado() {
        return valor.substring(0, 3) + "." + valor.substring(3, 6) + "."
                + valor.substring(6, 9) + "-" + valor.substring(9);
    }

    @Override
    public String toString() {
        return valor;
    }
}

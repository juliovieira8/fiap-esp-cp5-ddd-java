package br.com.fiap.bank.core.domain.model;

import br.com.fiap.bank.core.domain.exception.DadoInvalidoException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Locale;
import java.util.regex.Pattern;

/** Value Object: endereco de e-mail bem formado (normalizado em minusculas). */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Email {

    private static final Pattern FORMATO_VALIDO = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    String valor;

    public static Email de(String texto) {
        if (texto == null || !FORMATO_VALIDO.matcher(texto.trim()).matches()) {
            throw new DadoInvalidoException("E-mail inválido.");
        }
        return new Email(texto.trim().toLowerCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return valor;
    }
}

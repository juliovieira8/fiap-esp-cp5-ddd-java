package br.com.fiap.bank.plataforma.domain.model;

import br.com.fiap.bank.plataforma.domain.exception.DadoInvalidoException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.regex.Pattern;

/** Value Object: CPF (11 digitos limpos ou formato com mascara; sempre armazenado sem mascara). */
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

    public boolean terminaCom(String sufixo) {
        return valor.endsWith(sufixo);
    }

    @Override
    public String toString() {
        return valor;
    }
}

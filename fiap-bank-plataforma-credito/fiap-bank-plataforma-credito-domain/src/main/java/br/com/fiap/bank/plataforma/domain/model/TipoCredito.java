package br.com.fiap.bank.plataforma.domain.model;

import br.com.fiap.bank.plataforma.domain.exception.DadoInvalidoException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Modalidades de credito. A leitura tolerante ({@link #fromTexto(String)}) aceita tanto o
 * identificador canonico (EMPRESTIMO_PESSOAL) quanto os rotulos historicos usados pelos
 * front-ends legados ("EMPRÉSTIMO PESSOAL"), preservando a compatibilidade dos contratos antigos.
 */
@Getter
@RequiredArgsConstructor
public enum TipoCredito {

    EMPRESTIMO_PESSOAL("EMPRÉSTIMO PESSOAL"),
    CREDITO_IMOBILIARIO("CRÉDITO IMOBILIÁRIO"),
    CREDITO_AUTOMOTIVO("CRÉDITO AUTOMOTIVO"),
    CONSIGNADO("CONSIGNADO");

    /** Rotulo historico exibido pelos front-ends legados. */
    private final String descricao;

    public static TipoCredito fromTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new DadoInvalidoException("O tipo de crédito é obrigatório.");
        }
        String normalizado = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_");
        for (TipoCredito tipo : values()) {
            if (tipo.name().equals(normalizado)) {
                return tipo;
            }
        }
        throw new DadoInvalidoException("Tipo de crédito inválido: " + texto
                + ". Valores aceitos: EMPRESTIMO_PESSOAL, CREDITO_IMOBILIARIO, CREDITO_AUTOMOTIVO, CONSIGNADO.");
    }
}

package br.com.fiap.bank.core.application.exception;

import br.com.fiap.bank.core.domain.exception.DadoInvalidoException;
import br.com.fiap.bank.core.domain.exception.DomainException;

import java.util.function.Supplier;

/**
 * Traduz excecoes de dominio (puras) em excecoes de aplicacao, para que a camada de
 * apresentacao nunca precise conhecer o dominio.
 */
public final class TradutorDeExcecoes {

    private TradutorDeExcecoes() {
    }

    public static <T> T executar(Supplier<T> acao) {
        try {
            return acao.get();
        } catch (DomainException e) {
            throw traduzir(e);
        }
    }

    public static void executarSemRetorno(Runnable acao) {
        try {
            acao.run();
        } catch (DomainException e) {
            throw traduzir(e);
        }
    }

    private static RuntimeException traduzir(DomainException e) {
        if (e instanceof DadoInvalidoException) {
            return new DadosInvalidosException(e.getMessage());
        }
        return new ConflitoException(e.getMessage());
    }
}

package br.com.fiap.bank.core.presentation.exception;

import br.com.fiap.bank.core.application.exception.ConflitoException;
import br.com.fiap.bank.core.application.exception.DadosInvalidosException;
import br.com.fiap.bank.core.application.exception.RecursoNaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Tratamento global de excecoes: padroniza todas as respostas de erro no formato
 * {@code application/problem+json} (RFC 9457), sem vazar detalhes internos.
 * Erros nativos do Spring MVC (JSON malformado, tipo de parametro invalido, rota inexistente...)
 * continuam tratados pela superclasse.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ProblemDetail> tratarNaoEncontrado(RecursoNaoEncontradoException ex,
                                                             HttpServletRequest request) {
        return resposta(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage(), request);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ProblemDetail> tratarConflito(ConflitoException ex, HttpServletRequest request) {
        return resposta(HttpStatus.CONFLICT, "Conflito de negócio", ex.getMessage(), request);
    }

    @ExceptionHandler(DadosInvalidosException.class)
    public ResponseEntity<ProblemDetail> tratarDadosInvalidos(DadosInvalidosException ex,
                                                              HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, "Requisição inválida", ex.getMessage(), request);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> tratarConcorrencia(OptimisticLockingFailureException ex,
                                                            HttpServletRequest request) {
        return resposta(HttpStatus.CONFLICT, "Conflito de concorrência",
                "O recurso foi alterado por outra operação simultânea. Tente novamente.", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> tratarIntegridade(DataIntegrityViolationException ex,
                                                           HttpServletRequest request) {
        log.warn("Violação de integridade de dados: {}", ex.getMostSpecificCause().getMessage());
        return resposta(HttpStatus.CONFLICT, "Conflito de dados",
                "A operação viola uma restrição de integridade (registro duplicado ou em uso).", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> tratarErroInesperado(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado ao processar {} {}", request.getMethod(), request.getRequestURI(), ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.", request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Um ou mais campos da requisição são inválidos.");
        problema.setTitle("Requisição inválida");
        problema.setProperty("timestamp", Instant.now());
        List<Map<String, String>> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> Map.of(
                        "campo", erro.getField(),
                        "mensagem", erro.getDefaultMessage() == null ? "valor inválido" : erro.getDefaultMessage()))
                .toList();
        problema.setProperty("erros", erros);
        return handleExceptionInternal(ex, problema, headers, HttpStatus.BAD_REQUEST, request);
    }

    private ResponseEntity<ProblemDetail> resposta(HttpStatus status, String titulo, String detalhe,
                                                   HttpServletRequest request) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        problema.setProperty("timestamp", Instant.now());
        problema.setProperty("caminho", request.getRequestURI());
        return ResponseEntity.status(status).body(problema);
    }
}

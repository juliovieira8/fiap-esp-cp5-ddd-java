package br.com.fiap.bank.core.application.service;

import br.com.fiap.bank.core.application.dto.ClienteResponse;
import br.com.fiap.bank.core.application.dto.CriarClienteRequest;
import br.com.fiap.bank.core.application.exception.ConflitoException;
import br.com.fiap.bank.core.application.exception.DadosInvalidosException;
import br.com.fiap.bank.core.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.bank.core.application.exception.TradutorDeExcecoes;
import br.com.fiap.bank.core.domain.model.Cliente;
import br.com.fiap.bank.core.domain.model.Cpf;
import br.com.fiap.bank.core.domain.model.Email;
import br.com.fiap.bank.core.domain.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static br.com.fiap.bank.core.application.exception.TradutorDeExcecoes.executar;
import static br.com.fiap.bank.core.application.exception.TradutorDeExcecoes.executarSemRetorno;

/** Casos de uso do recurso Cliente. */
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.listarTodos().stream().map(ClienteResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(UUID id) {
        return ClienteResponse.de(obterPorId(id));
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorCpf(String cpf) {
        Cpf cpfValido = executar(() -> Cpf.de(cpf));
        return clienteRepository.buscarPorCpf(cpfValido)
                .map(ClienteResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cliente não encontrado para o CPF informado."));
    }

    @Transactional
    public ClienteResponse criar(CriarClienteRequest request) {
        Cpf cpf = executar(() -> Cpf.de(request.cpf()));
        if (clienteRepository.existePorCpf(cpf)) {
            throw new ConflitoException("Já existe um cliente cadastrado com o CPF informado.");
        }
        Cliente cliente = executar(() -> Cliente.criar(cpf, request.nome(), Email.de(request.email()),
                request.dataNascimento(), request.rendaMensal()));
        return ClienteResponse.de(clienteRepository.salvar(cliente));
    }

    @Transactional
    public ClienteResponse atualizar(UUID id, CriarClienteRequest request) {
        Cliente cliente = obterPorId(id);
        Cpf cpfInformado = executar(() -> Cpf.de(request.cpf()));
        if (!cliente.getCpf().equals(cpfInformado)) {
            throw new DadosInvalidosException("O CPF do cliente não pode ser alterado.");
        }
        executarSemRetorno(() -> cliente.atualizarDados(request.nome(), Email.de(request.email()),
                request.dataNascimento(), request.rendaMensal()));
        return ClienteResponse.de(clienteRepository.salvar(cliente));
    }

    @Transactional
    public void remover(UUID id) {
        obterPorId(id);
        clienteRepository.remover(id);
    }

    private Cliente obterPorId(UUID id) {
        return clienteRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado: " + id));
    }
}

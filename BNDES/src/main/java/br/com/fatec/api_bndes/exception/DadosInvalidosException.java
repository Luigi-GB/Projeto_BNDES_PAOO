package br.com.fatec.api_bndes.exception;

import java.util.List;

/** Dados de um cadastro/edição que não passaram na validação do Service (vira HTTP 400). */
public class DadosInvalidosException extends RuntimeException {

    private final List<String> erros;

    public DadosInvalidosException(List<String> erros) {
        super("Dados inválidos: " + String.join("; ", erros));
        this.erros = List.copyOf(erros);
    }

    public List<String> getErros() {
        return erros;
    }
}

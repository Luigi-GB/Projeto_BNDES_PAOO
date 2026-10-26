package br.com.fatec.api_bndes.exception;

/** O arquivo inteiro não pode ser processado (vazio, sem cabeçalho, colunas obrigatórias ausentes...). */
public class ArquivoInvalidoException extends RuntimeException {

    public ArquivoInvalidoException(String mensagem) {
        super(mensagem);
    }
}

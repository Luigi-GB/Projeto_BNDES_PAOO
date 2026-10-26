package br.com.fatec.api_bndes.controller;

import br.com.fatec.api_bndes.exception.ArquivoInvalidoException;
import br.com.fatec.api_bndes.exception.DadosInvalidosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * Converte falhas em respostas JSON no formato {status: "ERRO", mensagem: "..."}.
 * Vale só para o ContratoController, para não interceptar o painel nem recursos estáticos inexistentes.
 */
@RestControllerAdvice(assignableTypes = ContratoController.class)
public class TratadorDeErros {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);

    public record RespostaErro(String status, String mensagem) {
    }

    @ExceptionHandler(ArquivoInvalidoException.class)
    public ResponseEntity<RespostaErro> arquivoInvalido(ArquivoInvalidoException e) {
        return resposta(HttpStatus.BAD_REQUEST, "Erro: " + e.getMessage());
    }

    @ExceptionHandler(DadosInvalidosException.class)
    public ResponseEntity<RespostaErro> dadosInvalidos(DadosInvalidosException e) {
        return resposta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /** JSON mal formado, data em formato errado, número onde se esperava texto etc. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespostaErro> jsonInvalido(HttpMessageNotReadableException e) {
        return resposta(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido: confira o JSON e os formatos (datas como AAAA-MM-DD, valores numéricos).");
    }

    /** Ex.: /api/operacoes/abc (id não numérico) ou dataInicio=31/12/2025 no filtro. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RespostaErro> parametroInvalido(MethodArgumentTypeMismatchException e) {
        return resposta(HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro '" + e.getName() + "'.");
    }

    @ExceptionHandler({MissingServletRequestPartException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<RespostaErro> semArquivo(Exception e) {
        return resposta(HttpStatus.BAD_REQUEST, "Erro: nenhum arquivo foi enviado.");
    }

    @SuppressWarnings("deprecation")
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<RespostaErro> arquivoGrande(MaxUploadSizeExceededException e) {
        return resposta(HttpStatus.PAYLOAD_TOO_LARGE,
                "Erro: o arquivo excede o tamanho máximo permitido (veja spring.servlet.multipart.max-file-size).");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaErro> inesperado(Exception e) {
        log.error("Falha inesperada na API de operações", e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado ao processar a requisição. Veja o log do servidor.");
    }

    private ResponseEntity<RespostaErro> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new RespostaErro("ERRO", mensagem));
    }
}

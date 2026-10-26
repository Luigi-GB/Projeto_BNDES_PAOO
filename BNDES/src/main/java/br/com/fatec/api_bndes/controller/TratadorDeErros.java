package br.com.fatec.api_bndes.controller;

import br.com.fatec.api_bndes.exception.ArquivoInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * Converte falhas do arquivo inteiro em respostas JSON no mesmo formato (status ERRO + mensagem).
 * Vale só para o ImportacaoController, para não interceptar erros dos demais controllers
 * (clientes e operações) nem recursos estáticos inexistentes.
 */
@RestControllerAdvice(assignableTypes = ImportacaoController.class)
public class TratadorDeErros {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);

    public record RespostaErro(String status, String mensagem) {
    }

    @ExceptionHandler(ArquivoInvalidoException.class)
    public ResponseEntity<RespostaErro> arquivoInvalido(ArquivoInvalidoException e) {
        return resposta(HttpStatus.BAD_REQUEST, "Erro: " + e.getMessage());
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
        log.error("Falha inesperada ao importar arquivo", e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado ao processar o arquivo. Veja o log do servidor.");
    }

    private ResponseEntity<RespostaErro> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new RespostaErro("ERRO", mensagem));
    }
}

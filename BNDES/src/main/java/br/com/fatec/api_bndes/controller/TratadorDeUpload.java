package br.com.fatec.api_bndes.controller;

import br.com.fatec.api_bndes.controller.TratadorDeErros.RespostaErro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

/**
 * Trata falhas na leitura do upload (multipart).
 *
 * Elas acontecem antes de o Spring escolher o controller, por isso este tratador NÃO pode ser
 * restrito ao ImportacaoController (um tratador restrito nunca é aplicado a esse tipo de erro e o
 * Spring cai no tratamento padrão, que gera respostas sem corpo e um segundo erro ao renderizar /error).
 * Só reage a MultipartException, então não interfere nos demais controllers.
 */
@RestControllerAdvice
public class TratadorDeUpload {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeUpload.class);
    private static final int MAX_DETALHE = 250;

    private final String limiteArquivo;
    private final String limiteRequisicao;

    public TratadorDeUpload(@Value("${spring.servlet.multipart.max-file-size:1MB}") String limiteArquivo,
                            @Value("${spring.servlet.multipart.max-request-size:10MB}") String limiteRequisicao) {
        this.limiteArquivo = limiteArquivo;
        this.limiteRequisicao = limiteRequisicao;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<RespostaErro> arquivoGrande(MaxUploadSizeExceededException e) {
        String detalhe = detalheTecnico(e);
        log.warn("Upload recusado por tamanho. Limites configurados: arquivo={}, requisição={}. Causa: {}",
                limiteArquivo, limiteRequisicao, detalhe);
        return resposta(HttpStatus.CONTENT_TOO_LARGE,
                "Erro: o arquivo excede o tamanho máximo permitido (arquivo: " + limiteArquivo
                        + ", requisição: " + limiteRequisicao + "). Detalhe técnico: " + detalhe);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<RespostaErro> uploadInvalido(MultipartException e) {
        String detalhe = detalheTecnico(e);
        log.warn("Falha ao ler o upload: {}", detalhe);
        return resposta(HttpStatus.BAD_REQUEST,
                "Erro: não foi possível ler o arquivo enviado. Detalhe técnico: " + detalhe);
    }

    /** Mensagem da causa mais interna (ex.: a do Tomcat, que informa o tamanho recebido e o limite). */
    private static String detalheTecnico(Throwable e) {
        Throwable causa = e;
        while (causa.getCause() != null && causa.getCause() != causa) {
            causa = causa.getCause();
        }
        String texto = causa.getMessage() != null ? causa.getMessage() : causa.getClass().getSimpleName();
        return texto.length() <= MAX_DETALHE ? texto : texto.substring(0, MAX_DETALHE) + "…";
    }

    private ResponseEntity<RespostaErro> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new RespostaErro("ERRO", mensagem));
    }
}

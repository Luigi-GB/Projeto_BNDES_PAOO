package br.com.fatec.api_bndes.controller;

import br.com.fatec.api_bndes.dto.RelatorioImportacao;
import br.com.fatec.api_bndes.exception.ArquivoInvalidoException;
import br.com.fatec.api_bndes.service.ImportacaoService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;

@RestController
@RequestMapping("/api/importacao")
public class ImportacaoController {

    private final ImportacaoService importacaoService;

    public ImportacaoController(ImportacaoService importacaoService) {
        this.importacaoService = importacaoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RelatorioImportacao importar(@RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        String nome = arquivo.getOriginalFilename() == null ? "arquivo.csv" : arquivo.getOriginalFilename();
        String minusculo = nome.toLowerCase(Locale.ROOT);
        if (!minusculo.endsWith(".csv") && !minusculo.endsWith(".txt")) {
            throw new ArquivoInvalidoException("Envie um arquivo no formato .csv.");
        }
        return importacaoService.importar(nome, arquivo.getBytes());
    }
}

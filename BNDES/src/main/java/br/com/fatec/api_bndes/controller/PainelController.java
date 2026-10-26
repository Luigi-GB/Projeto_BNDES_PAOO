package br.com.fatec.api_bndes.controller;

import br.com.fatec.api_bndes.dto.OperacaoFinanciamento;
import br.com.fatec.api_bndes.service.OperacoesImportadas;
import br.com.fatec.api_bndes.service.PainelTecnologiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/painel")
public class PainelController {

    private final PainelTecnologiaService painelService;
    private final OperacoesImportadas importadas;

    public PainelController(PainelTecnologiaService painelService, OperacoesImportadas importadas) {
        this.painelService = painelService;
        this.importadas = importadas;
    }

    /** criterio: COMBINADO (padrão), INOVACAO ou PALAVRA_CHAVE. */
    @GetMapping("/tecnologia")
    public ResponseEntity<?> tecnologia(@RequestParam(name = "criterio", defaultValue = "COMBINADO") String criterio) {
        if (!PainelTecnologiaService.criterioValido(criterio)) {
            return ResponseEntity.badRequest().body(Map.of("mensagem",
                    "Critério inválido. Use COMBINADO, INOVACAO ou PALAVRA_CHAVE."));
        }
        List<OperacaoFinanciamento> operacoes = importadas.listar();
        if (operacoes.isEmpty()) {
            return ResponseEntity.status(409).body(Map.of("mensagem",
                    "Nenhum dado disponível. Importe o arquivo CSV primeiro."));
        }
        return ResponseEntity.ok(painelService.montar(operacoes, criterio, importadas.arquivo()));
    }
}

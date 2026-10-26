package br.com.fatec.api_bndes.controller;

import br.com.fatec.api_bndes.repository.ContratoRepository;
import br.com.fatec.api_bndes.service.PainelTecnologiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/painel")
public class PainelController {

    private final PainelTecnologiaService painelService;
    private final ContratoRepository contratoRepository;

    public PainelController(PainelTecnologiaService painelService, ContratoRepository contratoRepository) {
        this.painelService = painelService;
        this.contratoRepository = contratoRepository;
    }

    /** criterio: COMBINADO (padrão), INOVACAO ou PALAVRA_CHAVE. Os dados vêm do banco (Neon). */
    @GetMapping("/tecnologia")
    public ResponseEntity<?> tecnologia(@RequestParam(name = "criterio", defaultValue = "COMBINADO") String criterio) {
        if (!PainelTecnologiaService.criterioValido(criterio)) {
            return ResponseEntity.badRequest().body(Map.of("mensagem",
                    "Critério inválido. Use COMBINADO, INOVACAO ou PALAVRA_CHAVE."));
        }
        if (contratoRepository.count() == 0) {
            return ResponseEntity.status(409).body(Map.of("mensagem",
                    "Nenhum dado no banco. Importe o arquivo CSV primeiro."));
        }
        return ResponseEntity.ok(painelService.montar(contratoRepository.findAll(), criterio, "do banco de dados (Neon)"));
    }
}

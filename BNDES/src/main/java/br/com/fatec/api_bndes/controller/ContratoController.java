package br.com.fatec.api_bndes.controller;

import br.com.fatec.api_bndes.model.Contrato;
import br.com.fatec.api_bndes.service.ContratoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/operacoes")
public class ContratoController {

    private final ContratoService contratoService;

    public ContratoController(ContratoService contratoService){
        this.contratoService = contratoService;
    }

    // GET - listar todos os contratos
    @GetMapping
    public ResponseEntity<List<Contrato>> listar() {
        return ResponseEntity.ok().body(contratoService.listar());
    }
    // GET - buscar contrato por índice(ID)
    @GetMapping("/{id}")
    public ResponseEntity<Contrato> buscarPorId(@PathVariable("id") Integer id) {
        Optional<Contrato> contrato = contratoService.buscarPorId(id);
        return contrato.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST - adicionar contrato
    @PostMapping
    public ResponseEntity<Contrato> salvar(@RequestBody Contrato contrato) {
        Contrato novoContrato = contratoService.salvar(contrato);
        return ResponseEntity.status(201).body(contrato);
    }
    // POST - adiconar vários contratos por um arquivo .txt ou .csv
    /*@PostMapping("/importar")
    public ResponseEntity<String> importarArquivo(@RequestParam("arquivo") MultipartFile arquivo) {

    }*/

    // PUT - atualizar contrato
    @PutMapping("/{id}")
    public ResponseEntity<Contrato> atualizar(@PathVariable("id") Integer id,@RequestBody Contrato contrato) {
        return contratoService.atualizar(id, contrato)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE - remover contrato
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable("id") Integer id) {
        if (contratoService.buscarPorId(id).isPresent()) {
            contratoService.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    /*@GetMapping("/filtro")
    public ResponseEntity<Contrato> filtrar(){

    }*/
}

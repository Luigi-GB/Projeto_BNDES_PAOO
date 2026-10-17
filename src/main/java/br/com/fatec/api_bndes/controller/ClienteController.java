package br.com.fatec.api_bndes.controller;

import br.com.fatec.api_bndes.model.Cliente;
import br.com.fatec.api_bndes.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/operacoes")
public class ClienteController {
    //TODO - Intgrar o Service

    @GetMapping
    public ResponseEntity<List<Cliente>> listar() {

    }
    @GetMapping("/id")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable Integer id) {

    }
    @PostMapping
    public ResponseEntity<Cliente> salvar(@RequestBody Cliente cliente) {

    }
    @PostMapping("/importar")
    public ResponseEntity<String> importarArquivo(@RequestParam("arquivo") MultipartFile arquivo) {

    }
    @PutMapping("/{id}")
    public ResponseEntity<Cliente> atualizar(@PathVariable("id") Integer id,@RequestBody Cliente cliente) {

    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {

    }
    @GetMapping("/filtro")
    public ResponseEntity<Cliente> filtrar(){

    }


}

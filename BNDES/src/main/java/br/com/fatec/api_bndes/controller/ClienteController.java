package br.com.fatec.api_bndes.controller;

import br.com.fatec.api_bndes.model.Cliente;
import br.com.fatec.api_bndes.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/operacoes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService){
        this.clienteService = clienteService;
    }

    // GET - listar todos os clientes
    @GetMapping
    public ResponseEntity<List<Cliente>> listar() {
        return ResponseEntity.ok().body(clienteService.listar());
    }
    // GET - buscar cliente por índice(ID)
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable("id") Integer id) {
        Optional<Cliente> cliente = clienteService.buscarPorId(id);
        return cliente.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    // POST - adicionar cliente
    @PostMapping
    public ResponseEntity<Cliente> salvar(@RequestBody Cliente cliente) {
        Cliente novoCliente = clienteService.salvar(cliente);
        return ResponseEntity.status(201).body(cliente);
    }
    // POST - adiconar vários clientes por um arquivo .txt ou .csv
    /*@PostMapping("/importar")
    public ResponseEntity<String> importarArquivo(@RequestParam("arquivo") MultipartFile arquivo) {

    }*/
    // PUT - atualizar cliente
    @PutMapping("/{id}")
    public ResponseEntity<Cliente> atualizar(@PathVariable("id") Integer id,@RequestBody Cliente cliente) {
        return clienteService.atualizar(id, cliente)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    // DELETE - remover cliente
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable("id") Integer id) {
        if (clienteService.buscarPorId(id).isPresent()) {
            clienteService.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    /*@GetMapping("/filtro")
    public ResponseEntity<Cliente> filtrar(){

    }*/


}
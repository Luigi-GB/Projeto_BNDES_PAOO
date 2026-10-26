package br.com.fatec.api_bndes.controller;

import br.com.fatec.api_bndes.dto.AnaliseContrato;
import br.com.fatec.api_bndes.dto.FiltroContrato;
import br.com.fatec.api_bndes.dto.RelatorioImportacao;
import br.com.fatec.api_bndes.dto.ResumoUf;
import br.com.fatec.api_bndes.model.Contrato;
import br.com.fatec.api_bndes.service.ContratoService;
import br.com.fatec.api_bndes.service.ImportacaoService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/operacoes")
public class ContratoController {

    /** Cabeçalho com o total de registros que casam com a consulta (a lista pode vir limitada). */
    public static final String TOTAL_HEADER = "X-Total-Count";

    private final ContratoService contratoService;
    private final ImportacaoService importacaoService;

    public ContratoController(ContratoService contratoService, ImportacaoService importacaoService) {
        this.contratoService = contratoService;
        this.importacaoService = importacaoService;
    }

    // GET - lista todos os registros (use ?limite=N para receber só os N primeiros)
    @GetMapping
    public ResponseEntity<List<AnaliseContrato>> listar(@RequestParam(name = "limite", required = false) Integer limite) {
        return comTotal(contratoService.listar(limite));
    }

    // GET - filtra por critérios do domínio: uf, municipio, porteDoCliente, subsetorBndes, situacaoDoContrato, dataInicio, dataFim
    @GetMapping("/filtro")
    public ResponseEntity<List<AnaliseContrato>> filtrar(@ModelAttribute FiltroContrato filtro,
                                                         @RequestParam(name = "limite", required = false) Integer limite) {
        return comTotal(contratoService.buscarComFiltros(filtro, limite));
    }

    // GET - dados agregados para o gráfico (quantidade e valor contratado por UF)
    @GetMapping("/grafico/uf")
    public ResponseEntity<List<ResumoUf>> graficoPorUf() {
        return ResponseEntity.ok(contratoService.resumirPorUf());
    }

    // GET - busca por ID (200 ou 404)
    @GetMapping("/{id}")
    public ResponseEntity<Contrato> buscarPorId(@PathVariable("id") Integer id) {
        return contratoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST - cadastra um contrato (201 ou 400)
    @PostMapping
    public ResponseEntity<Contrato> salvar(@RequestBody Contrato contrato) {
        Contrato novoContrato = contratoService.salvar(contrato);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoContrato);
    }

    // POST - importa o CSV oficial enviado como multipart/form-data (200 ou 400)
    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RelatorioImportacao> importar(@RequestParam("arquivo") MultipartFile arquivo) {
        RelatorioImportacao relatorio = importacaoService.importar(arquivo);
        HttpStatus status = "ERRO".equals(relatorio.status()) ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(relatorio);
    }

    // PUT - atualiza um contrato (200, 400 ou 404)
    @PutMapping("/{id}")
    public ResponseEntity<Contrato> atualizar(@PathVariable("id") Integer id, @RequestBody Contrato contrato) {
        return contratoService.atualizar(id, contrato)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE - remove um contrato (204 ou 404)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable("id") Integer id) {
        return contratoService.deletar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private ResponseEntity<List<AnaliseContrato>> comTotal(Page<AnaliseContrato> pagina) {
        return ResponseEntity.ok()
                .header(TOTAL_HEADER, String.valueOf(pagina.getTotalElements()))
                .body(pagina.getContent());
    }
}

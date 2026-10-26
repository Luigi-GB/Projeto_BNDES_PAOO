package br.com.fatec.api_bndes.service;

import br.com.fatec.api_bndes.dto.OperacaoFinanciamento;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Guarda em memória as operações válidas da última importação, para o painel poder agregá-las.
 * Enquanto a gravação no banco não existe, é daqui que o painel lê; quando existir, basta o
 * PainelTecnologiaService passar a receber a lista vinda do repositório (ele não depende desta classe).
 */
@Component
public class OperacoesImportadas {

    private volatile List<OperacaoFinanciamento> operacoes = List.of();
    private volatile String arquivo = "";

    public void substituir(String arquivo, List<OperacaoFinanciamento> novas) {
        this.operacoes = List.copyOf(novas);
        this.arquivo = arquivo;
    }

    public List<OperacaoFinanciamento> listar() {
        return operacoes;
    }

    public String arquivo() {
        return arquivo;
    }
}
